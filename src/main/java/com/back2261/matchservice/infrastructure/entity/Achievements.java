package com.back2261.matchservice.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "achievements", schema = "schappl")
@Getter
@Setter
@NoArgsConstructor
public class Achievements implements Serializable {
    @Id
    private UUID id;

    private String achievementName;
    private Integer value;
    private String description;
}
