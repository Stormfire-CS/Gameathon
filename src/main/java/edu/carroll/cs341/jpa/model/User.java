package edu.carroll.cs341.jpa.model;

import jakarta.persistence.*;

import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;


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

    /** The ID of the family associated with this owner, if any. */
    @ManyToOne
    @JoinColumn(name = "family", nullable = true)
    private Family family;

    /**
     * Creates an empty Login object.
     *
     * <p>This constructor is required by JPA when retrieving login records
     * from the database.</p>
     */
    public User() {
    }

    /**
     * Creates a login with the specified username and raw password.
     *
     * <p>The supplied password is passed to {@link #setRawPassword(String)}
     * for storage.</p>
     *
     * @param username the username for the login
     * @param rawPassword the password supplied by the user
     */
    public User(String username, String rawPassword) {
        this.username = username;
        this.name = username;
        setRawPassword(rawPassword);
    }

    /**
     * Retrieves the login's database identifier.
     *
     * @return the unique login ID
     */
    public Long getId() {
        return userID;
    }

    /**
     * Sets the login's database identifier.
     *
     * @param userID the ID to assign to the login
     */
    public void setId(Long userID) {
        this.userID = userID;
    }

    /**
     * Retrieves the login's username.
     *
     * @return the username associated with this login
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the login's username.
     *
     * @param username the username to assign to this login
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
     * Sets the owner's name.
     *
     * @param name the name to assign to the owner
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the family ID.
     *
     * @return the family ID
     */
    public Family getFamily() {
        return family;
    }

    /**
     * Sets the owner's family ID.
     *
     * @param family the family ID to assign to the owner
     */
    public void setFamily(Family family) {
        this.family = family;
    }

    /**
     * Returns a readable representation of this login.
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final User user = (User) o;
        return username.equals(user.username) && hashedPassword.equals(user.hashedPassword);
    }

    //Never use primary key for hash
    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword);
    }

    public void setRawPassword(String rawPassword) {
        this.hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}
