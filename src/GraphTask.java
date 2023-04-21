import java.util.HashMap;
import java.util.Map;

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
      // average : 118002(ms) for graph with 2000+ vertices
      Graph g = new Graph("G");
      g.createRandomSimpleGraph(6, 9);
      System.out.println(g);

      Graph tc = g.transitiveClosure();
      System.out.println(tc);

   }

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
      public String toString() {return id;}
   }


   /** Arc represents one arrow in the graph. Two-directional edges are
    * represented by two Arc objects (for both directions).
    */
   class Arc {

      private final String id;
      private Vertex target;
      private Arc next;
      private final int info = 0;
      Arc (String s, Vertex v, Arc a) {
         id = s;
         target = v;
         next = a;
      }

      Arc (String s) {
         this (s, null, null);}

      @Override
      public String toString() {return id;}
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
      Graph (String s) {this (s, null);}

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

      /**
       * Transitive closure of the matrix is the representation
       * of existence of a path for every vertex to every other vertex.
       * The task which asks to find transitive closure of the graph can be expressed like this:
       * "Given a graph G, find if the vertex i is reachable from j for all such pairs (i,j)".
       * The following method returns a new graph object with the transitive closure of this graph.
       * The transitive closure is calculated using the Floyd-Warshall algorithm.
       */
      public Graph transitiveClosure() {
         // Create a new graph with the same vertices
         Graph tc = new Graph(id);

         // Create a map to store the vertex IDs
         Map<String, Vertex> vertices = new HashMap<>();
         Vertex v = first;
         while (v != null) {
            vertices.put(v.id, tc.createVertex(v.id));
            v = v.next;
         }

         // Create the transitive closure matrix using the adjacency matrix
         int[][] adjMatrix = createAdjMatrix();
         int n = adjMatrix.length;
         int[][] transitiveClosure = new int[n][n];
         for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
               if (i == j || adjMatrix[i][j] > 0) {
                  transitiveClosure[i][j] = 1;
               }
            }
         }
         for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
               for (int j = 0; j < n; j++) {
                  transitiveClosure[i][j] |= (transitiveClosure[i][k] & transitiveClosure[k][j]);
               }
            }
         }

         // Create the arcs in the transitive closure graph
         for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
               if (transitiveClosure[i][j] == 1) {
                  tc.createArc("", vertices.get(getVertex(i).id), vertices.get(getVertex(j).id));
               }
            }
         }

         return tc;
      }

      /**
       * Returns the vertex at the specified index.
       */
      public Vertex getVertex(int index) {
         Vertex v = first;
         for (int i = 0; i < index; i++) {
            v = v.next;
         }
         return v;
      }
   }

}
