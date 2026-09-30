package com.compliance.documentservice.service.interfaces;

import com.compliance.documentservice.storage.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file);

    Resource loadAsResource(String storedFileName);

    boolean exists(String storedFileName);

    void delete(String storedFileName);
}