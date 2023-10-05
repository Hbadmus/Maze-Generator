import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

//represents the maze
class Maze extends World {
  ArrayList<Vertex> vertices;
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> edges;
  int xSize;  
  int ySize;
  ArrayList<Edge> worklist = new ArrayList<Edge>();
  HashMap<Vertex, Vertex> key = new HashMap<Vertex,Vertex>();
  Random rand = new Random();
  Vertex start;
  Vertex end;
  boolean isBFS;
  Queue<Vertex> bfsQueue;
  Stack<Vertex> dfsStack;
  boolean isMazeComplete;


  Maze(int xSize, int ySize) {
    this.vertices = new ArrayList<Vertex>();
    this.edgesInTree = new ArrayList<Edge>();
    this.edges = new ArrayList<Edge>();
    this.xSize = xSize;
    this.ySize = ySize;
    this.makeVerts();
    this.makeEdges();
    this.makeMaze();
    this.start = this.vertices.get(0);
    this.end = this.vertices.get(this.vertices.size() - 1);
    this.isBFS = true;
    this.bfsQueue = new LinkedList<>();
    this.dfsStack = new Stack<>();
    isMazeComplete = false;

  }

  Maze(int xSize, int ySize, Random r) {
    this(xSize, ySize);
    rand = r;
  }

  //initializes the worklist by making edges 
  public void makeEdges() {
    int totalAmount = 0;
    for (int i = 0; i < this.ySize; i++) {
      for (int j = 0; j < this.xSize; j++) {
        Vertex v = this.vertices.get(totalAmount);
        if (i + 1 < this.ySize) {
          Edge e = new Edge(
              v, this.vertices.get(totalAmount + this.xSize), this.rand.nextInt());
          this.worklist.add(e);
          this.edges.add(e);
        }
        if (j + 1 < this.xSize) {
          Edge e = new Edge(
              v, this.vertices.get(totalAmount + 1), this.rand.nextInt());
          this.worklist.add(e);
          this.edges.add(e);
        }
        totalAmount++;
      }
    }
    this.worklist.sort(new EdgeWeightComparator());
  }

  //makes list of vertices
  public void makeVerts() {
    for (int i = 0; i < this.ySize; i++) {
      for (int j = 0; j < this.xSize; j++) {
        Color color = Color.gray;
        if (i == 0 && j == 0) {
          color = Color.green;
        }
        if (i == this.ySize - 1 && j == this.xSize - 1) {
          color = Color.red;
        }
        Vertex newVertex = new Vertex(color, 
            j * Vertex.SIZE + Vertex.SIZE / 2,
            i * Vertex.SIZE + Vertex.SIZE / 2);
        vertices.add(newVertex);
      }
    }
  }

  // uses Kruskal's algorithm to make random maze
  public void makeMaze() {
    for (Vertex v : vertices) {
      this.key.put(v, v);
    }
    while (this.edgesInTree.size() < this.key.size() - 1) {
      Edge e = worklist.remove(0);
      if (this.findKey(e.fro) != this.findKey(e.to)) {
        this.edgesInTree.add(e);
        this.key.replace(this.findKey(e.to), this.findKey(e.fro));
      }
    }
    for (Edge e : this.edgesInTree) {
      e.fro.outters.add(e);
      e.to.outters.add(new Edge(e.to, e.fro, e.weight));
    }
  }

  //what's the key for this vertex?
  public Vertex findKey(Vertex fro) {
    if (this.key.get(fro) == (fro)) {
      return fro;
    }
    return this.findKey(this.key.get(fro));
  }

  //draws everything (vertices and edges) onto world scene
  public WorldScene drawMaze(WorldScene maze) {
    for (Vertex v : this.vertices) {
      maze.placeImageXY(v.drawVert(), v.x, v.y);
    }
    for (Edge e : this.edges) {
      if (!this.edgesInTree.contains(e)) {
        e.fro.drawEdgeS(maze, e.to);
      }
    }
    return maze;
  }


