from haversine import haversine
import time
import numpy as np
from sklearn.neighbors import KDTree
import json

s = time.time()
doc = {}

with open('C:/Users/sienn/seoul_graphml_samp.json',encoding='utf-8') as f:
    doc = json.load(f)
print(time.time()-s)

def getLatLon(OSMId):
    lat = float(doc['nodes'][str(OSMId)]['lat'])
    lon = float(doc['nodes'][str(OSMId)]['lon'])

    return (lat, lon)

def getOSMId(lat, lon):
    for key, value in doc['nodes'].items():
        if value['lon']==lon and value['lat']==lat:
            return key
    
def calcHeurist(curr,dest):
    return (haversine(curr,dest))

def getNeighb(OSMId, dest_latlon):
    neighbDict = {}
    tempList = []
    result = [x for x in doc['edges'] if x['source']==str(OSMId)]

    for eachEdge in result:
        temp_neighb = {}
        
        neighb_cost = 0
        neighb_id = eachEdge["target"]
        neighb_latlon = getLatLon(neighb_id) 

        neighb_cost = eachEdge["weight"]       
        neighb_h = calcHeurist(neighb_latlon, dest_latlon)

        neighb_weight = eachEdge["cctv"]*0.4+eachEdge["bell"]*0.3+\
                        eachEdge["police"]*0.2+eachEdge["fire"]*0.1

        #neighb_weight = neighb_cost - neighb_cost*neighb_weight

        temp_neighb[neighb_id] = [neighb_latlon, neighb_cost, neighb_h, neighb_weight]

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
        neighb_weight = float(value[3])

    return neighb_id, neighb_h, neighb_cost, neighb_latlon, neighb_weight


def getKNN(pointLoc):
    loc = []
    for key, value in doc['nodes'].items():
        loc.append((value['lat'], value['lon']))

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
    
    while(parent!=source):
        tempDict = {}
        cost = cost + float(paths[str(child)]["cost"])
        parent = paths[str(child)]["parent"]
        parent = tuple(float(x) for x in parent.strip('()').split(','))
        weight = paths[str(child)]["weight"]
        
        tempDict["lat"] = parent[0]
        tempDict["lon"] = parent[1]
        tempDict["weight"] = weight
        
        finalPath.append(tempDict)
        child = parent

        
    return finalPath, cost
