package org.example.sddinventory.model;

import java.time.ZonedDateTime;

public class ErrorResponseDTO {
    private int status;
    private String error;
    private String message;
    private ZonedDateTime timestamp;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(int status, String error, String message, ZonedDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
