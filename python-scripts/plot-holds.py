import json
import sys
from PIL import Image, ImageDraw
import os


def plot_holds(
        wall_image_filename: str, wall_image_directory: str,
        route_image_directory: str, route_id: int, holds_json: str
) -> int:
    """
    Plots the holds on the wall image and saves the route image.
    :param wall_image_filename: The filename of the wall image.
    :param wall_image_directory: The directory of the wall image.
    :param route_image_directory: route image directory.
    :param route_id: The route id.
    :param holds_json: The holds in JSON format.
    :return: 0 if successful, -1 if not.
    """
    # Load the wall image
    wall_image_path = os.path.join(wall_image_directory, wall_image_filename)

    # opening the wall image

    # print("Opening wall image file: " + wall_image_path)
    # may raise PIL.UnidentifiedImageError
    wall_image = Image.open(wall_image_path)

    # Convert holds_json string to Python list of tuples
    holds = json.loads(holds_json)

    # draw the holds on
    route_image_filename = "r" + str(route_id) + "-" + wall_image_filename
    route_image_path = os.path.join(route_image_directory, route_image_filename)
    route_image = wall_image.copy()

    # Draw the holds on the route image
    draw = ImageDraw.Draw(route_image)
    radius = 25
    for hold in holds:
        x_norm, y_norm = hold.values()

        # scale coordinates to image size
        x = round(x_norm * wall_image.size[0])
        y = round(y_norm * wall_image.size[1])

        # highlight the hold - make the circle border thicker and red, no fill
        draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=None, outline=(255, 0, 0), width=5)

    # Save the route image
    route_image.save(route_image_path)

    return 0


if __name__ == "__main__":
    # command line arguments order: \
    # wall_image_filename, wall_image_directory, route_image_directory, route_id, holds_json

    # Plot the holds on the wall image
    plot_holds(*sys.argv[1:])
