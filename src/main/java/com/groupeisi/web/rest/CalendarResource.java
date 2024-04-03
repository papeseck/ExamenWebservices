package com.groupeisi.web.rest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services/calendar")
public class CalendarResource {

    @GetMapping("/dayfinder")
    public DayOfWeekResponse findDayOfWeek(@RequestParam("date") String date) {
        // Analyser la date fournie
        LocalDate parsedDate = LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Calculer le jour de la semaine
        DayOfWeek dayOfWeek = parsedDate.getDayOfWeek();

        // Créer une réponse JSON
        DayOfWeekResponse response = new DayOfWeekResponse(parsedDate.toString(), dayOfWeek.toString());

        return response;
    }

    // Classe pour représenter la réponse JSON
    public static class DayOfWeekResponse {

        private String date;
        private String dayOfWeek;

        public DayOfWeekResponse(String date, String dayOfWeek) {
            this.date = date;
            this.dayOfWeek = dayOfWeek;
        }
        // Getters et setters
    }
}
