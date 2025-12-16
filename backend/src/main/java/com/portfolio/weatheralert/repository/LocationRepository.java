package com.portfolio.weatheralert.repository;

import java.util.UUID;

import com.portfolio.weatheralert.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}

