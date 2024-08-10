package aroundtheeurope.apigateway.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;

@Service
public class RequestForwardingService {

    private final RestTemplate restTemplate;

    public RequestForwardingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> forwardRequest(HttpServletRequest request, String targetUrl, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"Authorization".equalsIgnoreCase(headerName)) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                uriBuilder.queryParam(entry.getKey(), value);
            }
        }

        URI uri = uriBuilder.build().toUri();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, method, entity, String.class);
    }
}
