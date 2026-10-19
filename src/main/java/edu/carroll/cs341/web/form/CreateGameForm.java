package edu.carroll.cs341.web.form;

import jakarta.validation.constraints.*;

/**
 * Represents the form data submitted when a user adds a new game
 * to the game catalog.
 *
 * <p>Stores the game name and the minimum and maximum numbers of
 * players supported by the game. Validation annotations ensure
 * that the submitted values meet the basic requirements before
 * the game creation process continues.</p>
 *
 * <p>The game name must not be blank and must contain no more than
 * 100 characters. Both player counts must be provided and must be
 * at least one.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with developing Javadoc comments
 *   </ul>
 *
 * @author Max Montowski
 */
public class CreateGameForm {

    /** Game name entered by the user; must not be blank or exceed 100 characters. */
    @NotBlank(message = "Game name is required")
    @Size(max = 100, message = "Game name must not exceed 100 characters")
    private String gameName;

    /** Minimum number of players; must be at least one. */
    @Min(value = 1, message = "Minimum number of players must be at least 1")
    private Integer minPlayers;

    /** Maximum number of players; must at least one. */
    @Min(value = 1, message = "Maximum number of players must be at least 1")
    private Integer maxPlayers;

    /**
     * Retrieves the game name entered by the user.
     *
     * @return the submitted game name
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Sets the game name entered by the user.
     *
     * @param gameName the game name to store
     */
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    /**
     * Retrieves the minimum number of players supported by the game.
     *
     * @return the submitted minimum player count
     */
    public Integer getMinPlayers() {
        return minPlayers;
    }

    /**
     * Sets the minimum number of players supported by the game.
     *
     * @param minPlayers the minimum player count to store
     */
    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    /**
     * Retrieves the maximum number of players supported by the game.
     *
     * @return the submitted maximum player count
     */
    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum number of players supported by the game.
     *
     * @param maxPlayers the maximum player count to store
     */
    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Checks that the minimum number of players does not exceed
     * the maximum number of players.
     *
     * @return true if both values are present and the minimum
     *         player count is less than or equal to the maximum;
     *         false otherwise
     */
    @AssertTrue(message = "Minimum number of players cannot exceed maximum number of players")
    public boolean isPlayerCountValid() {
        if (minPlayers == null || maxPlayers == null) {
            return true;
        }

        return minPlayers <= maxPlayers;
    }
}