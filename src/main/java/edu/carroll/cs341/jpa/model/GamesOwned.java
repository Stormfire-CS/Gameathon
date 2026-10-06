package edu.carroll.cs341.jpa.model;

import jakarta.persistence.*;

import static org.aspectj.util.LangUtil.EOL;

@Entity
@Table(name = "GamesOwned")
public class GamesOwned {

    @GeneratedValue
    @Id
    private Long gameOwnedID;

    @ManyToOne
    @JoinColumn(name = "gameID", nullable = false)
    private Games game;

    @Column(name = "ownerID", nullable = false)
    private Long ownerID;

    @Column(name = "yearProduced")
    private Integer yearProduced;

    public GamesOwned() {
    }

    public GamesOwned(Games game, Long ownerID) {
        this.game = game;
        this.ownerID = ownerID;
    }

    public Long getGameOwnedID() {
        return gameOwnedID;
    }

    public void setGameOwnedID(Long gameOwnedID) {
        this.gameOwnedID = gameOwnedID;
    }

    public Games getGame() {
        return game;
    }

    public void setGame(Games game) {
        this.game = game;
    }

    public Long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(Long ownerID) {
        this.ownerID = ownerID;
    }

    public Integer getYearProduced() {
        return yearProduced;
    }

    public void setYearProduced(Integer yearProduced) {
        this.yearProduced = yearProduced;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game Owned ID = ").append(gameOwnedID).append(EOL);
        builder.append("Game ID = ").append(game.getGameID()).append(EOL);
        builder.append("Game Name = ").append(game.getGameName()).append(EOL);
        builder.append("Owner ID = ").append(ownerID);
        if (yearProduced != null) {
            builder.append("Publish Year = ").append(yearProduced).append(EOL);
        }
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
