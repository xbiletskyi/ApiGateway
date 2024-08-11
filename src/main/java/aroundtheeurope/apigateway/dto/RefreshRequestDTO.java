package aroundtheeurope.apigateway.dto;

import java.util.UUID;

public class RefreshRequestDTO {

    String refreshToken;
    long expiration;

    public RefreshRequestDTO() {}

    public RefreshRequestDTO(
            String refreshToken,
            long expiration
    ) {
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    // Getters and setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
