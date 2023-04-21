package pbrg.webservices.models;

public class Gym {

    /** Gym ID. */
    private final int gid;

    /** Gym name. */
    private final String gymName;

    /** Gym location. */
    private final String gymLocation;

    /**
     * Construct a gym with ID and name.
     *
     * @param pGid     gym ID
     * @param pGymName gym name
     * @param pGymLocation gym location
     */
    public Gym(
        final int pGid,
        final String pGymName,
        final String pGymLocation
    ) {
        this.gid = pGid;
        this.gymName = pGymName;
        this.gymLocation = pGymLocation;
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

    /**
     * Get gym location.
     *
     * @return gym location
     */
    public String getGymLocation() {
        return gymLocation;
    }
}
