# Safest and Shortest Path Finding with Tmap API
Path finding algorithm to derive safety route in Korea on Tmap.

## Background
Even today, when security is strengthened, violent crimes targeting especially women are maximizing anxiety at night. Considering the occurence of these crimes are being more and more frequent, we are working on a project to ensure a safe way home.

## The Project
Project demonstration video link: https://youtu.be/sxv-lOkBG6I, https://youtu.be/NBlI5N8AKLE

The project consists of the components below.

## Flow Chart
![image](https://user-images.githubusercontent.com/89958453/170620194-37fb6dcf-24d4-4e27-8b58-3aeb6cb7837e.png)

## Tech Stack
<div align=center> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> 
  <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white">                     <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">                       <img src="https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">             </div>                                          

### 1. data
- parsed from OpenStreetMap https://www.openstreetmap.org/ as .osm file
- converted to lighter xml (graphml) file 
 
| file name | explanation | 
| :---      | :---      |
| ewha.osm   | OpenStreetMap data around Ewha and Sinchon station |
| ewha_graphml  | converted graphs from osm file |

- contain street data around Ewha and Sinchon stations. (would be expanded to larger region later.)
- the image of graphml file around Ewha and Sinchon stations.
- ![image](https://user-images.githubusercontent.com/58411517/145157397-e077234a-2635-4486-a9ab-0c9aa5c18c11.png)


### 2. algorithm
- applied Dijkstra and A* algorithm
- use public data as safety weights (under development)
- .py files in demo folder are simple codes using sample graphs for apprehending the concept.

| file name | function | 
| :---         | :---      |
| main.py   | get coordinates of source and destination; initiate A* algorithm |
| astar.py  | define A* algorithm |
| manXML.py  | define functions in managing graphxml file: getting OSM ID, neighbors, heuristic distances |

- path finding result from Ewha station to Sinchon station on Google Map.
- ![image](https://user-images.githubusercontent.com/58411517/145157293-97e49835-f434-44f5-95f8-5456dde0d458.png)


### 3. server
- contain public data: coordinates of CCTVs, police office, firestation, alarmbell etc.
- data management
- HTTP Connection between Android Studio and Server

### 4. application
- receive user's input for starting and destination points. (under development)
- based on the user's input, the safest(under development) or fastest route are shown.
- path finding result from Ewha station to Yonsei villa on application with tmap api.
- ![image](https://user-images.githubusercontent.com/58411517/172615764-b75d698f-5611-4b31-8554-42876d6c1ebb.png)

