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

@Service
public class RequestForwardingService {

    private final RestTemplate restTemplate;

    public RequestForwardingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> forwardRequest(
            HttpServletRequest request,
            String targetUrl,
            HttpMethod method,
            Object body
    ) {

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"Authorization".equalsIgnoreCase(headerName)) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                uriBuilder.queryParam(entry.getKey(), value);
            }
        }

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

    private String extractRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        return body.isEmpty() ? null : body;
    }
}
