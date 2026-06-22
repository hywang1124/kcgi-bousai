package jp.kcgi.bousai.service;

import jp.kcgi.bousai.dto.HazardZoneResponse;
import jp.kcgi.bousai.repository.HazardZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 危険区域のビジネスロジック。
 */
@Service
public class HazardZoneService {

    private final HazardZoneRepository hazardZoneRepository;

    public HazardZoneService(HazardZoneRepository hazardZoneRepository) {
        this.hazardZoneRepository = hazardZoneRepository;
    }

    @Transactional(readOnly = true)
    public List<HazardZoneResponse> findAll() {
        return hazardZoneRepository.findAll().stream()
                .map(HazardZoneResponse::from)
                .toList();
    }
}
