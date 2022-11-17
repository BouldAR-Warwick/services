public class LoggedInUser {
    private int uid;
    private String sessionID;
    private String username;

    public LoggedInUser (int uid, String sessionID, String username) {
        this.uid = uid;
        this.sessionID = sessionID;
        this.username = username;
    }
}