package aroundtheeurope.apigateway.dto;

import jakarta.validation.constraints.NotEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * A data transfer object representing a forwarded trip request.
 * This DTO extends TripRequestDTO and adds a userId field to associate the request with a specific user.
 */
public class ForwardedTripRequestDTO extends TripRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "User is required")
    private String userId;

    /**
     * Default constructor for serialization frameworks.
     */
    public ForwardedTripRequestDTO() {}

    /**
     * Constructs a ForwardedTripRequestDTO by copying fields from an existing TripRequestDTO
     * and adding a userId.
     *
     * @param tripRequestDTO the original trip request data transfer object
     * @param userId the ID of the user associated with the request
     */
    public ForwardedTripRequestDTO(
            TripRequestDTO tripRequestDTO,
            String userId
    ) {
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
