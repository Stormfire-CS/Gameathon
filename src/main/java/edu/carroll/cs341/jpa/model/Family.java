package edu.carroll.cs341.jpa.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Family")
public class Family {

    @Id
    @GeneratedValue
    private Long family_ID;

    @Column(name = "family_name", nullable = false, unique = true)
    private String familyName;

    public Family() {
    }

    public Family(String familyName) {
        this.familyName = familyName;
    }

    public Long getFamily_ID() {
        return family_ID;
    }

    public void setFamily_ID() {
        this.family_ID = family_ID;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public String toString() {

    }
}
