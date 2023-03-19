package com.example.user.jpa;

import javax.persistence.Entity;

import com.example.user.dto.ERole;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ERole name;

  public RoleEntity() {

  }

  public RoleEntity(ERole name) {
    this.name = name;
  }
}
