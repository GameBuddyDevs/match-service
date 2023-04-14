package com.back2261.matchservice.infrastructure.entity;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "gamer", schema = "schauth")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Gamer implements UserDetails {
    @Id
    private String userId;

    @Column(name = "username", unique = true)
    private String gamerUsername;

    @Column(unique = true, nullable = false)
    private String email;

    private Integer age;
    private String country;
    private byte[] avatar;

    @UpdateTimestamp
    private Date lastModifiedDate;

    private Date lastOnlineDate;

    private String pwd;
    private String gender;
    private Boolean isBlocked = false;

    @ManyToMany
    @JoinTable(
            name = "gamer_keywords_join",
            schema = "schauth",
            joinColumns = @JoinColumn(name = "gamer_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    private Set<Keywords> keywords;

    @ManyToMany
    @JoinTable(
            name = "gamer_games_join",
            schema = "schauth",
            joinColumns = @JoinColumn(name = "gamer_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id"))
    private Set<Games> likedgames;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return pwd;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
