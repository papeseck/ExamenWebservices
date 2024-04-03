package com.groupeisi.domain;

import static com.groupeisi.domain.CalendarTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.groupeisi.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CalendarTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Calendar.class);
        Calendar calendar1 = getCalendarSample1();
        Calendar calendar2 = new Calendar();
        assertThat(calendar1).isNotEqualTo(calendar2);

        calendar2.setId(calendar1.getId());
        assertThat(calendar1).isEqualTo(calendar2);

        calendar2 = getCalendarSample2();
        assertThat(calendar1).isNotEqualTo(calendar2);
    }
}
