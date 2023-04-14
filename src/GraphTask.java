/** Container class to different classes, that makes the whole
 * set of classes one class formally.
 */
public class GraphTask {

   /** Main method. */
   public static void main (String[] args) {
      GraphTask a = new GraphTask();
      a.run();
   }

   /** Actual main method to run examples and everything. */
   public void run() {
      Graph g = new Graph("G");
      Vertex v1 = g.createVertex("1");
      Vertex v2 = g.createVertex("2");
      Vertex v3 = g.createVertex("3");
      Vertex v4 = g.createVertex("4");
      g.createArc("a1", v1, v2);
      g.createArc("a2", v1, v3);
      g.transitiveClosure();
   }

   /**
    Transitive closure of the matrix is the representation
    of existence of a path for every vertex to every other vertex.
    The task which asks to find transitive closure of the graph can be expressed like this:
    "Given a graph G, find if the vertex i is reachable from j for all such pairs (i,j)".
    */

   class Vertex {

      private final String id;
      private Vertex next;
      private Arc first;
      private int info = 0;
      // You can add more fields, if needed

      Vertex (String s, Vertex v, Arc e) {
         id = s;
         next = v;
         first = e;
      }

      Vertex (String s) {
         this (s, null, null);
      }

      @Override
      public String toString() {
         return id;
      }
   }


   /** Arc represents one arrow in the graph. Two-directional edges are
    * represented by two Arc objects (for both directions).
    */
   class Arc {

      private final String id;
      private Vertex target;
      private Arc next;
      private final int info = 0;
      // You can add more fields, if needed

      Arc (String s, Vertex v, Arc a) {
         id = s;
         target = v;
         next = a;
      }

      Arc (String s) {
         this (s, null, null);
      }

      @Override
      public String toString() {
         return id;
      }
   } 


   class Graph {

      private final String id;
      private Vertex first;
      private int info = 0;

      private boolean[][] tc; // additional field for transitive closure matrix

      Graph (String s, Vertex v) {
         id = s;
         first = v;
      }

      Graph (String s) {
         this (s, null);
      }

      @Override
      public String toString() {
         String nl = System.getProperty ("line.separator");
         StringBuffer sb = new StringBuffer (nl);
         sb.append (id);
         sb.append (nl);
         Vertex v = first;
         while (v != null) {
            sb.append (v);
            sb.append (" -->");
            Arc a = v.first;
            while (a != null) {
               sb.append (" ");
               sb.append (a);
               sb.append (" (");
               sb.append (v);
               sb.append ("->");
               sb.append (a.target.toString());
               sb.append (")");
               a = a.next;
            }
            sb.append (nl);
            v = v.next;
         }
         return sb.toString();
      }

      public Vertex createVertex (String vid) {
         Vertex res = new Vertex (vid);
         res.next = first;
         first = res;
         return res;
      }

      public Arc createArc (String aid, Vertex from, Vertex to) {
         Arc res = new Arc (aid);
         res.next = from.first;
         from.first = res;
         res.target = to;
         return res;
      }

      /**
       * Create a connected undirected random tree with n vertices.
       * Each new vertex is connected to some random existing vertex.
       * @param n number of vertices added to this graph
       */
      public void createRandomTree (int n) {
         if (n <= 0)
            return;
         Vertex[] varray = new Vertex [n];
         for (int i = 0; i < n; i++) {
            varray [i] = createVertex ("v" + (n - i));
            if (i > 0) {
               int vnr = (int)(Math.random()*i);
               createArc ("a" + varray [vnr].toString() + "_"
                  + varray [i].toString(), varray [vnr], varray [i]);
               createArc ("a" + varray [i].toString() + "_"
                  + varray [vnr].toString(), varray [i], varray [vnr]);
            } else {}
         }
      }

      /**
       * Create an adjacency matrix of this graph.
       * Side effect: corrupts info fields in the graph
       * @return adjacency matrix
       */
      public int[][] createAdjMatrix() {
         info = 0;
         Vertex v = first;
         while (v != null) {
            v.info = info++;
            v = v.next;
         }
         int[][] res = new int [info][info];
         v = first;
         while (v != null) {
            int i = v.info;
            Arc a = v.first;
            while (a != null) {
               int j = a.target.info;
               res [i][j]++;
               a = a.next;
            }
            v = v.next;
         }
         return res;
      }

      /**
       * Create a connected simple (undirected, no loops, no multiple
       * arcs) random graph with n vertices and m edges.
       * @param n number of vertices
       * @param m number of edges
       */
      public void createRandomSimpleGraph (int n, int m) {
         if (n <= 0)
            return;
         if (n > 2500)
            throw new IllegalArgumentException ("Too many vertices: " + n);
         if (m < n-1 || m > n*(n-1)/2)
            throw new IllegalArgumentException 
               ("Impossible number of edges: " + m);
         first = null;
         createRandomTree (n);       // n-1 edges created here
         Vertex[] vert = new Vertex [n];
         Vertex v = first;
         int c = 0;
         while (v != null) {
            vert[c++] = v;
            v = v.next;
         }
         int[][] connected = createAdjMatrix();
         int edgeCount = m - n + 1;  // remaining edges
         while (edgeCount > 0) {
            int i = (int)(Math.random()*n);  // random source
            int j = (int)(Math.random()*n);  // random target
            if (i==j) 
               continue;  // no loops
            if (connected [i][j] != 0 || connected [j][i] != 0) 
               continue;  // no multiple edges
            Vertex vi = vert [i];
            Vertex vj = vert [j];
            createArc ("a" + vi.toString() + "_" + vj.toString(), vi, vj);
            connected [i][j] = 1;
            createArc ("a" + vj + "_" + vi, vj, vi);
            connected [j][i] = 1;
            edgeCount--;  // a new edge happily created
         }
      }

      /**The following method is based on the Depth-First Search(DFS) graph traversal method
       *
       * */
      public void transitiveClosure() {
         // First, create the adjacency matrix
         int[][] adjMatrix = createAdjMatrix();

         // Create a boolean matrix to store the transitive closure
         tc = new boolean[adjMatrix.length][adjMatrix.length];

         // For each vertex i, perform a DFS and mark all reachable vertices j
         for (int i = 0; i < adjMatrix.length; i++) {
            boolean[] visited = new boolean[adjMatrix.length];
            dfs(i, i, visited, adjMatrix);
         }

         // Print the transitive closure matrix
         System.out.println("Transitive closure:");
         for (boolean[] booleans : tc) {
            for (boolean aBoolean : booleans) {
               System.out.print(aBoolean ? "1 " : "0 ");
            }
            System.out.println();
         }
      }

      /**Actual DFS algorithm
       * @param i is one of the vertex we want to explore
       * @param j is another vertex we want to explore
       * @param visited is a boolean matrix which marks the vertices we have already visited
       * @param adjMatrix is an adjacency matrix for a given graph
       * */
      private void dfs(int i, int j, boolean[] visited, int[][] adjMatrix) {
         visited[j] = true;
         tc[i][j] = true;
         for (int k = 0; k < adjMatrix.length; k++) {
            if (adjMatrix[j][k] == 1 && !visited[k])
               dfs(i, k, visited, adjMatrix);
         }
      }

   }

} 
