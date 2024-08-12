package aroundtheeurope.apigateway.service;

import aroundtheeurope.apigateway.dto.TripRequestDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

@Service
public class TripRequestValidator {
    private static final String DEFAULT_RETURN_BEFORE = "3000-01-01T00:00:00";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TripRequestDTO validateAndSetDefaults(TripRequestDTO tripRequestDTO) {

        tripRequestDTO.setDepartureAt(dateFormatter(tripRequestDTO.getDepartureAt(), null));

        if (tripRequestDTO.getOrigin() == null || tripRequestDTO.getOrigin().isEmpty() ||
                tripRequestDTO.getBudget() < 50 || tripRequestDTO.getDepartureAt() == null) {
            return null;
        }

        if (tripRequestDTO.getDestination() == null || tripRequestDTO.getDestination().isEmpty()) {
            tripRequestDTO.setDestination(tripRequestDTO.getOrigin());
        }

        tripRequestDTO.setReturnBefore(dateFormatter(tripRequestDTO.getReturnBefore(), DEFAULT_RETURN_BEFORE));

        if (tripRequestDTO.getMaxStay() <= 0) {
            tripRequestDTO.setMaxStay(1);
        }

        if (tripRequestDTO.getMinStay() <= 0) {
            tripRequestDTO.setMinStay(1);
        }

        if (tripRequestDTO.getTimeLimitSeconds() <= 0) {
            tripRequestDTO.setTimeLimitSeconds(10);
        }

        if (tripRequestDTO.getExcludedCities() == null){
            tripRequestDTO.setExcludedCities(new ArrayList<>());
        }

        return tripRequestDTO;
    }

    private String dateFormatter(String date, String defaultValue) {
        if (date == null || date.isEmpty()) {
            return defaultValue;
        }
        try {
            LocalDateTime.parse(date, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime.parse(date + "T00:00:00", DATE_TIME_FORMATTER);
                return date + "T00:00:00";
            } catch (DateTimeParseException e2) {
                return defaultValue;
            }
        }
        return date;
    }
}
