package com.almgru.trabacco.data;

import com.almgru.trabacco.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
}
