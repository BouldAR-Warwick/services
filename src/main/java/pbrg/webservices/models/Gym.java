package pbrg.webservices.models;

public class Gym {
    private final int gid;
    private final String gym_name;

    public Gym (int gid, String gym_name) {
        this.gid = gid;
        this.gym_name = gym_name;
    }

    public int getGid() {
        return gid;
    }

    public String get_gym_name() {
        return gym_name;
    }
}
