package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "GamePlayers")
public class GamePlayer {

    @Id
    @GeneratedValue
    private Long gamePlayerID;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ownerID", unique = true)
    private Long ownerID;

    public GamePlayer() {
    }

    public GamePlayer(String name) {
        this.name = name;
    }

    public Long getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(Long gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        builder.append("Game Player: game player ID").append(gamePlayerID).append(EOL);
        builder.append("name = ").append(name).append(EOL);
        if (ownerID != null) {
            builder.append("owner ID").append(ownerID).append(EOL);
        } else {
            builder.append("No Owner Assocaited With game player.");
        }
        return builder.toString();
    }
}
