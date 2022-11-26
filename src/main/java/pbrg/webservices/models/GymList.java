package pbrg.webservices.models;

public class GymList {

    private final String[] gyms;

    public GymList(String[] gyms) {
        this.gyms = gyms;
    }

    public String[] getGyms() {
        return gyms;
    }
}
