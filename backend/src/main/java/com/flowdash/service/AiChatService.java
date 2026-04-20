package com.flowdash.service;

import com.flowdash.domain.AiProviderKey;
import com.flowdash.domain.AiProviderSetting;
import com.flowdash.domain.AppUser;
import com.flowdash.dto.AiChatChunk;
import com.flowdash.dto.AiChatRequest;
import com.flowdash.security.CurrentUserService;
import com.flowdash.service.ai.AiGenerationRequest;
import com.flowdash.service.ai.AiGenerationResult;
import com.flowdash.service.ai.AiMessage;
import com.flowdash.service.ai.AiProviderRegistry;
import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiChatService {

    private final DecisionService decisionService;
    private final DecisionPromptService promptService;
    private final AiSettingsService aiSettingsService;
    private final AiProviderRegistry providerRegistry;
    private final CurrentUserService currentUserService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public AiChatService(DecisionService decisionService,
                         DecisionPromptService promptService,
                         AiSettingsService aiSettingsService,
                         AiProviderRegistry providerRegistry,
                         CurrentUserService currentUserService) {
        this.decisionService = decisionService;
        this.promptService = promptService;
        this.aiSettingsService = aiSettingsService;
        this.providerRegistry = providerRegistry;
        this.currentUserService = currentUserService;
    }

    public SseEmitter streamChat(AiChatRequest request) {
        AppUser currentUser = currentUserService.requireCurrentUser();
        DecisionContext context = loadOrCreateContext(request, currentUser);
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(2).toMillis());
        executorService.submit(() -> {
            try {
                streamDecisionChat(request, context, currentUser, emitter);
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(new AiChatChunk("error", ex.getMessage(), true), MediaType.APPLICATION_JSON));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private void streamDecisionChat(AiChatRequest request, DecisionContext context, AppUser user, SseEmitter emitter) throws IOException {
        AiProviderSetting setting = aiSettingsService.resolveFor(user);
        String systemPrompt = promptService.systemPromptFor(request.tabKey());
        String finalPrompt = promptService.summaryPrompt(request.prompt(), context.thread().getSummary(), request.tabKey());
        List<AiMessage> history = decisionService.listMessages(context.thread()).stream()
                .map(message -> new AiMessage(message.getRole(), message.getContent()))
                .toList();

        AiGenerationRequest generationRequest = new AiGenerationRequest(
                setting.getProviderKey(),
                setting.getModelName(),
                setting.getBaseUrl(),
                aiSettingsService.resolveApiKey(setting.getProviderKey()),
                setting.getTemperature() == null ? new BigDecimal("0.30") : setting.getTemperature(),
                systemPrompt,
                finalPrompt,
                history
        );

        String userMessage = request.prompt();
        decisionService.addMessage(context.thread(), new com.flowdash.dto.DecisionMessageRequest(userMessage, request.tabKey()), "user", setting.getModelName());

        AiGenerationResult result;
        try {
            result = providerRegistry.generate(setting, generationRequest);
        } catch (Exception ex) {
            AiProviderSetting fallback = new AiProviderSetting(null, AiProviderKey.MOCK, "Mock", "flowdash-mock", null, true, true, new BigDecimal("0.30"));
            result = providerRegistry.generate(fallback, new AiGenerationRequest(AiProviderKey.MOCK, "flowdash-mock", null, "", new BigDecimal("0.30"), systemPrompt, finalPrompt, history));
        }

        String text = result.text() == null || result.text().isBlank()
                ? "No response was returned by the AI provider."
                : result.text();
        sendChunks(emitter, text, result.modelName());
        decisionService.addGeneratedMessage(context.thread(), request.tabKey(), text, result.modelName());
    }

    private void sendChunks(SseEmitter emitter, String text, String modelName) throws IOException {
        String[] tokens = text.split("(?<=\\s)|(?=\\s)");
        StringBuilder buffer = new StringBuilder();
        for (String token : tokens) {
            buffer.append(token);
            emitter.send(SseEmitter.event().name("message").data(new AiChatChunk("delta", token, false), MediaType.APPLICATION_JSON));
        }
        emitter.send(SseEmitter.event().name("message").data(new AiChatChunk("done", buffer.toString(), true), MediaType.APPLICATION_JSON));
    }

    private DecisionContext loadOrCreateContext(AiChatRequest request, AppUser currentUser) {
        if (request.threadId() != null) {
            return new DecisionContext(decisionService.requireOwnedThread(request.threadId(), currentUser.getId()));
        }
        AiProviderSetting setting = aiSettingsService.resolveFor(currentUser);
        String title = request.prompt().length() > 80 ? request.prompt().substring(0, 80) + "..." : request.prompt();
        return new DecisionContext(decisionService.createThread(new com.flowdash.dto.DecisionThreadRequest(title), setting.getProviderKey().name(), setting.getModelName()));
    }

    private record DecisionContext(com.flowdash.domain.DecisionThread thread) {
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
