package com.flowdash.web;

import com.flowdash.dto.DecisionMessageRequest;
import com.flowdash.dto.DecisionMessageResponse;
import com.flowdash.dto.DecisionThreadRequest;
import com.flowdash.dto.DecisionThreadResponse;
import com.flowdash.service.AiSettingsService;
import com.flowdash.service.ApiMappers;
import com.flowdash.service.DecisionService;
import com.flowdash.service.UserBootstrapService;
import com.flowdash.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionService decisionService;
    private final AiSettingsService aiSettingsService;
    private final UserBootstrapService userBootstrapService;
    private final CurrentUserService currentUserService;

    public DecisionController(DecisionService decisionService,
                              AiSettingsService aiSettingsService,
                              UserBootstrapService userBootstrapService,
                              CurrentUserService currentUserService) {
        this.decisionService = decisionService;
        this.aiSettingsService = aiSettingsService;
        this.userBootstrapService = userBootstrapService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<DecisionThreadResponse> list() {
        return decisionService.listThreads().stream().map(ApiMappers::toDecisionThreadResponse).toList();
    }

    @PostMapping
    public DecisionThreadResponse create(@Valid @RequestBody DecisionThreadRequest request) {
        var user = currentUserService.requireCurrentUser();
        userBootstrapService.ensureDefaultAiSettings(user);
        var setting = aiSettingsService.resolveFor(user);
        return ApiMappers.toDecisionThreadResponse(decisionService.createThread(request, setting.getProviderKey().name(), setting.getModelName()));
    }

    @GetMapping("/{id}")
    public DecisionThreadResponse get(@PathVariable Long id) {
        return ApiMappers.toDecisionThreadResponse(decisionService.requireOwnedThread(id));
    }

    @PutMapping("/{id}")
    public DecisionThreadResponse update(@PathVariable Long id, @Valid @RequestBody DecisionThreadRequest request) {
        return ApiMappers.toDecisionThreadResponse(decisionService.updateThread(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        decisionService.deleteThread(id);
    }

    @GetMapping("/{id}/messages")
    public List<DecisionMessageResponse> listMessages(@PathVariable Long id) {
        return decisionService.listMessages(id).stream().map(ApiMappers::toDecisionMessageResponse).toList();
    }

    @PostMapping("/{id}/messages")
    public DecisionMessageResponse addMessage(@PathVariable Long id, @Valid @RequestBody DecisionMessageRequest request) {
        return ApiMappers.toDecisionMessageResponse(decisionService.addMessage(id, request, "user", null));
    }
}