  //makes big bang scene
  public WorldScene makeScene() {
    WorldScene game = new WorldScene(0, 0);
    game = this.drawMaze(game);
    return game;
  }

  // Step 5: BFS method
  public void bfs(Vertex start) {
    start.color = Color.YELLOW;
    start.parent = null;
    bfsQueue.add(start);
  }

  // Step 6: DFS method
  public void dfs(Vertex start) {
    start.color = Color.YELLOW;
    start.parent = null;
    dfsStack.push(start);
  }

  // Step 7: onTick method
  public void onTick() {
    if (isBFS) {
      bfsOnTick();
    } else {
      dfsOnTick();
    }
  }

  // Handles BFS logic for onTick
  private void bfsOnTick() {
    if (!bfsQueue.isEmpty() && !isMazeComplete) {
      Vertex current = bfsQueue.remove();
      if (current != end) {
        current.color = Color.YELLOW;
        for (Edge edge : current.outters) {
          Vertex neighbor = edge.to;
          if (neighbor.color != Color.YELLOW) {
            neighbor.color = Color.YELLOW;
            neighbor.parent = current;
            bfsQueue.add(neighbor);
          }
        }
      } else {
        displaySolutionPath();
        isMazeComplete = true;
      }
    }
  }

  // Handles DFS logic for onTick
  private void dfsOnTick() {
    if (!dfsStack.isEmpty() && !isMazeComplete) {
      Vertex current = dfsStack.pop();
      if (current != end) {
        current.color = Color.YELLOW;
        for (Edge edge : current.outters) {
          Vertex neighbor = edge.to;
          if (neighbor.color != Color.YELLOW) {
            neighbor.color = Color.YELLOW;
            neighbor.parent = current;
            dfsStack.push(neighbor);
          }
        }
      } else {
        displaySolutionPath();
        isMazeComplete = true;
      }
    }
  }
  
  //Method to display the solution path
  public void displaySolutionPath() {
    Vertex current = end;
    while (current != null && current != start) {
      current.color = Color.GREEN;
      current = current.parent;
    }
    if (current == start) {
      start.color = Color.GREEN;
    }
  }


  //handles key events
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.resetMaze();
      this.isBFS = true;
      this.bfs(this.start);
    } else if (key.equals("d")) {
      this.resetMaze();
      this.isBFS = false;
      this.dfs(this.start);
    }
  }

  //Method to reset everything but the initial maze
  public void resetMaze() {
    this.start = this.vertices.get(0);
    this.end = this.vertices.get(this.vertices.size() - 1);
    this.bfsQueue.clear();
    this.dfsStack.clear();
    isMazeComplete = false;
    resetVerticesColor();
  }

  
  //resets the color of all vertices, including start and end vertices
  private void resetVerticesColor() {
    for (int i = 0; i < vertices.size(); i++) {
      Vertex vertex = vertices.get(i);
      if (i == 0) {
        vertex.color = Color.green;
      } else if (i == vertices.size() - 1) {
        vertex.color = Color.red;
      } else {
        vertex.color = Color.gray;
      }
    }
  }
}

//represents the edges of the maze
class Edge {
  Vertex to;
  Vertex fro;
  int weight;

  Edge(Vertex fro, Vertex to, int weight) {
    this.to = to;
    this.fro = fro;
    this.weight = weight;
  }

  //draws this singular edge
  public WorldImage drawEdge(boolean across) {
    if (across) {
      return new LineImage(new Posn(Vertex.SIZE, 0), Color.black);
    }
    return new LineImage(new Posn(0, Vertex.SIZE), Color.black);
  }
}

//represents the cube of a maze
class Vertex {
  Color color;
  Vertex left;
  Vertex right;
  Vertex top; 
  Vertex bottom;
  int x;
  int y;
  ArrayList<Edge> outters = new ArrayList<Edge>();
  static int SIZE = 10;
  Vertex parent;

