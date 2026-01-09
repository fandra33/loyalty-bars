package com.loyalty.gateway.service;

import com.loyalty.gateway.exception.CustomExceptions.*;
import com.loyalty.gateway.model.dto.BarDTO;
import com.loyalty.gateway.model.entity.Bar;
import com.loyalty.gateway.model.entity.User;
import com.loyalty.gateway.repository.BarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarService {

    private final BarRepository barRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<BarDTO> getAllActiveBars() {
        return barRepository.findByActiveTrue().stream()
                .map(BarDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BarDTO getBarById(Long barId) {
        Bar bar = barRepository.findById(barId)
                .orElseThrow(() -> new ResourceNotFoundException("Bar not found with id: " + barId));

        if (!bar.getActive()) {
            throw new BadRequestException("Bar is not active");
        }

        return BarDTO.fromEntity(bar);
    }

    @Transactional(readOnly = true)
    public BarDTO getMyBar() {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isBarAdmin()) {
            throw new UnauthorizedException("Only bar admins can access this endpoint");
        }

        List<Bar> bars = barRepository.findByOwnerId(currentUser.getId());

        if (bars.isEmpty()) {
            throw new ResourceNotFoundException("No bar found for current user");
        }

        return BarDTO.fromEntity(bars.get(0));
    }

    @Transactional(readOnly = true)
    public List<BarDTO> searchBars(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveBars();
        }

        return barRepository.searchBars(searchTerm).stream()
                .map(BarDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Bar getBarEntity(Long barId) {
        return barRepository.findById(barId)
                .orElseThrow(() -> new ResourceNotFoundException("Bar not found with id: " + barId));
    }

    public void verifyBarOwnership(Long barId, User user) {
        Bar bar = getBarEntity(barId);
        if (!bar.isOwnedBy(user)) {
            throw new UnauthorizedException("You don't have permission to manage this bar");
        }
    }
}
