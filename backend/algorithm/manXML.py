import xmltodict
from haversine import haversine
import time
import numpy as np
from sklearn.neighbors import KDTree

s = time.time()
doc = {}

with open('C:/Users/sienn/OneDrive/문서/대학/[2021-2] 캡스톤프로젝트/data/ewha_graphml',encoding='utf-8') as fd:
    doc = xmltodict.parse(fd.read())
print(time.time()-s)

def getLatLon(OSMId):
    lat, lon = 0, 0
    nodes = doc['graphml']['graph']['node']

    coords = [x for x in nodes if x["@id"]==str(OSMId)]
    lat = float(coords[0]["data"][0]["#text"])
    lon = float(coords[0]["data"][1]["#text"])

    return (lat, lon)

def getOSMId(lat, lon):
    OSMId = 0
    nodes = doc['graphml']['graph']['node']

    result = [x for x in nodes if x["data"][0]["#text"]==str(lat) and x["data"][1]["#text"]==str(lon)]
    OSMId = result[0]["@id"]

    return OSMId
    
def calcHeurist(curr,dest):
    return (haversine(curr,dest))

def getNeighb(OSMId, dest_latlon):
    neighbDict = {}
    tempList = []
    edges = doc['graphml']['graph']['edge']

    result = [x for x in edges if x["@source"]==str(OSMId)]

    for eachEdge in range(len(result)):
        temp_neighb = {}
        
        neighb_cost = 0
        neighb_id = result[eachEdge]["@target"]
        neighb_latlon = getLatLon(neighb_id) 
        
        dataPoints = result[eachEdge]["data"]

        neighb_cost = [d for d in dataPoints if d["@key"]=="d10"][0]["#text"]        
        neighb_h = calcHeurist(neighb_latlon, dest_latlon)
        
        temp_neighb[neighb_id] = [neighb_latlon, neighb_cost, neighb_h]
        tempList.append(temp_neighb)
            
    neighbDict[OSMId] = tempList
    return (neighbDict)

def getNeighbInfo(neighbDict):
    neighb_id = 0
    neighb_h = 0
    neighb_cost = 0
    for key, value in neighbDict.items():
        
        neighb_id = key
        neighb_h = float(value[2])
        neighb_cost = float(value[1])/1000
        neighb_latlon = value[0]
        
    return neighb_id, neighb_h, neighb_cost, neighb_latlon

def getKNN(pointLoc):
    nodes = doc["graphml"]["graph"]["node"]
    loc = []

    loc = list(map(lambda x: (x["data"][0]["#text"], x["data"][1]["#text"]),nodes))

    loc_arr = np.asarray(loc, dtype=np.float32)
    point = np.asarray(pointLoc, dtype=np.float32)

    tree = KDTree(loc_arr, leaf_size=2)
    dist, ind = tree.query(point.reshape(1,-1), k=3) 
    
    nearestNeighbLoc = (float(loc[ind[0][0]][0]), float(loc[ind[0][0]][1]))
    
    return nearestNeighbLoc
    
def getResponsePathDict(paths, source, dest):
    finalPath = []
    child = dest
    parent = ()
    cost = 0
    #print(paths)
    while(parent!=source):
        tempDict = {}
        cost = cost + float(paths[str(child)]["cost"])
        parent = paths[str(child)]["parent"]
        parent = tuple(float(x) for x in parent.strip('()').split(','))
        
        tempDict["lat"] = parent[0]
        tempDict["lon"] = parent[1]
        
        finalPath.append(tempDict)
        child = parent
        
    return finalPath, cost
