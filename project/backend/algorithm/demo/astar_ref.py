import heapq
import math

class Graph:
        node = []
        adj = []
        indStart = 0
        indEnd = 0

        def __init__(self, node, edge, start, end):
                self.node = []
                self.adj = [[] for i in node]
                for n in node:
                        self.node.append((n['lat'], n['lon']))
                for e in edge:
                        self.adj[e['s']].append((e['e'], self.calDist(e['s'], e['e'])))
                        self.adj[e['e']].append((e['s'], self.calDist(e['s'], e['e'])))
                self.indStart = start
                self.indEnd = end

        def calDist(self, s, e):
                return math.sqrt(
                        (self.node[s][0] - self.node[e][0]) ** 2 +
                        (self.node[s][1] - self.node[e][1]) ** 2 )

        def aStar(self):
                dist = [-1 for i in self.node]
                prev  = [ i for i in range(len(self.node))]
                q = [] # heap queue
                # element of q:
                #   [0]: A* value
                #   [1]: actual dist (sum of cost to reach the current node)
                #   [2]: current node
                #   [3]: => prev. node
                heapq.heappush(q, (0, 0, self.indStart, self.indStart))
                while (len(q) > 0): 
                        (ast, curr_dist, curr_n, prev_n) = heapq.heappop(q) # selected node with lowest cost; moved to closed lists

                        if (dist[curr_n] == -1): # not visited
                                dist[curr_n] = curr_dist # total distance taken to reach the dest node
                                prev[curr_n] = prev_n # set prev. node for the current node

                                if (curr_n == self.indEnd): # reached the end
                                        break
                                for n in self.adj[curr_n]:
                                        (next_n, next_dist) = n
                                        g = curr_dist + next_dist 
                                        h = self.calDist(self.indEnd, next_n) # distance from current node to the destination
                                        heapq.heappush(q, ( g+h , g, next_n, curr_n)) # push adjacent nodes with updated cost; into the open list
                                        
                # derive the route
                last = self.indEnd
                route = [last]
                while (last != self.indStart):
                        last = prev[last]
                        route = [last] + route
                return {
                        'route': route,
                        'distance': dist[self.indEnd]
                }


def main():
        g = Graph(
                [
                        { 'lat':  0, 'lon':  0 },
                        { 'lat':  1, 'lon':  0 },
                        { 'lat':  1, 'lon': -1 },
                        { 'lat':  1, 'lon': -3 },
                        { 'lat':  2, 'lon': -1 },
                        { 'lat':  3, 'lon': -3 },
                        { 'lat':  4, 'lon': -2 },
                        { 'lat':  4, 'lon': -4 },
                        { 'lat':  5, 'lon': -4 }
                ], [
                        { 's': 0, 'e': 1 },
                        { 's': 0, 'e': 2 },
                        { 's': 1, 'e': 4 },
                        { 's': 2, 'e': 3 },
                        { 's': 2, 'e': 5 },
                        { 's': 3, 'e': 5 },
                        { 's': 3, 'e': 6 },
                        { 's': 4, 'e': 5 },
                        { 's': 5, 'e': 6 },
                        { 's': 6, 'e': 7 }
                ], 0, 7
        )
        print(g.aStar())

if __name__ == "__main__":
        main()
