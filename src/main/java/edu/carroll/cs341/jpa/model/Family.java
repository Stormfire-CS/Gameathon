package edu.carroll.cs341.jpa.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "Family")
public class Family {

    @Id
    @GeneratedValue
    private Long familyID;

    @Column(name = "family_name", nullable = false, unique = true)
    private String familyName;

    public Family() {
    }

    public Family(String familyName) {
        this.familyName = familyName;
    }

    public Long getFamilyID() {
        return familyID;
    }

    public void setFamilyID(Long familyID) {
        this.familyID = familyID;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Family: family_ID = ").append(familyID).append(EOL);
        builder.append("familyName = ").append(familyName);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Family family = (Family) o;
        return(familyID.equals(family.familyID));
    }
}
