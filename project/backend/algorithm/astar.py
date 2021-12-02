import manXML as mx
import heapq
import time

def aStar(source, dest):
    open_list = [] # open list; min heap
    # element of open_list:
    # [0]: F value
    # [1]: G value
    # [2]: current node

    path = {}
    closed_list = {}
    g_values = {}
    
    source_id = mx.getOSMId(source[0], source[1])
    dest_id = mx.getOSMId(dest[0], dest[1])
    g_values[source_id] = 0
    h_source = mx.calcHeurist(source, dest)

    heapq.heappush(open_list, (h_source, 0, source_id)) # push the start node
    #print(open_list)
    s = time.time() # checking running time
    while(len(open_list)>0):
        (curr_h, curr_g, curr_id) = heapq.heappop(open_list) # pop the start node
        closed_list[curr_id] = ""

        if(curr_id==dest_id): # reached the end
            print("We found the route!")
            break 

        neighb = mx.getNeighb(curr_id, dest) # neighbours from start to destination
        neighb_curr = neighb[curr_id]
        for each_neighb in neighb_curr:
            neighb_id, neighb_h, neighb_cost, neighb_latlon = mx.getNeighbInfo(each_neighb)
            neighb_g = curr_g + neighb_cost # + weight for safety

            if(neighb_id in closed_list):
                continue

            else: # not visited neighbors
                neighb_f = neighb_g + neighb_h
                heapq.heappush(open_list, (neighb_f, neighb_g, neighb_id))
        
            path[str(neighb_latlon)] = {"parent":str(mx.getLatLon(curr_id)), "cost":neighb_cost}

    print("Time taken to find path(in second): "+str(time.time()-s))
    return path
