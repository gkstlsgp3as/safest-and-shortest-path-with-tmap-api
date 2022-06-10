import manXML as mx
import astar as ast
import json

def main():
    inputSourceLoc = (37.556729, 126.945855) # Ewha St.
    inputDestLoc = (37.555273, 126.936836) # Sinchon St.
    
    mappedSourceLoc = mx.getKNN(inputSourceLoc) # mapped location
    mappedDestLoc = mx.getKNN(inputDestLoc)

    print(mappedSourceLoc, mappedDestLoc)
    
    path = ast.aStar(mappedSourceLoc, mappedDestLoc) # astar algorithm
    finalPath, cost = mx.getResponsePathDict(path, mappedSourceLoc, mappedDestLoc)
    
    print("Cost of the path(km): "+str(cost))
    print(finalPath)

if __name__ == "__main__":
    main()
