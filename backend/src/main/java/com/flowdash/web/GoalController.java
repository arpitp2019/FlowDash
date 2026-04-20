package com.flowdash.web;

import com.flowdash.dto.GoalRequest;
import com.flowdash.dto.GoalResponse;
import com.flowdash.service.ApiMappers;
import com.flowdash.service.GoalService;
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
}
