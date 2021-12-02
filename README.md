# safest-and-shortest-path-with-tmap-api
Path finding algorithm to derive safety route in Korea on Tmap.

## The Project
The project consists of the components below:

*data
-parsed from OpenStreetMap https://www.openstreetmap.org/ as .osm file
-converted to lighter xml (graphml) file  
-contain street data around Ewha and Sinchon stations. (would be expanded to larger region later.)

*algorithm
-applied Dijkstra and A* algorithm
-use public data as safety weights (under development)
-.py files in demo folder are simple codes using sample graphs for apprehending the concept.

*server
-contain public data: coordinates of CCTVs, street lights, and security lights (under development)

*application
-receive user's input for starting and destination points. (under development)
-based on the user's input, the safest(under development) or fastest route are shown.
