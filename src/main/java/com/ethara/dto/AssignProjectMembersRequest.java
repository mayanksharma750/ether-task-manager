package com.ethara.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssignProjectMembersRequest(
        @NotEmpty
        List<Long> memberIds
) {
}
