package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "Owners")
public class Owners {

    @Id
    @GeneratedValue
    private Long ownerID;

    @Column(name = "name", nullable = false, unique = false)
    private String name;

    @Column(name = "familyID", nullable = true, unique = false)
    private Long familyID;

    public Owners() {
    }

    public Owners(String name) {
        this.name = name;
    }

    public Long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Long ownerID) {
        this.ownerID = ownerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Owner: ownerID").append(ownerID).append(EOL);
        builder.append("name = ").append(name);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o ) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Owners owner = (Owners) o;
        return(ownerID.equals(owner.ownerID));
    }


}
