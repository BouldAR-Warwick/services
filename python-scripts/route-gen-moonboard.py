import sys
import json

grade = sys.argv[1]

data = [{"x": 5, "y": 4},
        {"x": 5, "y": 4},
        {"x": 5, "y": 9},
        {"x": 2, "y": 12},
        {"x": 4, "y": 15},
        {"x": 4, "y": 17}]

json_data = json.dumps(data)

print(json_data)