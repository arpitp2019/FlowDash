package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.MindVaultItemSource;
import com.flowdash.domain.MindVaultItemStatus;
import com.flowdash.domain.MindVaultLearningItem;
import com.flowdash.domain.MindVaultReviewLog;
import com.flowdash.domain.MindVaultReviewRating;
import com.flowdash.domain.MindVaultSprint;
import com.flowdash.domain.MindVaultSprintStatus;
import com.flowdash.domain.MindVaultSubject;
import com.flowdash.dto.MindVaultItemRequest;
import com.flowdash.dto.MindVaultReviewRequest;
import com.flowdash.dto.MindVaultSprintRequest;
import com.flowdash.dto.MindVaultSubjectRequest;
import com.flowdash.repository.MindVaultLearningItemRepository;
import com.flowdash.repository.MindVaultReviewLogRepository;
import com.flowdash.repository.MindVaultSprintRepository;
import com.flowdash.repository.MindVaultSubjectRepository;
import com.flowdash.security.CurrentUserService;
import com.flowdash.service.exception.DuplicateResourceException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class MindVaultService {

    private final MindVaultSubjectRepository subjectRepository;
    private final MindVaultSprintRepository sprintRepository;
    private final MindVaultLearningItemRepository itemRepository;
    private final MindVaultReviewLogRepository reviewLogRepository;
    private final CurrentUserService currentUserService;

    public MindVaultService(MindVaultSubjectRepository subjectRepository,
                            MindVaultSprintRepository sprintRepository,
                            MindVaultLearningItemRepository itemRepository,
                            MindVaultReviewLogRepository reviewLogRepository,
                            CurrentUserService currentUserService) {
        this.subjectRepository = subjectRepository;
        this.sprintRepository = sprintRepository;
        this.itemRepository = itemRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.currentUserService = currentUserService;
    }

    public List<MindVaultSubject> listSubjects() {
        return subjectRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public MindVaultSubject createSubject(MindVaultSubjectRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        String title = normalizeTitle(request.title());
        if (subjectRepository.existsByUserIdAndTitleIgnoreCase(userId, title)) {
            throw new DuplicateResourceException("Subject already exists");
        }
        AppUser user = currentUserService.requireCurrentUser();
        MindVaultSubject subject = new MindVaultSubject(
                user,
                title,
                normalizeText(request.description()),
                clamp(request.priority(), 3, 1, 5),
                clamp(request.targetMastery(), 80, 1, 100),
                request.deadline(),
                normalizeTags(request.tags()),
                Boolean.TRUE.equals(request.archived())
        );
        return subjectRepository.save(subject);
    }

    public MindVaultSubject updateSubject(Long id, MindVaultSubjectRequest request) {
        MindVaultSubject subject = requireOwnedSubject(id);
        String title = normalizeTitle(request.title());
        if (!subject.getTitle().equalsIgnoreCase(title) && subjectRepository.existsByUserIdAndTitleIgnoreCase(currentUserService.requireCurrentUserId(), title)) {
            throw new DuplicateResourceException("Subject already exists");
        }
        subject.setTitle(title);
        subject.setDescription(normalizeText(request.description()));
        subject.setPriority(clamp(request.priority(), subject.getPriority(), 1, 5));
        subject.setTargetMastery(clamp(request.targetMastery(), subject.getTargetMastery(), 1, 100));
        subject.setDeadline(request.deadline());
        subject.setTags(normalizeTags(request.tags()));
        if (request.archived() != null) {
            subject.setArchived(request.archived());
        }
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        MindVaultSubject subject = requireOwnedSubject(id);
        List<MindVaultSprint> sprints = listSprints().stream()
                .filter(sprint -> sameId(sprint.getSubject(), subject))
                .peek(sprint -> sprint.setSubject(null))
                .toList();
        if (!sprints.isEmpty()) {
            sprintRepository.saveAll(sprints);
        }

        List<MindVaultLearningItem> items = listItems().stream()
                .filter(item -> sameId(item.getSubject(), subject))
                .peek(item -> item.setSubject(null))
                .toList();
        if (!items.isEmpty()) {
            itemRepository.saveAll(items);
        }

        subjectRepository.delete(subject);
    }

    public List<MindVaultSprint> listSprints() {
        return sprintRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public MindVaultSprint createSprint(MindVaultSprintRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        MindVaultSubject subject = requireOwnedSubject(request.subjectId());
        MindVaultSprint sprint = new MindVaultSprint(
                user,
                subject,
                normalizeTitle(request.title()),
                normalizeText(request.description()),
                request.status() == null ? MindVaultSprintStatus.PLANNED : request.status(),
                request.startDate(),
                request.dueDate(),
                request.estimatedSessions() == null ? 1 : request.estimatedSessions(),
                request.completedSessions() == null ? 0 : request.completedSessions()
        );
        return sprintRepository.save(sprint);
    }

    public MindVaultSprint updateSprint(Long id, MindVaultSprintRequest request) {
        MindVaultSprint sprint = requireOwnedSprint(id);
        MindVaultSubject subject = requireOwnedSubject(request.subjectId());
        sprint.setSubject(subject);
        sprint.setTitle(normalizeTitle(request.title()));
        sprint.setDescription(normalizeText(request.description()));
        sprint.setStatus(request.status() == null ? sprint.getStatus() : request.status());
        sprint.setStartDate(request.startDate());
        sprint.setDueDate(request.dueDate());
        sprint.setEstimatedSessions(request.estimatedSessions() == null ? sprint.getEstimatedSessions() : Math.max(1, request.estimatedSessions()));
        sprint.setCompletedSessions(request.completedSessions() == null ? sprint.getCompletedSessions() : Math.max(0, request.completedSessions()));
        return sprintRepository.save(sprint);
    }

    public void deleteSprint(Long id) {
        MindVaultSprint sprint = requireOwnedSprint(id);
        List<MindVaultLearningItem> items = listItems().stream()
                .filter(item -> sameId(item.getSprint(), sprint))
                .peek(item -> item.setSprint(null))
                .toList();
        if (!items.isEmpty()) {
            itemRepository.saveAll(items);
        }
        sprintRepository.delete(sprint);
    }

    public List<MindVaultLearningItem> listItems() {
        return itemRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public MindVaultLearningItem createItem(MindVaultItemRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        MindVaultSubject subject = resolveSubject(request.subjectId());
        MindVaultSprint sprint = resolveSprint(request.sprintId());
        if (subject == null && sprint != null && sprint.getSubject() != null) {
            subject = sprint.getSubject();
        }
        validateSubjectSprintCompatibility(subject, sprint);
        LocalDate today = today();
        MindVaultLearningItem item = new MindVaultLearningItem(
                user,
                subject,
                sprint,
                request.source() == null ? MindVaultItemSource.PLANNED : request.source(),
                normalizeTitle(request.title()),
                normalizeText(request.prompt()),
                normalizeText(request.answer()),
                normalizeText(request.notes()),
                normalizeTags(request.tags()),
                clamp(request.priority(), 3, 1, 5),
                clamp(request.difficulty(), 3, 1, 5),
                15,
                0,
                0,
                0,
                2.1d,
                1,
                today,
                Instant.now(),
                null,
                MindVaultItemStatus.ACTIVE,
                request.dueDate()
        );
        if (request.status() != null) {
            item.setStatus(request.status());
        }
        return itemRepository.save(item);
    }

    public MindVaultLearningItem updateItem(Long id, MindVaultItemRequest request) {
        MindVaultLearningItem item = requireOwnedItem(id);
        MindVaultSubject subject = resolveSubject(request.subjectId());
        MindVaultSprint sprint = resolveSprint(request.sprintId());
        if (subject == null && sprint != null && sprint.getSubject() != null) {
            subject = sprint.getSubject();
        }
        validateSubjectSprintCompatibility(subject, sprint);
        item.setSubject(subject);
        item.setSprint(sprint);
        item.setSource(request.source() == null ? item.getSource() : request.source());
        item.setTitle(normalizeTitle(request.title()));
        item.setPrompt(normalizeText(request.prompt()));
        item.setAnswer(normalizeText(request.answer()));
        item.setNotes(normalizeText(request.notes()));
        item.setTags(normalizeTags(request.tags()));
        item.setPriority(request.priority() == null ? item.getPriority() : clamp(request.priority(), item.getPriority(), 1, 5));
        item.setDifficulty(request.difficulty() == null ? item.getDifficulty() : clamp(request.difficulty(), item.getDifficulty(), 1, 5));
        item.setDueDate(request.dueDate());
        if (request.status() != null) {
            item.setStatus(request.status());
        }
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        MindVaultLearningItem item = requireOwnedItem(id);
        itemRepository.delete(item);
    }

    public MindVaultReviewLog reviewItem(Long id, MindVaultReviewRequest request) {
        MindVaultLearningItem item = requireOwnedItem(id);
        MindVaultReviewRating rating = MindVaultReviewRating.fromValue(request.rating());
        MindVaultScheduler.MindVaultReviewOutcome outcome = MindVaultScheduler.applyReview(item, rating, today());
        item.setMasteryScore(outcome.masteryScore());
        item.setDifficulty(outcome.difficulty());
        item.setReviewStreak(outcome.reviewStreak());
        item.setReviewCount(outcome.reviewCount());
        item.setSuccessCount(outcome.successCount());
        item.setEaseFactor(outcome.easeFactor());
        item.setReviewIntervalDays(outcome.nextIntervalDays());
        item.setNextReviewDate(outcome.nextReviewDate());
        item.setLastReviewedAt(Instant.now());
        item.setLastRating(outcome.rating());
        item.setStatus(outcome.status());
        MindVaultLearningItem saved = itemRepository.save(item);
        MindVaultReviewLog log = new MindVaultReviewLog(
                currentUserService.requireCurrentUser(),
                saved,
                outcome.rating(),
                outcome.previousIntervalDays(),
                outcome.nextIntervalDays(),
                outcome.masteryScore(),
                outcome.easeFactor(),
                normalizeText(request.note())
        );
        return reviewLogRepository.save(log);
    }

    public MindVaultSnapshot snapshot() {
        Long userId = currentUserService.requireCurrentUserId();
        return new MindVaultSnapshot(
                subjectRepository.findAllByUserIdOrderByUpdatedAtDesc(userId),
                sprintRepository.findAllByUserIdOrderByUpdatedAtDesc(userId),
                itemRepository.findAllByUserIdOrderByUpdatedAtDesc(userId),
                reviewLogRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    public List<MindVaultLearningItem> queue() {
        return buildQueue(listItems());
    }

    static List<MindVaultLearningItem> buildQueue(List<MindVaultLearningItem> items) {
        LocalDate today = today();
        return items.stream()
                .filter(item -> item.getStatus() != MindVaultItemStatus.ARCHIVED && item.getStatus() != MindVaultItemStatus.MASTERED)
                .filter(item -> isDue(item, today))
                .sorted(queueComparator(today))
                .limit(25)
                .toList();
    }

    static boolean isDue(MindVaultLearningItem item, LocalDate today) {
        return effectiveDueDate(item, today).compareTo(today) <= 0;
    }

    static LocalDate effectiveDueDate(MindVaultLearningItem item, LocalDate today) {
        LocalDate nextReviewDate = item.getNextReviewDate();
        LocalDate dueDate = item.getDueDate();
        if (nextReviewDate == null && dueDate == null) {
            return today;
        }
        if (nextReviewDate == null) {
            return dueDate;
        }
        if (dueDate == null) {
            return nextReviewDate;
        }
        return nextReviewDate.isBefore(dueDate) ? nextReviewDate : dueDate;
    }

    static String queueReason(MindVaultLearningItem item, LocalDate today) {
        LocalDate effectiveDue = effectiveDueDate(item, today);
        if (item.getDueDate() != null && !item.getDueDate().isAfter(today)) {
            return item.getNextReviewDate() != null && !item.getNextReviewDate().isAfter(today)
                    ? "deadline + review"
                    : "deadline";
        }
        if (item.getNextReviewDate() != null && !item.getNextReviewDate().isAfter(today)) {
            return "review";
        }
        if (effectiveDue.equals(today)) {
            return "new item";
        }
        return "scheduled";
    }

    private static Comparator<MindVaultLearningItem> queueComparator(LocalDate today) {
        return Comparator
                .comparing((MindVaultLearningItem item) -> effectiveDueDate(item, today))
                .thenComparing(MindVaultLearningItem::getPriority, Comparator.reverseOrder())
                .thenComparing(MindVaultLearningItem::getMasteryScore)
                .thenComparing(MindVaultLearningItem::getDifficulty, Comparator.reverseOrder())
                .thenComparing(MindVaultLearningItem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private MindVaultSubject requireOwnedSubject(Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        MindVaultSubject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        if (!Objects.equals(subject.getUser().getId(), userId)) {
            throw new AccessDeniedException("Subject does not belong to the current user");
        }
        return subject;
    }

    private MindVaultSprint requireOwnedSprint(Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        MindVaultSprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));
        if (!Objects.equals(sprint.getUser().getId(), userId)) {
            throw new AccessDeniedException("Sprint does not belong to the current user");
        }
        return sprint;
    }

    private MindVaultLearningItem requireOwnedItem(Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        MindVaultLearningItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Learning item not found"));
        if (!Objects.equals(item.getUser().getId(), userId)) {
            throw new AccessDeniedException("Learning item does not belong to the current user");
        }
        return item;
    }

    private MindVaultSubject resolveSubject(Long id) {
        return id == null ? null : requireOwnedSubject(id);
    }

    private MindVaultSprint resolveSprint(Long id) {
        return id == null ? null : requireOwnedSprint(id);
    }

    private void validateSubjectSprintCompatibility(MindVaultSubject subject, MindVaultSprint sprint) {
        if (subject != null && sprint != null && sprint.getSubject() != null && !sameId(subject, sprint.getSubject())) {
            throw new IllegalArgumentException("Sprint subject does not match item subject");
        }
    }

    private static boolean sameId(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof MindVaultSubject subjectLeft && right instanceof MindVaultSubject subjectRight) {
            return Objects.equals(subjectLeft.getId(), subjectRight.getId());
        }
        if (left instanceof MindVaultSprint sprintLeft && right instanceof MindVaultSprint sprintRight) {
            return Objects.equals(sprintLeft.getId(), sprintRight.getId());
        }
        if (left instanceof MindVaultLearningItem itemLeft && right instanceof MindVaultLearningItem itemRight) {
            return Objects.equals(itemLeft.getId(), itemRight.getId());
        }
        return Objects.equals(left, right);
    }

    private static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeTitle(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new IllegalArgumentException("Title is required");
        }
        return normalized;
    }

    private static String normalizeTags(String tags) {
        String normalized = normalizeText(tags);
        if (normalized == null) {
            return null;
        }
        return normalized.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private static Integer clamp(Integer value, Integer defaultValue, int min, int max) {
        int candidate = value == null ? defaultValue : value;
        return Math.max(min, Math.min(max, candidate));
    }

    public record MindVaultSnapshot(
            List<MindVaultSubject> subjects,
            List<MindVaultSprint> sprints,
            List<MindVaultLearningItem> items,
            List<MindVaultReviewLog> reviews
    ) {
    }
}
