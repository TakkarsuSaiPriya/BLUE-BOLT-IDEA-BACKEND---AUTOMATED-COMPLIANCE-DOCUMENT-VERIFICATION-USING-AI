package com.compliance.authservice.audit;

import com.compliance.authservice.enums.AuditAction;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    AuditAction action();
}