package com.compliance.verificationservice.repository;

import com.compliance.verificationservice.entity.VerificationRuleResult;
import com.compliance.verificationservice.enums.RuleOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRuleResultRepository
        extends JpaRepository<VerificationRuleResult, Long> {

    List<VerificationRuleResult>
    findAllByVerificationResultIdOrderByExecutionOrderAscIdAsc(
            Long verificationResultId
    );

    List<VerificationRuleResult>
    findAllByVerificationResultIdAndOutcomeOrderByExecutionOrderAscIdAsc(
            Long verificationResultId,
            RuleOutcome outcome
    );

    long countByVerificationResultIdAndOutcome(
            Long verificationResultId,
            RuleOutcome outcome
    );

    long countByVerificationResultIdAndMandatoryTrueAndOutcome(
            Long verificationResultId,
            RuleOutcome outcome
    );

    void deleteAllByVerificationResultId(
            Long verificationResultId
    );
}