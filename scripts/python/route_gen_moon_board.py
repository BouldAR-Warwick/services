"""Generate a random MoonBoard route for a given grade."""
from typing import Any, Dict, List
import random
import json
import sys
import os


def parse_json_file(filepath: str) -> Dict[str, Any]:
    """Parse a JSON file and return a dictionary of the data."""
    with open(filepath, 'r', encoding='utf-8') as file:
        file_as_json = json.load(file)
    return file_as_json


# static 2016 MoonBoard normalised-coordinate route
static_warwick_route = json.dumps([
    {"x": 0.21353383458646616, "y": 0.844574780058651},
    {"x": 0.29172932330827067, "y": 0.793743890518084},
    {"x": 0.29172932330827067, "y": 0.6412512218963832},
    {"x": 0.5263157894736842, "y": 0.5395894428152492},
    {"x": 0.3699248120300752, "y": 0.4887585532746823},
    {"x": 0.7609022556390977, "y": 0.3362658846529814},
    {"x": 0.6045112781954888, "y": 0.13294232649071358}
])

# storing cached routes for the Warwick MoonBoard
WARWICK_CACHED_ROUTES_FILENAME = 'warwick-routes-cached-v4-9.json'

if __name__ == '__main__':
    # ensure we have both arguments
    if len(sys.argv) != 2:
        print("Usage: python3 <route-gen-script>.py <grade>")
        sys.exit(1)

    # parse the folder
    script_folder = os.path.dirname(os.path.abspath(sys.argv[0]))

    # parse the grade from the first argument
    GRADE_KEY = sys.argv[1]

    # ensure grade is numeric
    if not GRADE_KEY.isnumeric():
        print("Grade must be numeric")
        sys.exit(1)
    GRADE: int = int(GRADE_KEY)

    # ensure grade is in range V1-V14
    if GRADE < 1 or GRADE > 14:
        print("Grade must be between 1 and 14")
        sys.exit(1)

    # if grade is 1-3 or 10-14, use the static route
    if GRADE <= 3 or GRADE >= 10:
        print(static_warwick_route)
        sys.exit(0)

    # relative path to the cache
    cache_path = os.path.join(script_folder, WARWICK_CACHED_ROUTES_FILENAME)

    # ensure file exists
    if not os.path.isfile(cache_path):
        print("Cache file does not exist")
        sys.exit(1)

    # load Warwick wall route cache
    routes: Dict[str, List[List[dict]]] = parse_json_file(cache_path)
    assert len(routes) != 0

    # pick a route of grade from the cache
    graded_routes: List[List[dict]] = routes[GRADE_KEY]
    assert len(graded_routes) != 0

    # pick a random route from the graded_routes
    random_route: List[dict] = random.choice(graded_routes)

    print(random_route)
