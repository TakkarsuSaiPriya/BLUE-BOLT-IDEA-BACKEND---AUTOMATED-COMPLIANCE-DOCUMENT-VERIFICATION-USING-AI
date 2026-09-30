package com.compliance.authservice.mapper;

import com.compliance.authservice.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditLog clone(AuditLog auditLog);
}