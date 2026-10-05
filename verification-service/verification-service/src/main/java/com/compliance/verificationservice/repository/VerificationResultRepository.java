package com.compliance.verificationservice.repository;

import com.compliance.verificationservice.entity.VerificationResult;
import com.compliance.verificationservice.enums.VerificationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationResultRepository
        extends JpaRepository<VerificationResult, Long> {

    @EntityGraph(attributePaths = "ruleResults")
    Optional<VerificationResult>
    findWithRuleResultsByIdAndActiveTrue(
            Long id
    );

    Optional<VerificationResult>
    findFirstByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
            Long documentId
    );

    @EntityGraph(attributePaths = "ruleResults")
    Optional<VerificationResult>
    findFirstWithRuleResultsByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
            Long documentId
    );

    Optional<VerificationResult>
    findFirstByOcrResultIdAndActiveTrueOrderByCreatedAtDesc(
            Long ocrResultId
    );

    List<VerificationResult>
    findAllByDocumentIdAndActiveTrueOrderByCreatedAtDesc(
            Long documentId
    );

    List<VerificationResult>
    findAllByRequestedByAndActiveTrueOrderByCreatedAtDesc(
            String requestedBy
    );

    List<VerificationResult>
    findAllByStatusAndActiveTrueOrderByCreatedAtDesc(
            VerificationStatus status
    );

    List<VerificationResult>
    findAllByActiveTrueOrderByCreatedAtDesc();

    boolean existsByOcrResultIdAndActiveTrue(
            Long ocrResultId
    );

    long countByActiveTrue();

    long countByStatusAndActiveTrue(
            VerificationStatus status
    );
}