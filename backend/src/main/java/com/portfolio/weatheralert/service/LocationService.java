package com.portfolio.weatheralert.service;

import java.util.List;

import com.portfolio.weatheralert.domain.Location;
import com.portfolio.weatheralert.repository.LocationRepository;
import com.portfolio.weatheralert.service.dto.CreateLocationRequest;
import com.portfolio.weatheralert.service.dto.LocationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        Location saved = locationRepository.save(new Location(request.name(), request.latitude(), request.longitude()));
        return LocationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> list() {
        return locationRepository.findAll().stream()
                .map(LocationResponse::from)
                .toList();
    }
}

