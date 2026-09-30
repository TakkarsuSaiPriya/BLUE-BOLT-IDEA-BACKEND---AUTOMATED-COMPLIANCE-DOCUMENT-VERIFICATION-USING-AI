package com.compliance.documentservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentServiceApplicationTests {

    @Test
    void applicationClassShouldExist() {

        DocumentServiceApplication application =
                new DocumentServiceApplication();

        assertNotNull(application);
    }
}