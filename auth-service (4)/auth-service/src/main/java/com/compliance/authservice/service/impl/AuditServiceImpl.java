package com.compliance.authservice.service.impl;

import com.compliance.authservice.entity.AuditLog;
import com.compliance.authservice.repository.AuditLogRepository;
import com.compliance.authservice.service.interfaces.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
}