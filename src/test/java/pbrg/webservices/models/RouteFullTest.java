package pbrg.webservices.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RouteFullTest {
    @Test
    void testGetters() {
        int routeId = 1;
        int wallId = 2;
        int creatorUserId = 3;
        int difficulty = 4;
        String routeContent = "content";
        String imageFileName = "image.jpg";

        RouteFull routeFull = new RouteFull(
            routeId, wallId, creatorUserId, difficulty,
            routeContent, imageFileName
        );

        assertEquals(routeId, routeFull.getRouteId());
        assertEquals(wallId, routeFull.getWallId());
        assertEquals(creatorUserId, routeFull.getCreatorUserId());
        assertEquals(difficulty, routeFull.getDifficulty());
        assertEquals(routeContent, routeFull.getRouteContent());
        assertEquals(imageFileName, routeFull.getImageFileName());
    }
}