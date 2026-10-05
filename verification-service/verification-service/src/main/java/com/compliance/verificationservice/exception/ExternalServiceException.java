package com.compliance.verificationservice.exception;

public class ExternalServiceException
        extends RuntimeException {

    private final String serviceName;

    private final Integer statusCode;

    public ExternalServiceException(
            String serviceName,
            String message) {

        super(message);

        this.serviceName = serviceName;
        this.statusCode = null;
    }

    public ExternalServiceException(
            String serviceName,
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );

        this.serviceName = serviceName;
        this.statusCode = null;
    }

    public ExternalServiceException(
            String serviceName,
            Integer statusCode,
            String message) {

        super(message);

        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(
            String serviceName,
            Integer statusCode,
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );

        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}