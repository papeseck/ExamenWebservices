package com.groupeisi.web.rest;

import static com.groupeisi.domain.CalendarAsserts.*;
import static com.groupeisi.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupeisi.IntegrationTest;
import com.groupeisi.domain.Calendar;
import com.groupeisi.repository.CalendarRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CalendarResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CalendarResourceIT {

    private static final Long DEFAULT_DATE = 1L;
    private static final Long UPDATED_DATE = 2L;

    private static final String DEFAULT_DAYOFWEEK = "AAAAAAAAAA";
    private static final String UPDATED_DAYOFWEEK = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/calendars";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCalendarMockMvc;

    private Calendar calendar;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Calendar createEntity(EntityManager em) {
        Calendar calendar = new Calendar().date(DEFAULT_DATE).dayofweek(DEFAULT_DAYOFWEEK);
        return calendar;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Calendar createUpdatedEntity(EntityManager em) {
        Calendar calendar = new Calendar().date(UPDATED_DATE).dayofweek(UPDATED_DAYOFWEEK);
        return calendar;
    }

    @BeforeEach
    public void initTest() {
        calendar = createEntity(em);
    }

    @Test
    @Transactional
    void createCalendar() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Calendar
        var returnedCalendar = om.readValue(
            restCalendarMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(calendar)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Calendar.class
        );

        // Validate the Calendar in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCalendarUpdatableFieldsEquals(returnedCalendar, getPersistedCalendar(returnedCalendar));
    }

    @Test
    @Transactional
    void createCalendarWithExistingId() throws Exception {
        // Create the Calendar with an existing ID
        calendar.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCalendarMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(calendar)))
            .andExpect(status().isBadRequest());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCalendars() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        // Get all the calendarList
        restCalendarMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(calendar.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.intValue())))
            .andExpect(jsonPath("$.[*].dayofweek").value(hasItem(DEFAULT_DAYOFWEEK)));
    }

    @Test
    @Transactional
    void getCalendar() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        // Get the calendar
        restCalendarMockMvc
            .perform(get(ENTITY_API_URL_ID, calendar.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(calendar.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.intValue()))
            .andExpect(jsonPath("$.dayofweek").value(DEFAULT_DAYOFWEEK));
    }

    @Test
    @Transactional
    void getNonExistingCalendar() throws Exception {
        // Get the calendar
        restCalendarMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCalendar() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the calendar
        Calendar updatedCalendar = calendarRepository.findById(calendar.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCalendar are not directly saved in db
        em.detach(updatedCalendar);
        updatedCalendar.date(UPDATED_DATE).dayofweek(UPDATED_DAYOFWEEK);

        restCalendarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCalendar.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCalendar))
            )
            .andExpect(status().isOk());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCalendarToMatchAllProperties(updatedCalendar);
    }

    @Test
    @Transactional
    void putNonExistingCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, calendar.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(calendar))
            )
            .andExpect(status().isBadRequest());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(calendar))
            )
            .andExpect(status().isBadRequest());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(calendar)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCalendarWithPatch() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the calendar using partial update
        Calendar partialUpdatedCalendar = new Calendar();
        partialUpdatedCalendar.setId(calendar.getId());

        partialUpdatedCalendar.date(UPDATED_DATE);

        restCalendarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCalendar.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCalendar))
            )
            .andExpect(status().isOk());

        // Validate the Calendar in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCalendarUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCalendar, calendar), getPersistedCalendar(calendar));
    }

    @Test
    @Transactional
    void fullUpdateCalendarWithPatch() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the calendar using partial update
        Calendar partialUpdatedCalendar = new Calendar();
        partialUpdatedCalendar.setId(calendar.getId());

        partialUpdatedCalendar.date(UPDATED_DATE).dayofweek(UPDATED_DAYOFWEEK);

        restCalendarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCalendar.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCalendar))
            )
            .andExpect(status().isOk());

        // Validate the Calendar in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCalendarUpdatableFieldsEquals(partialUpdatedCalendar, getPersistedCalendar(partialUpdatedCalendar));
    }

    @Test
    @Transactional
    void patchNonExistingCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, calendar.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(calendar))
            )
            .andExpect(status().isBadRequest());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(calendar))
            )
            .andExpect(status().isBadRequest());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCalendar() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        calendar.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCalendarMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(calendar)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Calendar in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCalendar() throws Exception {
        // Initialize the database
        calendarRepository.saveAndFlush(calendar);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the calendar
        restCalendarMockMvc
            .perform(delete(ENTITY_API_URL_ID, calendar.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return calendarRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Calendar getPersistedCalendar(Calendar calendar) {
        return calendarRepository.findById(calendar.getId()).orElseThrow();
    }

    protected void assertPersistedCalendarToMatchAllProperties(Calendar expectedCalendar) {
        assertCalendarAllPropertiesEquals(expectedCalendar, getPersistedCalendar(expectedCalendar));
    }

    protected void assertPersistedCalendarToMatchUpdatableProperties(Calendar expectedCalendar) {
        assertCalendarAllUpdatablePropertiesEquals(expectedCalendar, getPersistedCalendar(expectedCalendar));
    }
}
