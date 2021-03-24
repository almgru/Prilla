package com.almgru.trabacco;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class AppConfig {
    @Bean
    public Map<LocalDate, Integer> dateToSnusCount() {
        return new TreeMap<>(Comparator.reverseOrder());
    }
}
