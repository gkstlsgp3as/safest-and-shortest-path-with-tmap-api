import heapq  # 우선순위 큐 구현을 위함
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
                print(self.adj)

        def calDist(self, s, e):
                return math.sqrt(
                        (self.node[s][0] - self.node[e][0]) ** 2 +
                        (self.node[s][1] - self.node[e][1]) ** 2 )

        def dijkstra(self):
                dist = [float('inf') for i in self.node]  # set all distance as Inf
                dist[self.indStart] = 0  # start distance is 0
                prev  = [ i for i in range(len(self.node))]
                q = []
                # element of q:
                # [0]: dist
                # [1]: current
                # [2]: prev node
                heapq.heappush(q, (dist[self.indStart], self.indStart, 0))  # push the start node
                
                while q:  # until queue is empty
                    (curr_dist, curr_n, prev_n) = heapq.heappop(q)  # select the closest node 
                    if dist[curr_n] < curr_dist:  # if known distance is shorter than current distance
                        continue

                    prev[curr_n] = prev_n

                    for n in self.adj[curr_n]: # adjacent nodes
                            (next_n, next_dist) = n
                            tot_dist = curr_dist+next_dist 
                            if tot_dist < dist[next_n]:  # if total distance is shorter than knwon distance
                                    dist[next_n] = tot_dist # renew the distance list
                                    heapq.heappush(q, [tot_dist, next_n, curr_n])  # push the adjacent node with shortest distance

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
        print(g.dijkstra())

if __name__ == "__main__":
        main()
