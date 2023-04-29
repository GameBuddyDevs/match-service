package com.back2261.matchservice.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "keywords", schema = "schauth")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Keywords implements Serializable {
    @Id
    private UUID id;

    private String keywordName;

    @CreationTimestamp
    private Date createdDate;

    private String description;

    @ManyToMany(mappedBy = "keywords")
    private Set<Gamer> gamers;
}
