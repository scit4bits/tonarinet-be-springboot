
import json
import csv
import re
from math import radians, sin, cos, sqrt, atan2

try:
    from shapely.geometry import shape
except ImportError:
    print("Shapely library not found. Please install it using 'pip install shapely'")
    exit()

def haversine_distance(lon1, lat1, lon2, lat2):
    R = 6371000  # Radius of Earth in meters
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    distance = R * c
    return distance

def main():
    with open(r'gadm41_JPN_2.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    processed_features = []
    
    ratio=0.5
    for feature in data['features']:
        properties = feature['properties']
        
        # Filter for Tokyo only
        if properties.get('NAME_1') != 'Tokyo':
            continue
            
        geom = shape(feature['geometry'])
        centroid = geom.centroid
        min_lon, min_lat, max_lon, max_lat = geom.bounds
        radius1 = haversine_distance(centroid.x, centroid.y, min_lon, min_lat)
        radius2 = haversine_distance(centroid.x, centroid.y, max_lon, max_lat)
        max_radius = max(radius1, radius2)
        
        processed_feature = {
            'properties': {
                'NAME_1': properties.get('NL_NAME_1'),
                'NAME_2': properties.get('NL_NAME_2')
            },
            'geometry': {
                'latitude': centroid.y,
                'longitude': centroid.x,
                'radius': int(max_radius * ratio)
            }
        }
        processed_features.append(processed_feature)
    
    data['features'] = processed_features

    with open(r'japan_regions.csv', 'w', newline='', encoding='utf-8') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['id', 'country_code', 'category1','category2','category3', 'category4', 'longitude', 'latitude', 'radius'])
        
        for i, feature in enumerate(data['features'], 1):
            properties = feature['properties']
            geometry = feature['geometry']
            
            csv_writer.writerow([
                '',
                'jpn',
                properties['NAME_1'],
                properties['NAME_1'],
                properties['NAME_1'],
                properties['NAME_2'],
                geometry['longitude'],
                geometry['latitude'],
                geometry['radius']
            ])

    print("Successfully processed the data and created 'japan_regions.csv'")

if __name__ == '__main__':
    main()
