package jp.kcgi.bousai.repository;

import jp.kcgi.bousai.domain.HazardZone;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 危険区域のリポジトリ。
 */
public interface HazardZoneRepository extends JpaRepository<HazardZone, Long> {
}
