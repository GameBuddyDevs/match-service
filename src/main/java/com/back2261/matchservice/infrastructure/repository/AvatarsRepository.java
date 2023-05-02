package com.back2261.matchservice.infrastructure.repository;

import com.back2261.matchservice.infrastructure.entity.Avatars;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarsRepository extends JpaRepository<Avatars, UUID> {}
