package com.flowdash.web;

import com.flowdash.dto.VaultRequest;
import com.flowdash.dto.VaultResponse;
import com.flowdash.service.ApiMappers;
import com.flowdash.service.VaultService;
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
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public List<VaultResponse> list() {
        return vaultService.list().stream().map(ApiMappers::toVaultResponse).toList();
    }

    @PostMapping
    public VaultResponse create(@Valid @RequestBody VaultRequest request) {
        return ApiMappers.toVaultResponse(vaultService.create(request));
    }

    @PutMapping("/{id}")
    public VaultResponse update(@PathVariable Long id, @Valid @RequestBody VaultRequest request) {
        return ApiMappers.toVaultResponse(vaultService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        vaultService.delete(id);
    }
}
