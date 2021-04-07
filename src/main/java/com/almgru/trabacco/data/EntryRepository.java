package com.almgru.trabacco.data;

import com.almgru.trabacco.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
    Set<Entry> findByAppliedDateIsBetween(LocalDate start, LocalDate end);
}
