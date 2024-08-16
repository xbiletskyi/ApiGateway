package aroundtheeurope.apigateway.dto;

/**
 * A data transfer object representing a logout request.
 * This DTO contains the refresh token that needs to be invalidated during logout.
 */
public class LogoutRequest {

    String refreshToken;

    /**
     * Default constructor for serialization frameworks.
     */
    public LogoutRequest() {}

    /**
     * Constructs a LogoutRequest with the specified refresh token.
     *
     * @param refreshToken the token to be invalidated during logout
     */
    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
