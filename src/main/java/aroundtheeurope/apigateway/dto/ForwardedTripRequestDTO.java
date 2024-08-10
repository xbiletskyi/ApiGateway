package aroundtheeurope.apigateway.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public class ForwardedTripRequestDTO extends TripRequestDTO {

    @NotEmpty(message = "User is required")
    private String userId;

    public ForwardedTripRequestDTO(TripRequestDTO tripRequestDTO, String userId) {
        super(tripRequestDTO); // Calls the copy constructor of the parent class
        this.userId = userId;
    }

    // Getters and setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
