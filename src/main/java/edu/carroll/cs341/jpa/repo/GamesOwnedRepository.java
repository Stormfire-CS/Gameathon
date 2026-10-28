package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides database operations for {@link GameOwned} entities.
 *
 * <p>This repository extends {@link JpaRepository} to provide standard
 * persistence operations for {@link GameOwned} objects, using {@link Long}
 * as the type of the entity's primary key.</p>
 *
 * <p>It also defines query methods for retrieving game ownership records
 * by user and by game ownership record ID, as well as retrieving records
 * associated with a list of users.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *     <li>ChatGPT assisted with Javadoc comments for this file.
 *     All comments were reviewed by the author. No code was generated.</li>
 * </ul>
 *
 * @author Max Montowski
 */

public interface GamesOwnedRepository extends JpaRepository<GameOwned, Long> {

    /**
     * Finds all game ownership records associated with the specified user.
     *
     * @param user the user whose game ownership records are being retrieved
     * @return a list of game ownership records for the specified user;
     * an empty list if no matching records are found
     */
    List<GameOwned> findByUser(User user);

    /**
     * Finds a game ownership record by its unique database identifier.
     *
     * @param gameOwnedID the ID of the game ownership record to retrieve
     * @return the matching game ownership record, or null if no record is found
     */
    GameOwned findByGameOwnedID(Long gameOwnedID);

    /**
     * Finds all game ownership records associated with any of the specified users.
     *
     * @param users the list of users whose game ownership records are being retrieved
     * @return a list of matching game ownership records;
     * an empty list if no matching records are found
     */
    List<GameOwned> findByUserIn(List<User> users);
}
