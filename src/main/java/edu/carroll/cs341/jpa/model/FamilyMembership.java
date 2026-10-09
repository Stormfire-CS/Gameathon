package edu.carroll.cs341.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "FamilyMembership")
public class FamilyMembership {

    @Id
    @GeneratedValue
    private Long membershipID;

    @Column(name = "familyID", nullable = false)
    private Long familyID;

    @Column(name = "userID", nullable = false)
    private Long userID;

    @Column(name = "status", nullable = false)
    private String status;

    public FamilyMembership() {
    }

    public FamilyMembership(Long familyID, Long userID, String status) {
        this.familyID = familyID;
        this.userID = userID;
        this.status = status;
    }

    public Long getMembershipID() {
        return membershipID;
    }

    public void setMembershipID(Long membershipID) {
        this.membershipID = membershipID;
    }

    public Long getFamilyID() {
        return familyID;
    }

    public void setFamilyID(Long familyID) {
        this.familyID = familyID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
