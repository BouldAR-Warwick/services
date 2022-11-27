package pbrg.webservices.models;

public class Route {

    /**
     * Route ID.
     */
    private final int routeID;

    /**
     * Route difficulty.
     */
    private final int difficulty;

    /**
     * Route name.
     */
    private final String routeName;

    /**
     * Construct a route using its id, difficulty, and name
     *
     * @param pRouteID       route ID
     * @param pDifficulty    difficulty
     * @param pRouteName     route name
     */
    public Route(
        final int pRouteID,
        final int pDifficulty,
        final String pRouteName
    ) {
        this.routeID = pRouteID;
        this.difficulty = pDifficulty;
        this.routeName = pRouteName;
    }

    /**
     * Get route ID.
     *
     * @return route ID
     */
    public final int getRouteId() {
        return routeID;
    }

    /**
     * Get route difficulty.
     *
     * @return difficulty
     */
    public final int getDifficulty() {
        return difficulty;
    }

    /**
     * Get route name.
     *
     * @return route name
     */
    public final String getRouteName() {
        return routeName;
    }
}
