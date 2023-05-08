"""Plot a route on a wall image, creating a route image."""
import json
import os
import sys
from typing import List

from PIL import Image, ImageDraw

# radius of the hold circle
HOLD_RADIUS = 25


def plot_holds(wall_image_of_route: Image, route_image_in: Image, holds: List[dict]):
    """Plot the holds on the route image."""
    # draw the holds on the route image
    draw = ImageDraw.Draw(route_image_in)
    for hold in holds:
        x_norm, y_norm = hold.values()

        # scale coordinates to image size
        x_coordinate = round(x_norm * wall_image_of_route.size[0])
        y_coordinate = round(y_norm * wall_image_of_route.size[1])

        # highlight the hold - make the circle border thicker and red, no fill
        draw.ellipse(
            (
                x_coordinate - HOLD_RADIUS,
                y_coordinate - HOLD_RADIUS,
                x_coordinate + HOLD_RADIUS,
                y_coordinate + HOLD_RADIUS,
            ),
            fill=None,
            outline=(255, 0, 0),
            width=5,
        )


def get_wall_image(
    wall_image_directory_of_route: str,
    wall_image_filename_of_route: str,
) -> Image:
    """Open the wall image file. Exits if the file does not exist."""
    # parse the wall image path
    wall_image_path = os.path.join(
        wall_image_directory_of_route, wall_image_filename_of_route
    )

    # ensure file exists
    if not os.path.isfile(wall_image_path):
        print("Wall image file does not exist")
        sys.exit(1)

    # open the wall image
    return Image.open(wall_image_path)


def create_blank_route_image(wall_image_of_route: Image) -> Image:
    """Create the blank route image from the wall image."""
    return wall_image_of_route.copy()


def create_route_image_filepath(
    route_id_in: str, wall_image_filename_of_route: str, route_image_directory_in: str
) -> str:
    """Create the route image path."""
    route_image_filename = "r" + route_id_in + "-" + wall_image_filename_of_route
    return os.path.join(route_image_directory_in, route_image_filename)


if __name__ == "__main__":
    # ensure we have all 5 arguments
    if len(sys.argv) != 6:
        print(
            "Usage: python3 <plot-holds-script>.py <wall-image-filename> "
            "<wall-image-directory> <route-image-directory> <route-id> "
            "<holds-json>"
        )
        sys.exit(1)

    # parse the arguments
    assert len(sys.argv[1:]) == 5
    (
        wall_image_filename,
        wall_image_directory,
        route_image_directory,
        route_id,
        holds_json,
    ) = tuple(sys.argv[1:])

    # get the wall image, create the route image
    wall_image: Image = get_wall_image(wall_image_directory, wall_image_filename)
    route_image: Image = create_blank_route_image(wall_image)

    # create the route image path
    route_image_path: str = create_route_image_filepath(
        route_id, wall_image_filename, route_image_directory
    )

    # plot the holds on the wall image
    plot_holds(wall_image, route_image, json.loads(holds_json))

    # save the route image
    route_image.save(route_image_path)
