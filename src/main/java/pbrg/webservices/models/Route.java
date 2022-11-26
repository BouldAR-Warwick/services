package pbrg.webservices.models;

public class Route {

    /**
     * Route ID.
     */
    private final int routeId;

    /**
     * Wall ID.
     */
    private final int wallId;

    /**
     * Creator user ID.
     */
    private final int creatorUserId;

    /**
     * Route difficulty.
     */
    private final int difficulty;

    /**
     * Route content.
     */
    private final String routeContent;

    /**
     * Route image file name.
     */
    private final String imageFileName;

    /**
     * Construct a route with ID, wall ID, creator user ID, \ difficulty, content, and image file
     * name.
     *
     * @param pRouteId       route ID
     * @param pWallId        wall ID
     * @param pCreatorUserId creator user ID
     * @param pDifficulty    difficulty
     * @param pRouteContent  content
     * @param pImageFileName image file name
     */
    public Route(
        final int pRouteId,
        final int pWallId,
        final int pCreatorUserId,
        final int pDifficulty,
        final String pRouteContent,
        final String pImageFileName
    ) {
        this.routeId = pRouteId;
        this.wallId = pWallId;
        this.creatorUserId = pCreatorUserId;
        this.difficulty = pDifficulty;
        this.routeContent = pRouteContent;
        this.imageFileName = pImageFileName;
    }

    /**
     * Get route ID.
     *
     * @return route ID
     */
    public final int getRouteId() {
        return routeId;
    }

    /**
     * Get wall ID.
     *
     * @return wall ID
     */
    public final int getWallId() {
        return wallId;
    }

    /**
     * Get route creator's user ID.
     *
     * @return creator user ID
     */
    public final int getCreatorUserId() {
        return creatorUserId;
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
     * Get route content.
     *
     * @return content
     */
    public final String getRouteContent() {
        return routeContent;
    }

    /**
     * Get route image file name.
     *
     * @return image file name
     */
    public final String getImageFileName() {
        return imageFileName;
    }
}
