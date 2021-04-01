package com.almgru.trabacco.entity;

import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Immutable
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate insertedDate;
    private LocalTime insertedTime;

    private LocalDate removedDate;
    private LocalTime removedTime;
    private int amount;

    public Entry() { }

    public Entry(LocalDateTime inserted, LocalDateTime removed, int amount) {
        this.insertedDate = inserted.toLocalDate();
        this.insertedTime = inserted.toLocalTime();
        this.removedDate = removed.toLocalDate();
        this.removedTime = removed.toLocalTime();
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getInserted() {
        return LocalDateTime.of(this.insertedDate, this.insertedTime);
    }

    public LocalDateTime getRemoved() {
        return LocalDateTime.of(this.removedDate, this.removedTime);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return amount == entry.amount && Objects.equals(id, entry.id) && Objects.equals(insertedDate, entry.insertedDate) && Objects.equals(insertedTime, entry.insertedTime) && Objects.equals(removedDate, entry.removedDate) && Objects.equals(removedTime, entry.removedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, insertedDate, insertedTime, removedDate, removedTime, amount);
    }
}
