package com.arenafinder.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "arenas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Arena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String openTime;

    @Column(nullable = false)
    private String closeTime;

    @Column(nullable = false)
    private double pricePerHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sport sport;

    public enum Sport {
        FOOTBALL,
        BADMINTON,
        BOTH
    }

    @Column(nullable = false)
    private Long ownerId;
}
