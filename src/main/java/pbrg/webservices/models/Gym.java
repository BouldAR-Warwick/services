package pbrg.webservices.models;

public class Gym {

    /**
     * Gym ID.
     */
    private final int gid;

    /**
     * Gym name.
     */
    private final String gymName;

    /**
     * Construct a gym with ID and name.
     *
     * @param pGid     gym ID
     * @param pGymName gym name
     */
    public Gym(final int pGid, final String pGymName) {
        this.gid = pGid;
        this.gymName = pGymName;
    }

    /**
     * Get gym ID.
     *
     * @return gym ID
     */
    public int getGid() {
        return gid;
    }

    /**
     * Get gym name.
     *
     * @return gym name
     */
    public String getGymName() {
        return gymName;
    }
}
