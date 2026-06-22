package jp.kcgi.bousai.controller;

import jp.kcgi.bousai.dto.HazardZoneResponse;
import jp.kcgi.bousai.service.HazardZoneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 危険区域 REST API（一覧は公開）。
 */
@RestController
@RequestMapping("/api/v1/hazard-zones")
public class HazardZoneController {

    private final HazardZoneService hazardZoneService;

    public HazardZoneController(HazardZoneService hazardZoneService) {
        this.hazardZoneService = hazardZoneService;
    }

    /** 危険区域一覧を取得する（公開）。 */
    @GetMapping
    public List<HazardZoneResponse> list() {
        return hazardZoneService.findAll();
    }
}
