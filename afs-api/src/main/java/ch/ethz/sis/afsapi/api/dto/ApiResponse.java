package ch.ethz.sis.afsapi.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiResponse {
    private final String id;
    private final Object result;
    private final Object error;
}
