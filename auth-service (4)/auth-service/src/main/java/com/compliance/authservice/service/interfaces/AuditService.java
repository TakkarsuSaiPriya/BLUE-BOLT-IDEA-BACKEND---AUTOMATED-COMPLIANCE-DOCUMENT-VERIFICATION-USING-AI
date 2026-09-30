package com.compliance.authservice.service.interfaces;

import com.compliance.authservice.entity.AuditLog;

public interface AuditService {

    AuditLog save(AuditLog auditLog);
}