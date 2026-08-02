package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ScenarioCondition> conditions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ScenarioAction> actions = new HashSet<>();
}