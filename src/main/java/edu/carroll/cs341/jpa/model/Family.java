package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "Families")
public class Family {

    @Id
    @GeneratedValue
    private Long familyID;

    @Column(name = "familyName", nullable = false, unique = true)
    private String familyName;

    @Column(name = "familyAdmin", nullable = false)
    private Long familyAdminId;

    public Family() {
    }

    public Family(String familyName, Long familyAdminId){
        this.familyName = familyName;
        this.familyAdminId = familyAdminId;
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

    public Long getFamilyAdminId() {
        return familyAdminId;
    }

    public void setFamilyAdminId (Long familyAdminId) {
        this.familyAdminId = familyAdminId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Family: family ID = ").append(familyID).append(EOL);
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
