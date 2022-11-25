package pbrg.webservices.models;

public class GymList {
    public String[] getGyms() {
        return gyms;
    }

    private final String[] gyms;

    public GymList (String[] gyms) {
        this.gyms = gyms;
    }
}