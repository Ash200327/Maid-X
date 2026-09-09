package com.example.maidmanager.audit.repository;

import com.example.maidmanager.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("SELECT a FROM AuditEvent a WHERE a.owner.id = :ownerId ORDER BY a.createdAt DESC")
    Page<AuditEvent> findAllByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.owner.id = :ownerId AND a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditEvent> findAllByEntity(@Param("ownerId") UUID ownerId,
                                     @Param("entityType") String entityType,
                                     @Param("entityId") UUID entityId);
}
