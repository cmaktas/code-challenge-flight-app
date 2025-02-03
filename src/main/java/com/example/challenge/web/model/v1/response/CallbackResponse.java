package com.example.challenge.web.model.v1.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallbackResponse {

    private Long seatId;
    private String status;
    private String message;
}

