package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "GamesOwned")
public class GamesOwned {

    @GeneratedValue
    @Id
    private Long gameOwnedID;

    @Column(name = "gameID", nullable = false)
    private Long gameID;

    @Column(name = "ownerID", nullable = false)
    private Long ownerID;

    public Long getGameOwnedID() {
        return gameOwnedID;
    }

    public void setGameOwnedID(Long gameOwnedID) {
        this.gameOwnedID = gameOwnedID;
    }

    public Long getGameID() {
        return gameID;
    }

    public void setGameID(Long gameID) {
        this.gameID = gameID;
    }

    public Long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Long ownerID) {
        this.ownerID = ownerID;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game Owned ID = ").append(gameOwnedID).append(EOL);
        builder.append("Game ID = ").append(gameID).append(EOL);
        builder.append("Owner ID = ").append(ownerID);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final GamesOwned gameOwned = (GamesOwned)o;
        return gameOwnedID.equals(gameOwned.gameOwnedID);
    }
}
