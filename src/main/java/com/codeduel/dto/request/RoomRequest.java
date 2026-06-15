package com.codeduel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class RoomRequest {

    @Data
    public static class Create {
        @NotNull(message = "Problem ID is required")
        private Long problemId;

        private Integer timeLimitSeconds = 1800; // 30 min default
    }

    @Data
    public static class Join {
        @NotBlank(message = "Room code is required")
        private String roomCode;
    }
}
