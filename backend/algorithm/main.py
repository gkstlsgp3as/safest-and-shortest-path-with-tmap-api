import manXML as mx
import astar as ast
import json
#import server
#import client
#from threading import Thread, Lock
#import conSftp
import requests
import sys

def main():
    #loc = sys.argv[1]
    #loc = conSftp.getData('loc.txt')
    #inputSourceLoc = (sys.argv[1], sys.argv[2])
    #inputDestLoc = (sys.argv[3], sys.argv[4])
    #inputSourceLoc = (loc[0], loc[1])#(37.556729, 126.945855) # Ewha St.
    inputSourceLoc =  (37.5568, 126.9455)#(37.5568476, 126.9462521)  #(37.547640,126.941851) (37.546300, 126.940429
    #inputSourceLoc = (37.556997, 126.946378)
    #inputDestLoc = (loc[2], loc[3])#(37.555273, 126.936836) # Sinchon St.
    inputDestLoc =  (37.5583807, 126.9411343)#(37.547227,126.941475) (37.547673, 126.945862)
    
    mappedSourceLoc = mx.getKNN(inputSourceLoc) # mapped location
    mappedDestLoc = mx.getKNN(inputDestLoc)
    
    print(mappedSourceLoc, mappedDestLoc)

    #socket_thread = Thread(target=server.run_socket)
    #socket_thread.start()

    path = ast.aStar(mappedSourceLoc, mappedDestLoc, 0) # astar algorithm
    #print(path)
    fastestPath, cost = mx.getResponsePathDict(path, mappedSourceLoc, mappedDestLoc)
    
    print("Cost of the path(km): "+str(cost))

    path = []
    for node in fastestPath:
        path.append(node['lat'])
        path.append(node['lon'])
        path.append(node['weight'])

    path_str = "fastest: "+(", ".join(str(_) for _ in path))
    path_str = path_str+", "+str(round(cost*1000,4))
    print(path_str)

    path = ast.aStar(mappedSourceLoc, mappedDestLoc, 1) # astar algorithm
    #print(path)
    safestPath, cost = mx.getResponsePathDict(path, mappedSourceLoc, mappedDestLoc)
    
    print("Cost of the path(km): "+str(cost))

    path = []
    for node in safestPath:
        path.append(node['lat'])
        path.append(node['lon'])
        path.append(node['weight'])

    path_str2 = "safest: "+(", ".join(str(_) for _ in path))
    path_str2 = path_str2+", "+str(round(cost*1000,4))
    print(path_str2)

    #datas = {'route': path_str, 'weight':str(round(cost*1000, 4))}
    #url = "http://yourguard.dothome.co.kr/"
    #path = 'html/'
    #requests.post(url+path, data=datas)
    
    with open('./route.txt', "w") as file:
        file.write(str(path_str + path_str2))

    #conSftp.uploadData('route.txt')
    
    #client.send_data(finalPath)
    
if __name__ == "__main__":
    main()
