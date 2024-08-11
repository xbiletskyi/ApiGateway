package aroundtheeurope.apigateway.configuration;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;

public class PassThroughResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        // Always return false, so RestTemplate won't treat any response as an error
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        // No-op: We don't want to handle the error, just pass it through
    }
}

