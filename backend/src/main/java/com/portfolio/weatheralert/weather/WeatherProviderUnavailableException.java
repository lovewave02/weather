package com.portfolio.weatheralert.weather;

public class WeatherProviderUnavailableException extends RuntimeException {

    public WeatherProviderUnavailableException(String message) {
        super(message);
    }

    public WeatherProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

