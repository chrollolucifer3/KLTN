package com.learning_forum.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeRangeRequest {
    String startDate; // Format: YYYY-MM-DD
    String endDate;   // Format: YYYY-MM-DD
}