package aroundtheeurope.apigateway.service;

import aroundtheeurope.apigateway.dto.TripRequestDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Service responsible for validating and setting default values for trip requests.
 * Ensures that the trip request contains valid data and fills in defaults where necessary.
 */
@Service
public class TripRequestValidator {
    private static final String DEFAULT_RETURN_BEFORE = "3000-01-01T00:00:00";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Validates the given TripRequestDTO and sets default values for any missing or invalid fields.
     *
     * @param tripRequestDTO the trip request to validate and modify
     * @return the modified TripRequestDTO with defaults applied, or null if validation fails
     */
    public TripRequestDTO validateAndSetDefaults(TripRequestDTO tripRequestDTO) {

        // Validate and format the departure date
        tripRequestDTO.setDepartureAt(dateFormatter(tripRequestDTO.getDepartureAt(), null));

        // Validate required fields and basic constraints
        if (tripRequestDTO.getOrigin() == null || tripRequestDTO.getOrigin().isEmpty() ||
                tripRequestDTO.getBudget() < 50 || tripRequestDTO.getDepartureAt() == null) {
            return null;
        }

        // Set the destination to origin if not provided
        if (tripRequestDTO.getDestination() == null || tripRequestDTO.getDestination().isEmpty()) {
            tripRequestDTO.setDestination(tripRequestDTO.getOrigin());
        }

        // Validate and set the return date
        tripRequestDTO.setReturnBefore(dateFormatter(tripRequestDTO.getReturnBefore(), DEFAULT_RETURN_BEFORE));

        // Ensure positive values for stay duration
        if (tripRequestDTO.getMaxStay() <= 0) {
            tripRequestDTO.setMaxStay(1);
        }

        if (tripRequestDTO.getMinStay() <= 0) {
            tripRequestDTO.setMinStay(1);
        }

        // Ensure a valid time limit for processing
        if (tripRequestDTO.getTimeLimitSeconds() <= 0) {
            tripRequestDTO.setTimeLimitSeconds(10);
        }

        // Initialize excluded cities if null
        if (tripRequestDTO.getExcludedAirports() == null){
            tripRequestDTO.setExcludedAirports(new ArrayList<>());
        }

        return tripRequestDTO;
    }

    /**
     * Helper method to format date strings and apply a default value if formatting fails.
     *
     * @param date the date string to format
     * @param defaultValue the default value to apply if the date is invalid
     * @return the formatted date string or the default value
     */
    private String dateFormatter(String date, String defaultValue) {
        if (date == null || date.isEmpty()) {
            return defaultValue;
        }
        try {
            LocalDateTime.parse(date, DATE_TIME_FORMATTER); // Validate the date format
        } catch (DateTimeParseException e) {
            try {
                // Attempt to parse with a time component if missing
                LocalDateTime.parse(date + "T00:00:00", DATE_TIME_FORMATTER);
                return date + "T00:00:00";
            } catch (DateTimeParseException e2) {
                return defaultValue;    // Return default if all parsing attempts fail
            }
        }
        return date;
    }
}
