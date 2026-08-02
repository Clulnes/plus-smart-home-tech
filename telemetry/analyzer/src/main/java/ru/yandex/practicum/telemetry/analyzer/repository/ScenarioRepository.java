package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    @EntityGraph(attributePaths = {
            "conditions",
            "conditions.sensor",
            "conditions.condition",
            "actions",
            "actions.sensor",
            "actions.action"
    })
    List<Scenario> findByHubId(String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);
}