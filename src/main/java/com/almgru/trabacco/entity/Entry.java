package com.almgru.trabacco.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate date;
    private int amount;

    public Entry() { }

    public Entry(LocalDate date, int amount) {
        this.date = date;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
        return amount == entry.amount && Objects.equals(id, entry.id) && Objects.equals(date, entry.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, amount);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", date=" + date +
                ", amount=" + amount +
                '}';
    }
}
