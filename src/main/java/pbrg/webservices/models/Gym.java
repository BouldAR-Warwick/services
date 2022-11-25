package pbrg.webservices.models;

public class Gym {
    private final int gid;
    private final String gymname;

    public Gym (int gid, String gymname) {
        this.gid = gid;
        this.gymname = gymname;
    }

    public int getGid() {
        return gid;
    }

    public String getGymname() {
        return gymname;
    }
}
