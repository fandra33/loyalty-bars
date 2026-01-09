package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    
    Optional<QRCode> findByCode(String code);
    
    List<QRCode> findByBarIdOrderByCreatedAtDesc(Long barId);
    
    @Query("SELECT q FROM QRCode q WHERE q.used = false AND q.expiresAt > :now")
    List<QRCode> findActiveQRCodes(@Param("now") LocalDateTime now);
    
    @Query("SELECT q FROM QRCode q WHERE q.bar.id = :barId AND q.used = false AND q.expiresAt > :now")
    List<QRCode> findActiveQRCodesByBar(@Param("barId") Long barId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(q) FROM QRCode q WHERE q.used = false AND q.expiresAt < :now")
    Long countExpiredQRCodes(@Param("now") LocalDateTime now);
    
    void deleteByUsedTrueAndCreatedAtBefore(LocalDateTime before);
}
