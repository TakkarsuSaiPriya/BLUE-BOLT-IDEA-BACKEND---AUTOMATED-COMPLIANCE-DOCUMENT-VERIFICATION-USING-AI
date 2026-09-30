package com.compliance.authservice.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseCapture {

    private String requestPayload;

    private String responsePayload;
}