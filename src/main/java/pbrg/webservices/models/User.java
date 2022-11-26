package pbrg.webservices.models;

public class User {

    private final int uid;
    private final String username;

    public User(int uid, String username) {
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
