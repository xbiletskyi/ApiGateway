package aroundtheeurope.apigateway.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

/**
 * A data transfer object representing a trip request.
 * This DTO contains all the necessary details to request a trip, including origin, destination, dates, and budget.
 */
public class TripRequestDTO {

    @NotEmpty(message = "Origin is required")
    private String origin;

    private String destination;

    @NotEmpty(message = "Departure date is required")
    private String departureAt;

    private String returnBefore = "3000-01-01";

    @Positive(message = "Budget must be positive")
    private double budget;

    @Positive(message = "Max stay must be positive")
    private int maxStay = 1;

    @Positive(message = "Min stay must be positive")
    private int minStay = 1;

    private boolean schengenOnly = false;

    List<String> excludedAirports = new ArrayList<>();

    @Positive(message = "Time limit must be positive")
    private int timeLimitSeconds = 10;

    /**
     * Default constructor for serialization frameworks.
     */
    public TripRequestDTO() {}

    /**
     * Copy constructor that creates a new TripRequestDTO by copying all fields from another instance.
     *
     * @param other the original TripRequestDTO to copy
     */
    public TripRequestDTO(TripRequestDTO other) {
        this.origin = other.origin;
        this.destination = other.destination;
        this.departureAt = other.departureAt;
        this.returnBefore = other.returnBefore;
        this.budget = other.budget;
        this.maxStay = other.maxStay;
        this.minStay = other.minStay;
        this.schengenOnly = other.schengenOnly;
        this.excludedAirports = new ArrayList<>(other.excludedAirports); // Ensure a deep copy of the list
        this.timeLimitSeconds = other.timeLimitSeconds;
    }

    // Getters and Setters

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureAt() {
        return departureAt;
    }

    public void setDepartureAt(String departureAt) {
        this.departureAt = departureAt;
    }

    public String getReturnBefore() {
        return returnBefore;
    }

    public void setReturnBefore(String returnBefore) {
        this.returnBefore = returnBefore;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getMaxStay() {
        return maxStay;
    }

    public void setMaxStay(int maxStay) {
        this.maxStay = maxStay;
    }

    public int getMinStay() {
        return minStay;
    }

    public void setMinStay(int minStay) {
        this.minStay = minStay;
    }

    public boolean isSchengenOnly() {
        return schengenOnly;
    }

    public void setSchengenOnly(boolean schengenOnly) {
        this.schengenOnly = schengenOnly;
    }

    public List<String> getExcludedAirports() {
        return excludedAirports;
    }

    public void setExcludedAirports(List<String> excludedAirports) {
        this.excludedAirports = excludedAirports;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }
}

