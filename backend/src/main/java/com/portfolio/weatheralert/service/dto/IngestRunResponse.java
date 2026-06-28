package com.portfolio.weatheralert.service.dto;

public record IngestRunResponse(
        int totalLocations,
        int fetchedLocations,
        int insertedSnapshots,
        int updatedSnapshots,
        int unchangedSnapshots,
        int providerMisses,
        int alertsCreated
) {
}
