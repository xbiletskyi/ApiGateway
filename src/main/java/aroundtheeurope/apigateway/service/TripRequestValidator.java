package aroundtheeurope.apigateway.service;

import aroundtheeurope.apigateway.dto.TripRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class TripRequestValidator {

    public TripRequestDTO validateAndSetDefaults(TripRequestDTO tripRequestDTO) {
        if (tripRequestDTO.getDestination() == null || tripRequestDTO.getDestination().isEmpty()) {
            tripRequestDTO.setDestination(tripRequestDTO.getOrigin());
        }

        if (tripRequestDTO.getReturnBefore() == null || tripRequestDTO.getReturnBefore().isEmpty()) {
            tripRequestDTO.setReturnBefore("3000-01-01");
        }

        if (tripRequestDTO.getMaxStay() <= 0) {
            tripRequestDTO.setMaxStay(1);
        }

        if (tripRequestDTO.getMinStay() <= 0) {
            tripRequestDTO.setMinStay(1);
        }

        if (tripRequestDTO.getTimeLimitSeconds() <= 0) {
            tripRequestDTO.setTimeLimitSeconds(10);
        }

        return tripRequestDTO;
    }
}
