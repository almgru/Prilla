package com.almgru.prilla.server.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.springframework.data.annotation.Immutable;

@Entity
@Immutable
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate appliedDate;
    private LocalTime appliedTime;

    private LocalDate removedDate;
    private LocalTime removedTime;
    private int amount;

    public Entry() { }

    public Entry(final LocalDateTime appliedAt, final LocalDateTime removedAt, final int amount) {
        this.appliedDate = appliedAt.toLocalDate();
        this.appliedTime = appliedAt.toLocalTime();
        this.removedDate = removedAt.toLocalDate();
        this.removedTime = removedAt.toLocalTime();
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getAppliedAt() {
        return LocalDateTime.of(this.appliedDate, this.appliedTime);
    }

    public LocalDateTime getRemovedAt() {
        return LocalDateTime.of(this.removedDate, this.removedTime);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final var entry = (Entry)other;

        return amount == entry.amount && Objects.equals(id, entry.id) && Objects.equals(appliedDate, entry.appliedDate) &&
            Objects.equals(appliedTime, entry.appliedTime) && Objects.equals(removedDate, entry.removedDate) && Objects.equals(removedTime, entry.removedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appliedDate, appliedTime, removedDate, removedTime, amount);
    }
}
