package pbrg.webservices.models;

public class LoggedInUser {
    private int uid;
    private String username;

    public LoggedInUser (int uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public int get_uid() {
        return this.uid;
    }

    public String get_username() {
        return this.username;
    }
}