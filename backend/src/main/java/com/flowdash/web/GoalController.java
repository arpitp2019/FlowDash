package com.flowdash.web;

import com.flowdash.dto.GoalActivityRequest;
import com.flowdash.dto.GoalActivityResponse;
import com.flowdash.dto.GoalOverviewResponse;
import com.flowdash.dto.GoalRequest;
import com.flowdash.dto.GoalResponse;
import com.flowdash.service.ApiMappers;
import com.flowdash.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public List<GoalResponse> list() {
        return goalService.list().stream().map(ApiMappers::toGoalResponse).toList();
    }

    @GetMapping("/overview")
    public GoalOverviewResponse overview(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                         @RequestParam(required = false) Integer year) {
        return goalService.overview(date, year);
    }

    @PostMapping
    public GoalResponse create(@Valid @RequestBody GoalRequest request) {
        return ApiMappers.toGoalResponse(goalService.create(request));
    }

    @PutMapping("/{id}")
    public GoalResponse update(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        return ApiMappers.toGoalResponse(goalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        goalService.delete(id);
    }

    @PostMapping("/{id}/activity")
    public GoalActivityResponse markActivity(@PathVariable Long id, @RequestBody GoalActivityRequest request) {
        return goalService.markActivity(id, request);
    }

    @DeleteMapping("/{id}/activity/{date}")
    public void clearActivity(@PathVariable Long id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        goalService.clearActivity(id, date);
    }

    @GetMapping("/{id}/activity")
    public List<GoalActivityResponse> activities(@PathVariable Long id,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return goalService.activities(id, from, to);
    }
}
