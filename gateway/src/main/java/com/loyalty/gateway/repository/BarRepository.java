package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.Bar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarRepository extends JpaRepository<Bar, Long> {
    
    List<Bar> findByActiveTrue();
    
    List<Bar> findByOwnerId(Long ownerId);
    
    Optional<Bar> findByIdAndOwnerId(Long id, Long ownerId);
    
    @Query("SELECT b FROM Bar b WHERE b.active = true AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Bar> searchBars(@Param("search") String search);
}
