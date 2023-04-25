package com.back2261.matchservice.infrastructure.repository;

import com.back2261.matchservice.infrastructure.entity.Achievements;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementsRepository extends JpaRepository<Achievements, UUID> {

    Optional<Achievements> findByAchievementName(String achievementName);
}
