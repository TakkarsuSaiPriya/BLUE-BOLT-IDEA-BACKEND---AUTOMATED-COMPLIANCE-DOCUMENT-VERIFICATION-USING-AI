package com.compliance.ocrservice.repository;

import com.compliance.ocrservice.entity.OcrResult;
import com.compliance.ocrservice.enums.OcrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrResultRepository
        extends JpaRepository<OcrResult, Long> {

    Optional<OcrResult> findByIdAndActiveTrue(
            Long id
    );

    Optional<OcrResult>
    findFirstByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
            Long documentId
    );

    List<OcrResult>
    findAllByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
            Long documentId
    );

    List<OcrResult>
    findAllByRequestedByAndActiveTrueOrderByCreatedAtDesc(
            String requestedBy
    );

    List<OcrResult>
    findAllByStatusAndActiveTrueOrderByCreatedAtDesc(
            OcrStatus status
    );

    List<OcrResult> findAllByActiveTrueOrderByCreatedAtDesc();

    boolean existsByDocumentIdAndStatusInAndActiveTrue(
            Long documentId,
            List<OcrStatus> statuses
    );

    long countByStatusAndActiveTrue(
            OcrStatus status
    );

    long countByActiveTrue();
}