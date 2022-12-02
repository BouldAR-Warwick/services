package pbrg.webservices.models;

public class GymList {

    /**
     * list of gym names.
     */
    private final String[] gyms;

    /**
     * Construct a list of gym names.
     *
     * @param pGyms list of gym names
     */
    public GymList(final String[] pGyms) {
        this.gyms = pGyms;
    }

    /**
     * Get array of gym names.
     *
     * @return array of gym names
     */
    public String[] getGyms() {
        return gyms;
    }
}
