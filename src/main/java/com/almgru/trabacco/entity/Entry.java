package com.almgru.trabacco.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime inserted;
    private LocalDateTime removed;
    private int amount;

    public Entry() { }

    public Entry(LocalDateTime inserted, LocalDateTime removed, int amount) {
        this.inserted = inserted;
        this.removed = removed;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getInserted() {
        return inserted;
    }

    public void setInserted(LocalDateTime inserted) {
        this.inserted = inserted;
    }

    public LocalDateTime getRemoved() {
        return removed;
    }

    public void setRemoved(LocalDateTime removed) {
        this.removed = removed;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return amount == entry.amount && Objects.equals(id, entry.id) && Objects.equals(inserted, entry.inserted) && Objects.equals(removed, entry.removed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inserted, removed, amount);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", inserted=" + inserted +
                ", removed=" + removed +
                ", amount=" + amount +
                '}';
    }
}
