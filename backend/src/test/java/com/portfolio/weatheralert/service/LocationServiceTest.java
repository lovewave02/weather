package com.portfolio.weatheralert.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.service.dto.CreateLocationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LocationServiceTest {

    private final LocationRepository locationRepository = Mockito.mock(LocationRepository.class);
    private final LocationService service = new LocationService(locationRepository);

    @Test
    void create_rejectsDuplicateCoordinates() {
        CreateLocationRequest request = new CreateLocationRequest("Seoul", 37.5665, 126.9780);
        given(locationRepository.findByLatitudeAndLongitude(37.5665, 126.9780))
                .willReturn(Optional.of(new Location("Existing", 37.5665, 126.9780)));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("location already exists");
    }
}
