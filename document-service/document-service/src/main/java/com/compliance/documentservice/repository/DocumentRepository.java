package com.compliance.documentservice.repository;

import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndActiveTrue(Long id);

    List<Document> findAllByActiveTrue();

    List<Document> findAllByUploadedByAndActiveTrue(
            String uploadedBy
    );

    List<Document> findAllByStatusAndActiveTrue(
            DocumentStatus status
    );

    List<Document> findAllByDocumentTypeAndActiveTrue(
            DocumentType documentType
    );

    Optional<Document> findByChecksumAndUploadedByAndActiveTrue(
            String checksum,
            String uploadedBy
    );

    boolean existsByChecksumAndUploadedByAndActiveTrue(
            String checksum,
            String uploadedBy
    );

    boolean existsByStoredFileName(String storedFileName);
}