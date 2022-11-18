package pbrg.webservices.servlets.models;

public class LoggedInUser {
    private int uid;
    private String username;

    public LoggedInUser (int uid, String username) {
        this.uid = uid;
        this.username = username;
    }
}