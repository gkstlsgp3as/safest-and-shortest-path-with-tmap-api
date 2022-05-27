import manXML as mx
import heapq
import time

def aStar(source, dest, method=1):
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

    heapq.heappush(open_list, (h_source, 0, source_id, 0)) # push the start node

    #print(open_list)
    s = time.time() # checking running time
    while(len(open_list)>0):

        (curr_h, curr_g, curr_id, curr_weight) = heapq.heappop(open_list) # pop the start node

        closed_list[curr_id] = ""

        if(curr_id==dest_id): # reached the end
            print("We found the route!")
            break 

        #print("현재노드: ",mx.getLatLon(curr_id))
        neighb = mx.getNeighb(curr_id, dest) # neighbours from start to destination
        neighb_curr = neighb[curr_id]
        for each_neighb in neighb_curr:

            neighb_id, neighb_h, neighb_cost, neighb_latlon, neighb_weight = mx.getNeighbInfo(each_neighb)

            if (method):
                neighb_g = curr_g + neighb_cost - neighb_cost*neighb_weight
            else:
                neighb_g = curr_g + neighb_cost

            #print(neighb_latlon, neighb_weight, neighb_cost, neighb_g)
            if(neighb_id in closed_list):
                continue

            else: # not visited neighbors
                #g_values[neighb_id] = neighb_g
                neighb_f = neighb_g + neighb_h
                heapq.heappush(open_list, (neighb_f, neighb_g, neighb_id, neighb_weight))
                #print(neighb_cost, neighb_weight)
    
            
            path[str(neighb_latlon)] = {"parent":str(mx.getLatLon(curr_id)), "cost":neighb_cost,
                                        "weight": neighb_weight}

    print("Time taken to find path(in second): "+str(time.time()-s))
    return path
