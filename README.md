# Safest and Shortest Path Finding with Tmap API
Path finding algorithm to derive safety route in Korea on Tmap.

## The Project
The project consists of the components below:Even today, when security is strengthened, violent crimes and women's target crimes are still increasing anxiety at night. Considering that these crimes occur frequently, we are working on a project to ensure a safe way home


### 1. data
- parsed from OpenStreetMap https://www.openstreetmap.org/ as .osm file
- converted to lighter xml (graphml) file  
- contain street data around Ewha and Sinchon stations. (would be expanded to larger region later.)

### 2. algorithm
- applied Dijkstra and A* algorithm
- use public data as safety weights (under development)
- .py files in demo folder are simple codes using sample graphs for apprehending the concept.

### 3. server
- contain public data: coordinates of CCTVs, street lights, and security lights (under development)

### 4. application
- receive user's input for starting and destination points. (under development)
- based on the user's input, the safest(under development) or fastest route are shown.
