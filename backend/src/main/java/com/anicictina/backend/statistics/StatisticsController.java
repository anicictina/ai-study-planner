package com.anicictina.backend.statistics;

import com.anicictina.backend.statistics.dto.StatisticsOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public StatisticsOverviewResponse getOverview() {
        return statisticsService.getOverview();
    }
}
