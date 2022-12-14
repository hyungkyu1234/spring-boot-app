package io.hyungkyu.app.modules.zone.infra.repository;

import io.hyungkyu.app.modules.zone.domain.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByCityAndProvince(String cityName, String provinceName);
}
