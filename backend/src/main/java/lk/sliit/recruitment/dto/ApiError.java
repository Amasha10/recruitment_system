package lk.sliit.recruitment.dto;

import java.util.Map;

/** Every error the API returns has this shape, so the React app can show it consistently. */
public record ApiError(int status, String message, Map<String, String> fieldErrors) {
    public ApiError(int status, String message) { this(status, message, Map.of()); }
}
