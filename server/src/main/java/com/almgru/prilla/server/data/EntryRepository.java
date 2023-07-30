package com.almgru.prilla.server.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.almgru.prilla.server.entity.Entry;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
    Set<Entry> findByAppliedDateIsBetween(LocalDate start, LocalDate end);
    List<Entry> findByAppliedDateIsBetween(LocalDate start, LocalDate end, Sort sort);
}
