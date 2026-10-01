package edu.carroll.cs341.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "GamePlayedPlayers")
public class GamePlayedPlayers {

    @Id
    @GeneratedValue
    private Long gamePlayedPlayerID;

    @Column(name = "playerID", nullable = false, unique = true)
    private Long playerID;

    @Column(name = "score")
    private Integer score;

    @Column(name = "didWin")
    private Boolean didWin;

    public GamePlayedPlayers() {
    }

    public GamePlayedPlayers(Long playerID) {
        this.playerID = playerID;
    }

    public Long getGamePlayedPlayerID() {
        return gamePlayedPlayerID;
    }

    public void setGamePlayedPlayerID(Long gamePlayedPlayerID) {
        this.gamePlayedPlayerID = gamePlayedPlayerID;
    }

    public Long getPlayerID() {
        return playerID;
    }

    public void setPlayerID(Long playerID) {
        this.playerID = playerID;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getDidWin() {
        return didWin;
    }

    public void setDidWin(Boolean didWin) {
        this.didWin = didWin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game played player: game played player ID = ").append(gamePlayedPlayerID).append(EOL);
        builder.append("player ID = ").append(playerID).append(EOL);
        if (score != null) {
            builder.append("score = ").append(score).append(EOL);
        }
        if (didWin != null) {
            builder.append("Did player win boolean = ").append(didWin);
        }
        return builder.toString();
    }

}
