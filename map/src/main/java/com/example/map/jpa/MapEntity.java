package com.example.map.jpa;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import java.util.Date;

@Data
@Entity
@Table(name = "map")
// @EntityListeners(AuditingEntityListener.class)
public class MapEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String xlocation;
    @Column(nullable = false)
    private String ylocation;

    @Column(nullable = false)
    private String userId;
    @Column(nullable = false, unique = true)
    private String mapId;

    @Column(nullable = false)
    private String mapType;

    @CreationTimestamp
    private Instant createdAt;
}
