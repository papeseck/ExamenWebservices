package com.groupeisi.service;

import com.groupeisi.domain.Calendar;
import com.groupeisi.repository.CalendarRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    private final CalendarRepository calendarRepository;

    @Autowired
    public CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public List<Calendar> getAllCalendars() {
        return calendarRepository.findAll();
    }

    public Optional<Calendar> getCalendarById(Long id) {
        return calendarRepository.findById(id);
    }

    public Calendar saveCalendar(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

    public void deleteCalendar(Long id) {
        calendarRepository.deleteById(id);
    }
}
