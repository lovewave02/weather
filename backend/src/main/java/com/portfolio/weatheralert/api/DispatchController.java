package com.portfolio.weatheralert.api;

import com.portfolio.weatheralert.service.AlertDispatchService;
import com.portfolio.weatheralert.service.dto.AlertDispatchRunResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {

    private final AlertDispatchService alertDispatchService;

    public DispatchController(AlertDispatchService alertDispatchService) {
        this.alertDispatchService = alertDispatchService;
    }

    @PostMapping("/alerts")
    public AlertDispatchRunResponse dispatchAlerts() {
        return new AlertDispatchRunResponse(alertDispatchService.dispatchPendingNow());
    }
}