  Vertex(Color color, int x, int y) {
    this.color = color;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
    this.x = x;
    this.y = y;
    this.parent = null;
  }

  Vertex(Color color, Vertex left, Vertex top, Vertex right, Vertex bottom, ArrayList<Edge> outters,
      int x, int y) {
    this.color = color;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.x = x;
    this.y = y;
    this.outters = outters;

  }

  //draw this singular vertex
  public WorldImage drawVert() {
    return new RectangleImage(Vertex.SIZE, Vertex.SIZE, OutlineMode.SOLID, this.color);
  }

  //draw the edges of this vertex
  public void drawEdgeS(WorldScene game, Vertex to) {
    boolean across = false;
    if (this.x == to.x) {
      across = true;
    }
    Posn p = new Posn(0, Vertex.SIZE);
    if (across) {
      p = new Posn(Vertex.SIZE, 0);
    }

    if (across) {
      game.placeImageXY(new LineImage(p, Color.BLACK), this.x, this.y 
          + (Vertex.SIZE / 2));
    } 
    else {
      game.placeImageXY(new LineImage(p, Color.BLACK), this.x
          + (Vertex.SIZE / 2), this.y);
    }
  }
}

//comparator for two edges
class EdgeWeightComparator implements Comparator<Edge> {

  // which weight edge should go first
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }
}

//examples for maze game tests
class ExamplesMaze {
  Vertex vert1;
  Vertex vert2;
  Vertex vert3;
  Edge edges1;
  Edge edge2;
  Edge edge3;
  Maze maze;

  //initiates the data
  void initData() {
    vert1 = new Vertex(Color.gray, 5, 5);
    vert2 = new Vertex(Color.gray, 10, 10);
    vert3 = new Vertex(Color.gray, 15, 15);

    edges1 = new Edge(vert1, vert2, 5);
    edge2 = new Edge(vert2, vert1, 5);
    edge3 = new Edge(vert2, vert3, 10);

    maze = new Maze(3, 2, new Random(1));
  }

  //tests MakeEdges
  void testMakeEdges(Tester t) {
    this.initData();

    ArrayList<Edge> worklist = new ArrayList<Edge>(this.maze.worklist);
    ArrayList<Edge> edges = new ArrayList<Edge>(this.maze.edges);
    t.checkExpect(maze.worklist, worklist);
    t.checkExpect(maze.edges, edges);

    this.initData();
    maze.makeEdges();

    worklist = new ArrayList<Edge>(this.maze.worklist);
    edges = new ArrayList<Edge>(this.maze.edges);
    t.checkExpect(maze.edges, edges);
    t.checkExpect(maze.worklist, worklist);
  }

  //tests makeVerts
  void testMakeVerts(Tester t) {
    this.initData();
    ArrayList<Vertex> allVerts = new ArrayList<Vertex>(this.maze.vertices);
    t.checkExpect(this.maze.vertices, allVerts);

    this.maze.rand = new Random(2);
    this.maze.vertices.clear();
    this.maze.makeVerts();

    Vertex vert4 = new Vertex(Color.green, 5, 5);
    Vertex vert5 = new Vertex(Color.gray, 15, 5);
    Vertex vert6 = new Vertex(Color.gray, 25, 5);
    Vertex vert7 = new Vertex(Color.gray, 5, 15);
    Vertex vert8 = new Vertex(Color.gray, 15, 15);
    Vertex vert9 = new Vertex(Color.red, 25, 15);
    allVerts.clear();
    allVerts.add(vert4);
    allVerts.add(vert5);
    allVerts.add(vert6);
    allVerts.add(vert7);
    allVerts.add(vert8);
    allVerts.add(vert9);
    t.checkExpect(this.maze.vertices,allVerts);
  }

