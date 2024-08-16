package aroundtheeurope.apigateway.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for forwarding HTTP requests to target services.
 * It constructs the target URL, copies headers, and sends the request using RestTemplate.
 */
@Service
public class RequestForwardingService {

    private final RestTemplate restTemplate;

    /**
     * Constructor for RequestForwardingService, autowiring the RestTemplate.
     *
     * @param restTemplate the RestTemplate used for sending HTTP requests
     */
    public RequestForwardingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Forwards the HTTP request to the specified target URL with the given method and optional body.
     *
     * @param request the original HttpServletRequest to forward
     * @param targetUrl the target URL where the request should be forwarded
     * @param method the HTTP method to use (GET, POST, etc.)
     * @param body the body content to include in the forwarded request (can be null)
     * @return ResponseEntity with the response from the target service
     */
    public ResponseEntity<String> forwardRequest(
            HttpServletRequest request,
            String targetUrl,
            HttpMethod method,
            Object body
    ) {

        // Copy headers from the original request, excluding Authorization
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"Authorization".equalsIgnoreCase(headerName)) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the target URI with query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                uriBuilder.queryParam(entry.getKey(), value);
            }
        }

        // If the body is null and the method is POST, PUT, or PATCH, extract the request body
        if (body == null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            try {
                body = extractRequestBody(request);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read request body", e);
            }
        }

        URI uri = uriBuilder.build().toUri();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        System.out.println("Forwarded request: " + uri);

        return restTemplate.exchange(uri, method, entity, String.class);
    }

    /**
     * Helper method to extract the body content from the original HTTP request.
     *
     * @param request the original HttpServletRequest
     * @return the body content as a String, or null if empty
     * @throws IOException if an I/O error occurs while reading the request body
     */
    private String extractRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        return body.isEmpty() ? null : body;
    }
}
