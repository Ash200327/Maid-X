package com.example.maidmanager.maid.repository;

import com.example.maidmanager.maid.entity.Maid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaidRepository extends JpaRepository<Maid, UUID> {

    Optional<Maid> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Maid> findAllByOwnerIdAndIsActiveOrderByCreatedAtDesc(UUID ownerId, boolean isActive);

    List<Maid> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Page<Maid> findAllByOwnerId(UUID ownerId, Pageable pageable);

    @Query("SELECT m FROM Maid m WHERE m.owner.id = :ownerId AND LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Maid> searchByName(@Param("ownerId") UUID ownerId, @Param("search") String search);

    long countByOwnerIdAndIsActive(UUID ownerId, boolean isActive);
}
