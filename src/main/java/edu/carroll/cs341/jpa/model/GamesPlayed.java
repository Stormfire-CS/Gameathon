package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "GamesPlayed")
public class GamesPlayed {

    @Id
    @GeneratedValue
    private Long gamePlayedID;

    @Column(name = "ownedGameID", nullable = false)
    private Long ownedGameID;

    @Column(name = "datePlayed", nullable = false)
    private Date datePlayed;

    public GamesPlayed() {
    }

    public GamesPlayed(Long ownedGameID, Date datePlayed) {
        this.ownedGameID = ownedGameID;
        this.datePlayed = datePlayed;
    }

    public Long getGamePlayedID() {
        return gamePlayedID;
    }

    public void setGamePlayedID(Long gamesPlayedID) {
        this.gamePlayedID = gamesPlayedID;
    }

    public Long getOwnedGameID() {
        return ownedGameID;
    }

    public void setOwnedGameID(Long ownedGameID) {
        this.ownedGameID = ownedGameID;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game Played: game played ID = ").append(gamePlayedID).append(EOL);
        builder.append("owned game ID = ").append(ownedGameID).append(EOL);
        builder.append("date played = ").append(datePlayed).append(EOL);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GamesPlayed gamePlayed = (GamesPlayed) o;
        return(gamePlayedID.equals(gamePlayed.gamePlayedID));
    }
}
