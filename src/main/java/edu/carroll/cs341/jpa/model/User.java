package edu.carroll.cs341.jpa.model;

import jakarta.persistence.*;

import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Represents a user account in the application.
 *
 * <p>A user has a unique username, a stored BCrypt password hash, an owner's
 * name, and an optional association with a family. This class is also a JPA
 * entity used to persist user information in the database.</p>
 *
 * @author Max Montowski
 * ChatGPT assisted with documentation; reviewed by author. No code generated.
 */
@Entity
@Table(name = "users")
public class User {

    /** The serialization version identifier. */
    private static final long serialVersionUID = 1L;

    /** The unique database identifier for this login record. */
    @Id
    @GeneratedValue
    private Long userID;

    /** The unique username associated with this login. */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** The stored password value associated with this login. */
    @Column(name = "password", nullable = false)
    private String hashedPassword;

    /** The name of the owner. */
    @Column(name = "name", nullable = false, unique = false)
    private String name;

    /** The family associated with this user, if any. */
    @ManyToOne
    @JoinColumn(name = "family", nullable = true)
    private Family family;

    /**
     * Creates an empty User object.
     *
     * <p>This constructor is required by JPA when retrieving user records
     * from the database.</p>
     */
    public User() {
    }

    /**
     * Creates a user with the specified username and raw password.
     *
     * <p>The supplied password is passed to {@link #setRawPassword(String)}
     * for storage.</p>
     *
     * @param username the username for the user
     * @param rawPassword the password supplied by the user
     */
    public User(String username, String rawPassword) {
        this.username = username;
        this.name = username;
        setRawPassword(rawPassword);
    }

    /**
     * Retrieves the user's database identifier.
     *
     * @return the unique user ID
     */
    public Long getId() {
        return userID;
    }

    /**
     * Sets the user's database identifier.
     *
     * @param userID the ID to assign to the user
     */
    public void setId(Long userID) {
        this.userID = userID;
    }

    /**
     * Retrieves the user's username.
     *
     * @return the username associated with this user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username the username to assign to this user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the stored password value.
     *
     * @return the stored password value
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Sets the stored password value directly.
     *
     * @param hashedPassword the password value to store
     */
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /** The platform-specific line separator used when formatting output. */
    private static final String EOL = System.lineSeparator();

    /** The tab character used to indent formatted output. */
    private static final String TAB = "\t";

    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name the name to assign to the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the family.
     *
     * @return the family
     */
    public Family getFamily() {
        return family;
    }

    /**
     * Sets the user's family.
     *
     * @param family the family to assign to the user
     */
    public void setFamily(Family family) {
        this.family = family;
    }

    /**
     * Returns a readable representation of this user.
     *
     * <p>The representation includes the username but masks the stored
     * password value.</p>
     *
     * @return a formatted string containing the login information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Login @ ").append(super.toString()).append("[").append(EOL);
        builder.append(TAB).append("username=").append(username).append(EOL);
        builder.append(TAB).append("hashedPassword=").append("****").append(EOL);
        builder.append("]").append(EOL);
        return builder.toString();
    }

    /**
     * Compares this user with another object for equality.
     *
     * <p>Two users are considered equal when their usernames and stored
     * password hashes match.</p>
     *
     * @param o the object to compare with this user
     * @return true if the objects represent equal users; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final User user = (User) o;
        return username.equals(user.username) && hashedPassword.equals(user.hashedPassword);
    }

    /**
     * Calculates a hash code using the username and stored password hash.
     *
     * @return the hash code for this user
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword);
    }

    /**
     * Hashes a raw password with BCrypt and stores the resulting hash.
     *
     * @param rawPassword the plain-text password to hash and store
     */
    public void setRawPassword(String rawPassword) {
        this.hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}
