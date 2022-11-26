package pbrg.webservices.models;

public class Route {
    private final int route_id;
    private final int wall_id;
    private final int creator_user_id;
    private final int difficulty;
    private final String route_content;
    private final String image_file_name;

    public Route(int route_id, int wall_id, int creator_user_id, int difficulty,
        String route_content,
        String image_file_name) {
        this.route_id = route_id;
        this.wall_id = wall_id;
        this.creator_user_id = creator_user_id;
        this.difficulty = difficulty;
        this.route_content = route_content;
        this.image_file_name = image_file_name;
    }

    public int getRoute_id() {
        return route_id;
    }

    public int getWall_id() {
        return wall_id;
    }

    public int getCreator_user_id() {
        return creator_user_id;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getRoute_content() {
        return route_content;
    }

    public String getImage_file_name() {
        return image_file_name;
    }
}
