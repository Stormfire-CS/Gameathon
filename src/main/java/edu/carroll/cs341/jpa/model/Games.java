package edu.carroll.cs341.jpa.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "Games")
public class Games {

    @Id
    @GeneratedValue
    private Long gameID;

    @Column(name = "gameName", nullable = false)
    private String gameName;

    @Column(name = "minPlayers")
    private Integer minPlayers;

    @Column(name = "maxPlayers")
    private Integer maxPlayers;

    public Games() {
    }

    public Games(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Long getGameID() {
        return gameID;
    }

    public void setGameID(Long gameID) {
        this.gameID = gameID;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game: Game_ID = ").append(gameID).append(EOL);
        if (maxPlayers != null) {
            builder.append("Max Number of Players = ").append(maxPlayers).append(EOL);
        }
        if (minPlayers != null) {
            builder.append("Min Number of Players = ").append(minPlayers);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Games game = (Games) o;
        return(gameName.equals(game.gameName));
    }

}
