package aroundtheeurope.apigateway.dto;


/**
 * A data transfer object representing a request to refresh a JWT token.
 * This DTO contains the refresh token and the desired expiration time for the new token.
 */
public class RefreshRequestDTO {

    String refreshToken;
    long expiration;

    /**
     * Default constructor for serialization frameworks.
     */
    public RefreshRequestDTO() {}

    /**
     * Constructs a RefreshRequestDTO with the specified refresh token and expiration time.
     *
     * @param refreshToken the token to be refreshed
     * @param expiration the new expiration time for the token
     */
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
