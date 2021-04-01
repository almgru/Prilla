package com.almgru.trabacco.service;

import com.almgru.trabacco.dto.WeekDataDTO;
import com.almgru.trabacco.projection.WeekDataProjection;
import org.springframework.stereotype.Service;

@Service
public class WeekDataConverter {
    public WeekDataDTO weekDataProjectionToDTO(WeekDataProjection projection) {
        return new WeekDataDTO(projection.date(), projection.amount());
    }
}
