package com.compliance.authservice.config;

import com.compliance.authservice.entity.Role;
import com.compliance.authservice.enums.RoleType;
import com.compliance.authservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(
            RoleRepository roleRepository) {

        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        createRoleIfMissing(
                RoleType.ROLE_ADMIN,
                "Administrator with complete system access"
        );

        createRoleIfMissing(
                RoleType.ROLE_AUDITOR,
                "Auditor with access to audit records and reports"
        );

        createRoleIfMissing(
                RoleType.ROLE_VERIFIER,
                "Compliance officer responsible for verification"
        );

        createRoleIfMissing(
                RoleType.ROLE_USER,
                "Standard user who can upload and manage documents"
        );
    }

    private void createRoleIfMissing(
            RoleType roleType,
            String description) {

        if (!roleRepository.existsByName(roleType)) {

            Role role = new Role();
            role.setName(roleType);
            role.setDescription(description);

            roleRepository.save(role);
        }
    }
}