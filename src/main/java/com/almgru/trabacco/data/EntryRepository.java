package com.almgru.trabacco.data;

import com.almgru.trabacco.dto.EntryDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
}
