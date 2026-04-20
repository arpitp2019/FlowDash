package com.flowdash.web;

import com.flowdash.dto.HabitRequest;
import com.flowdash.dto.HabitResponse;
import com.flowdash.service.ApiMappers;
import com.flowdash.service.HabitService;
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
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> list() {
        return habitService.list().stream().map(ApiMappers::toHabitResponse).toList();
    }

    @PostMapping
    public HabitResponse create(@Valid @RequestBody HabitRequest request) {
        return ApiMappers.toHabitResponse(habitService.create(request));
    }

    @PutMapping("/{id}")
    public HabitResponse update(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return ApiMappers.toHabitResponse(habitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        habitService.delete(id);
    }
}
