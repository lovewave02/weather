package com.portfolio.weatheralert.weather;

import java.util.List;

public record HourlyForecast(
        List<HourlyForecastPoint> points
) {
}