  //tests makeMaze
  void testMakeMaze(Tester t) {
    this.initData();
    HashMap<Vertex,Vertex> key = new HashMap<Vertex,Vertex>();
    this.maze.key.clear();
    this.maze.makeMaze();
    key.put(this.maze.vertices.get(4), this.maze.vertices.get(4));
    key.put(this.maze.vertices.get(3), this.maze.vertices.get(3));
    key.put(this.maze.vertices.get(5), this.maze.vertices.get(5));
    key.put(this.maze.vertices.get(1), this.maze.vertices.get(1));
    key.put(this.maze.vertices.get(0), this.maze.vertices.get(0));
    key.put(this.maze.vertices.get(2), this.maze.vertices.get(2));
    t.checkExpect(this.maze.key, key);
  }

  //tests findKey 
  void testFindKey(Tester t) {
    this.initData();
    maze.key.put(vert1, vert2);
    maze.key.put(vert2, vert2);
    maze.key.put(vert3, vert1);
    t.checkExpect(maze.findKey(vert1), vert2);
    t.checkExpect(maze.findKey(vert2), vert2);
    //    t.checkExpect(maze.findKey(vert3), vert2);
  }

  //tests drawMaze
  void testDrawMaze(Tester t) {
    this.initData();
    Maze otherMaze = new Maze(1,1, new Random(1));
    WorldScene scene = new WorldScene(0,0);
    scene.placeImageXY(new RectangleImage(0,0, OutlineMode.OUTLINE, Color.black), 0, 0);
    scene.placeImageXY(new RectangleImage(10,10, OutlineMode.SOLID, Color.red), 10, 10);
    t.checkExpect(otherMaze.drawMaze(new WorldScene(0,0)), scene);
  }

  //tests makeScene
  void testMakeScene(Tester t) {
    this.initData();
    Maze otherMaze = new Maze(1,1, new Random(1));
    WorldScene scene = new WorldScene(0,0);
    scene.placeImageXY(new RectangleImage(0,0, OutlineMode.OUTLINE, Color.black), 0, 0);
    scene.placeImageXY(new RectangleImage(10,10, OutlineMode.SOLID, Color.red), 10, 10);
    t.checkExpect(otherMaze.drawMaze(new WorldScene(0,0)), scene);
  }

  //test drawEdge
  void testDrawEdge(Tester t) {
    this.initData();
    t.checkExpect(this.maze.edges.get(0).drawEdge(false), new LineImage(new Posn(0,10), Color.black));
    t.checkExpect(this.maze.edges.get(0).drawEdge(true), new LineImage(new Posn(10,0), Color.black));
    t.checkExpect(this.maze.edges.get(1).drawEdge(false), new LineImage(new Posn(0,10), Color.black));
    t.checkExpect(this.maze.edges.get(1).drawEdge(true), new LineImage(new Posn(10,0), Color.black));
    t.checkExpect(this.maze.edges.get(2).drawEdge(false), new LineImage(new Posn(0,10), Color.black));
    t.checkExpect(this.maze.edges.get(2).drawEdge(true), new LineImage(new Posn(10,0), Color.black));
  }

  //tests drawVert
  void testDrawVert(Tester t) {
    this.initData();
    t.checkExpect(this.maze.vertices.get(0).drawVert(), 
        new RectangleImage(10,10,OutlineMode.SOLID,Color.green));
    t.checkExpect(this.maze.vertices.get(1).drawVert(), 
        new RectangleImage(10,10,OutlineMode.SOLID,Color.gray));
    t.checkExpect(this.maze.vertices.get(2).drawVert(), 
        new RectangleImage(10,10,OutlineMode.SOLID,Color.gray));
  }

  //tests compare
  void testCompare(Tester t) {
    EdgeWeightComparator comparator = new EdgeWeightComparator();
    t.checkExpect(comparator.compare(edges1, edge2), 0);
    t.checkExpect(comparator.compare(edges1, edge3), -5);
    t.checkExpect(comparator.compare(edge3, edge2), 5);
  }

  //tests the game
  void testGame(Tester t) {
    Maze game = new Maze(40, 40);
    game.bigBang(1000, 1000, 0.001);
  }
}