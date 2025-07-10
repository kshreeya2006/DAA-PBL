import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Comparator;

class Edge {
    int to, weight;
    public Edge(int to, int weight) {
        this.to = to;
        this.weight = weight;
    }
}

class Graph {
    int V; // Number of vertices
    List<List<Edge>> adj;

    public Graph(int V) {
        this.V = V;
        adj = new ArrayList<>();
        for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
    }

    // Adds edge between u and v with the given weight
    void addEdge(int u, int v, int weight) {
        adj.get(u).add(new Edge(v, weight));
        adj.get(v).add(new Edge(u, weight));
    }

    // Dijkstra's algorithm to find shortest paths from source
    int[] dijkstra(int source, int[] prev) {
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        //(vertex, distance)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{source, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];

            for (Edge edge : adj.get(u)) {
                int v = edge.to, weight = edge.weight;
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }
        return dist;
    }

    // Retrieves the weight of the edge between u and v
    int getEdgeWeight(int u, int v) {
        for (Edge e : adj.get(u)) {
            if (e.to == v) return e.weight;
        }
        return -1; 
    }
}

class GraphPanel extends JPanel {
    Graph graph;
    int[] prev; // Predecessor array for path reconstruction
    Point[] points; // Coordinates for each node
    int source; // Starting node 

    public GraphPanel(Graph graph, int[] prev, Point[] points, int source) {
        this.graph = graph;
        this.prev = prev;
        this.points = points;
        this.source = source;
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g2d) {
        super.paintComponent(g2d);
        Graphics2D g = (Graphics2D) g2d;
        g.setStroke(new BasicStroke(2));
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        // Draw all edges in light gray with weights
        for (int u = 0; u < graph.V; u++) {
            for (Edge e : graph.adj.get(u)) {
                int v = e.to;
                if (u < v) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(points[u].x, points[u].y, points[v].x, points[v].y);

                    int midX = (points[u].x + points[v].x) / 2;
                    int midY = (points[u].y + points[v].y) / 2;

                    
                    if ((u == 4 && v == 5) || (u == 5 && v == 4)) {
                        midY -= 15; 
                    }
                
                    if ((u == 2 && v == 6) || (u == 6 && v == 2)) {
                        midY += 15; 
                    }

                    g.setColor(Color.GRAY);
                    g.drawString(String.valueOf(e.weight), midX, midY);
                }
            }
        }

        // Highlight ONLY the shortest path from source to college in blue
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(3)); 
        int current = 7; 
        while (prev[current] != -1) {
            int parent = prev[current];
            g.drawLine(points[current].x, points[current].y, 
                      points[parent].x, points[parent].y);
            current = parent;
        }

        for (int i = 0; i < points.length; i++) {
            if (i == source) g.setColor(Color.MAGENTA); // Home
            else if (i == 7) g.setColor(Color.GREEN); // College
            else g.setColor(Color.ORANGE); // Other stops

            g.fillOval(points[i].x - 10, points[i].y - 10, 20, 20);
            g.setColor(Color.BLACK);
            String label = (i == source ? "Home" : (i == 7 ? "College" : "Stop " + (i + 1)));
            g.drawString(label, points[i].x - 20, points[i].y - 15);
        }
    }
}

// Main class for the GUI application
public class SchoolBusRouteGUI {
    public static void main(String[] args) {
        int V = 8; // Number of stops 
        Graph graph = new Graph(V);

        graph.addEdge(0, 1, 24);
        graph.addEdge(1, 4, 19);
        graph.addEdge(1, 5, 32);
        graph.addEdge(3, 0, 16);
        graph.addEdge(3, 1, 14);
        graph.addEdge(3, 4, 11);
        graph.addEdge(4, 6, 21);
        graph.addEdge(4, 7, 23);
        graph.addEdge(5, 7, 7);
        graph.addEdge(6, 7, 10);
        graph.addEdge(2, 3, 17);

        Point[] positions = {
            new Point(100, 100),  // Stop 1
            new Point(250, 100),  // Stop 2
            new Point(100, 200),  // Stop 3
            new Point(250, 200),  // Stop 4
            new Point(400, 100),  // Stop 5
            new Point(400, 200),  // Stop 6
            new Point(550, 100),  // Stop 7
            new Point(550, 200)   // College
        };

        String[] options = {"Stop 1", "Stop 2", "Stop 3", "Stop 4", "Stop 5", "Stop 6", "Stop 7"};
        String selection = (String) JOptionPane.showInputDialog(
            null,
            "Select your Home (starting point):",
            "Select Source",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        int source = 0; // Default to Stop 1
        if (selection != null) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(selection)) {
                    source = i;
                    break;
                }
            }
        }

        int[] prev = new int[V];
        graph.dijkstra(source, prev);

        // Build the shortest path from source to college (node 7)
        StringBuilder sb = new StringBuilder();
        sb.append("Edges in the Shortest Route (from Home to College):\n");
        java.util.List<Integer> path = new ArrayList<>();
        int current = 7; 
        while (current != -1) {
            path.add(0, current);
            current = prev[current];
        }
    
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            String stopU = (u == 0 ? "Stop 1" : u == 7 ? "College" : "Stop " + (u + 1));
            String stopV = (v == 0 ? "Stop 1" : v == 7 ? "College" : "Stop " + (v + 1));
            int weight = graph.getEdgeWeight(u, v);
            sb.append(stopU)
              .append(" -> ")
              .append(stopV)
              .append(" : ")
              .append(weight)
              .append("\n");
        }

        //Text area to show the shortest route edges
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setPreferredSize(new Dimension(800, 300)); 

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JFrame frame = new JFrame("School Bus Route Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new GraphPanel(graph, prev, positions, source), BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }
}