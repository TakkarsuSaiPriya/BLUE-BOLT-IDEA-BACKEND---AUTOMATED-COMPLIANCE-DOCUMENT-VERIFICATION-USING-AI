package com.compliance.documentservice.util;

import com.compliance.documentservice.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class ChecksumUtil {

    private static final int BUFFER_SIZE = 8192;

    public String calculateSha256(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException(
                    "Cannot calculate checksum for an empty file"
            );
        }

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            try (InputStream inputStream =
                         file.getInputStream()) {

                byte[] buffer =
                        new byte[BUFFER_SIZE];

                int bytesRead;

                while ((bytesRead =
                        inputStream.read(buffer)) != -1) {

                    messageDigest.update(
                            buffer,
                            0,
                            bytesRead
                    );
                }
            }

            return convertToHex(
                    messageDigest.digest()
            );

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 checksum algorithm is unavailable",
                    exception
            );

        } catch (IOException exception) {
            throw new InvalidDocumentException(
                    "Unable to calculate document checksum",
                    exception
            );
        }
    }

    private String convertToHex(
            byte[] digest) {

        StringBuilder hexadecimal =
                new StringBuilder(
                        digest.length * 2
                );

        for (byte value : digest) {
            hexadecimal.append(
                    String.format(
                            "%02x",
                            value & 0xFF
                    )
            );
        }

        return hexadecimal.toString();
    }
}