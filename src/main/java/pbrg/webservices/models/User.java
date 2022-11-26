package pbrg.webservices.models;

public class User {

    /**
     * User ID.
     */
    private final int uid;

    /**
     * Username.
     */
    private final String username;

    /**
     * Construct a user with ID and username.
     *
     * @param pUid      user ID
     * @param pUsername username
     */
    public User(final int pUid, final String pUsername) {
        this.uid = pUid;
        this.username = pUsername;
    }

    /**
     * Get user ID.
     *
     * @return user ID
     */
    public int getUid() {
        return this.uid;
    }

    /**
     * Get username.
     *
     * @return username
     */
    public String getUsername() {
        return this.username;
    }
}
