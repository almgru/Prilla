package com.almgru.trabacco.server.data;

import com.almgru.trabacco.server.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
    Set<Entry> findByAppliedDateIsBetween(LocalDate start, LocalDate end);
}