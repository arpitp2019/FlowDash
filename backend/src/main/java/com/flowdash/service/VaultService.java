package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.VaultEntry;
import com.flowdash.dto.VaultRequest;
import com.flowdash.repository.VaultRepository;
import com.flowdash.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VaultService {

    private final VaultRepository vaultRepository;
    private final CurrentUserService currentUserService;

    public VaultService(VaultRepository vaultRepository, CurrentUserService currentUserService) {
        this.vaultRepository = vaultRepository;
        this.currentUserService = currentUserService;
    }

    public List<VaultEntry> list() {
        return vaultRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public VaultEntry create(VaultRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        VaultEntry item = new VaultEntry(user, request.entryType(), request.title(), request.content(), request.tags(), request.favorite() != null && request.favorite());
        return vaultRepository.save(item);
    }

    public VaultEntry update(Long id, VaultRequest request) {
        VaultEntry item = requireOwnedVault(id);
        item.setTitle(request.title());
        item.setContent(request.content());
        if (request.entryType() != null) {
            item.setEntryType(request.entryType());
        }
        item.setTags(request.tags());
        if (request.favorite() != null) {
            item.setFavorite(request.favorite());
        }
        return vaultRepository.save(item);
    }

    public void delete(Long id) {
        VaultEntry item = requireOwnedVault(id);
        vaultRepository.delete(item);
    }

    private VaultEntry requireOwnedVault(Long id) {
        VaultEntry item = vaultRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vault entry not found"));
        if (!item.getUser().getId().equals(currentUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Vault entry does not belong to the current user");
        }
        return item;
    }
}
