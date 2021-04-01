package com.almgru.trabacco.data;

import com.almgru.trabacco.dto.WeekDataDTO;
import com.almgru.trabacco.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Integer> {
    // TODO: Move DTO conversion to service
    @Query(
            "SELECT new com.almgru.trabacco.dto.WeekDataDTO(e.insertedDate, SUM(e.amount)) FROM Entry e " +
            "GROUP BY e.insertedDate HAVING e.insertedDate BETWEEN :start AND :end"
    )
    List<WeekDataDTO> findByInsertedDateBetweenGroupByDayOfWeek(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
