package com.rms.admin.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ResourceIdExtractor {

    private static final Pattern NUMERIC_SEGMENT = Pattern.compile("\\d+");

    /**
     * Extracts resource ID by method: POST from Location header, GET/PUT/DELETE from path variable.
     */
    public String extract(String method, HttpServletRequest request, HttpServletResponse response) {
        if ("POST".equalsIgnoreCase(method)) {
            return extractResourceIdFromLocation(response.getHeader("Location"));
        }
        if ("GET".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return extractResourceIdFromPath(getPath(request));
        }
        return null;
    }

    /**
     * Parses resource ID from Location header URI (e.g. /assets/123 or https://host/assets/123).
     */
    public String extractResourceIdFromLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        String path = location;
        int schemeEnd = location.indexOf("://");
        if (schemeEnd > 0) {
            int pathStart = location.indexOf('/', schemeEnd + 3);
            path = pathStart >= 0 ? location.substring(pathStart) : location;
        }
        int queryStart = path.indexOf('?');
        if (queryStart >= 0) {
            path = path.substring(0, queryStart);
        }
        path = path.replaceAll("/+$", "");
        if (path.isEmpty()) {
            return null;
        }
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        if (NUMERIC_SEGMENT.matcher(lastSegment).matches()) {
            return lastSegment;
        }
        return lastNumericSegment(path);
    }

    /**
     * Parses first numeric segment from path (e.g. /users/5/profile -> 5).
     */
    public String extractResourceIdFromPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String[] segments = path.split("/");
        for (String segment : segments) {
            if (!segment.isEmpty() && NUMERIC_SEGMENT.matcher(segment).matches()) {
                return segment;
            }
        }
        return null;
    }

    private String getPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path != null && !path.isEmpty()) {
            return path;
        }
        String uri = request.getRequestURI();
        return uri != null ? uri : "";
    }

    private String lastNumericSegment(String path) {
        String[] segments = path.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isEmpty() && NUMERIC_SEGMENT.matcher(segments[i]).matches()) {
                return segments[i];
            }
        }
        return null;
    }
}
