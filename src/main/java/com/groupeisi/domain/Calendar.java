package com.groupeisi.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Calendar.
 */
@Entity
@Table(name = "calendar")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Calendar implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date")
    private Long date;

    @Column(name = "dayofweek")
    private String dayofweek;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Calendar id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDate() {
        return this.date;
    }

    public Calendar date(Long date) {
        this.setDate(date);
        return this;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getDayofweek() {
        return this.dayofweek;
    }

    public Calendar dayofweek(String dayofweek) {
        this.setDayofweek(dayofweek);
        return this;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek = dayofweek;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Calendar)) {
            return false;
        }
        return getId() != null && getId().equals(((Calendar) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Calendar{" +
            "id=" + getId() +
            ", date=" + getDate() +
            ", dayofweek='" + getDayofweek() + "'" +
            "}";
    }
}
