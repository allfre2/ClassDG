import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Random;
import java.awt.Toolkit;
import java.awt.Dimension;

// Creates a HashMap that contains a Class "Dependency" Graph
public class ClassDG{

 HashMap<String, List<String>> DGraph;
 HashSet<List<String>> cycles;
 List<File> javaFiles;
 List<String> classNames;

 // Screen dimentions
 int width, height;

 double[][] nodePoints;

 boolean randomShape = false;

 public ClassDG(String path){

  DGraph = new HashMap<>();

  javaFiles =
    Arrays
     .asList((new File(path))
     .listFiles())
     .stream()
     .filter(f -> !f.isDirectory() && f.getName().endsWith(".java"))
     .collect(Collectors.toList());

  classNames =
    javaFiles
     .stream()
     .map(this::getClassName)
     .collect(Collectors.toList());

  for(File file: javaFiles){
     String type = getClassName(file);

     final String fileStr = readFile(file);

     DGraph.put(type, new ArrayList<String>(javaFiles
      .stream()
      .map(this::getClassName)
      .filter(s -> !s.equals(type) && fileStr.matches(".*\\W+"+s+"\\W+.*"))
      .collect(Collectors.toList())));
   }

   detectCycles();

    nodePoints = new double[javaFiles.size()][2];
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    width = (int)screenSize.getWidth();
    height = (int)screenSize.getHeight();
    calcNodePoints();
 }

 public void setRandomShape(boolean value){
  randomShape = value;
  calcNodePoints();
 }

 public void printAdjTable(){
  for(Map.Entry<String, List<String>> entry: DGraph.entrySet()){
   System.out.println(entry.getKey() + " -> " + entry.getValue());
  }
 }

 private String readFile(File file){
    String lines = "";
    try(BufferedReader br =
         new BufferedReader(new FileReader(file))){
      String line;
      while((line = br.readLine()) != null)
        lines += line;
    }catch(IOException e){
      e.printStackTrace();
    }
  return lines;
 }

 private void detectCycles(){

  cycles = new HashSet<List<String>>();
  List<String> visited = new ArrayList<>();

  for(final Map.Entry<String, List<String>> entry: DGraph.entrySet()){
    String key = entry.getKey();
    if(!visited.contains(key)){
     visited.add(key);

     List<String> posibleCycles = new ArrayList<String>(entry.getValue());
     posibleCycles.retainAll(visited);

      // Look for cycles
      for(String node: posibleCycles){
       List<String> cycle = recordCycle(visited, node, key);
       if(!cycle.isEmpty() && !cycles.contains(cycle)){
        cycles.add(cycle);
       }
      }
    }
  }
 }

 private List<String>
  recordCycle(List<String> visited,
                    String start,
                    String end)
 {
  Stack<String> path = new Stack<>();
  path.push(start);

  return followPath(visited, path, end);
 }

 private List<String>
  followPath(List<String> visited,
            Stack<String> path,
            String        end)
 {

  List<String> cycle = new ArrayList<>();

  if(DGraph.get(path.peek()).contains(end)){
    cycle.addAll(path);
    cycle.add(end);
  }
  else{
    List<String> adj = new ArrayList<String>(DGraph.get(path.peek()));
    adj.retainAll(visited);
    adj.removeAll(path);
  
    for(String node: adj){
     path.push(node);
     cycle = followPath(visited, path, end);
     if(!cycle.isEmpty())
      break;
     path.pop();
    }
  }
  return cycle;
 }

 private String getClassName(File f){
  return f.getName().replaceAll("\\.java", "");
 }

 private void calcNodePoints(){
  int numberOfFiles = javaFiles.size();
  int[] center = {width/2, height/2}; 
  double x = center[0];
  double y = height;
  double θ = 360 / (double)numberOfFiles;
  double r = height/2;

  for(int i = 0; i < numberOfFiles; ++i){

    double shrink =
      1.0 -
    (double)(DGraph.get(getClassName(javaFiles.get(i))).size()/numberOfFiles-1);

    if(randomShape){
     nodePoints[i][0] = new Random().nextDouble();
     nodePoints[i][1] = new Random().nextDouble();
    }else{
      double radius = r*shrink;
     nodePoints[i][0] = center[0] + (radius*Math.cos((i+1)*θ));
     nodePoints[i][1] = center[1] + (radius*Math.sin((i+1)*θ));
     nodePoints[i][0] -= shrink;
     nodePoints[i][1] -= shrink;
    }

    x = nodePoints[i][0];
    y = nodePoints[i][1];
  }
 }

 public String getSigmaJsonDG(){

  String nodes = "{\n  \"nodes\": [";
  String edges = "\n ],\n  \"edges\": [";

  for(String name: classNames){
    //nodes
    double[] point = getPoint(name);
    nodes +=
     sigmaNode(Arrays.asList(name, DGraph.get(name).size(),
      point[0], point[1], selectColor(name), 1));
    nodes += ",";

    //edges
    for(String target: DGraph.get(name)){
     edges += sigmaEdge(Arrays.asList(name, target));
     edges += ",";
    }
  }
   // Remove trailing comma
   nodes = nodes.substring(0,nodes.length()-1);
   edges = edges.substring(0,edges.length()-1);

   return nodes + edges + "\n ]\n}";
 }

 private String sigmaNode(List<?> fields){
   if(fields.size() < 6) return "{}";
  String json = "\n   {\n    ";
    json += "\"id\": \"" + fields.get(0) + "\",\n";
    json += "    \"label\": \"" + fields.get(0) +
            "(" + fields.get(1) + ")" + "\",\n";
    json += "    \"x\": " + fields.get(2) + ",\n";
    json += "    \"y\": " + fields.get(3) + ",\n";
    json += "    \"color\": \"" + fields.get(4) + "\",\n";
    json += "    \"size\": " + fields.get(5) + "\n";
    json += "   }";

    return json;
 }

 private String sigmaEdge(List<?> fields){
   if(fields.size() < 2) return "{}";
    String json = "\n   {\n";
    json += "     \"id\": \"" + fields.get(0) + "-" + fields.get(1) + "\",\n";
    json += "     \"source\": \"" + fields.get(0) + "\",\n";
    json += "     \"target\": \"" + fields.get(1) + "\",\n";
    json += "     \"type\": \"arrow\"\n";
    json += "   }";

    return json;
 }

 private String selectColor(String className){
  String[] colors = {
    "#8fbc8f","#7fffd4","#ffd700", "#d2691e","#6495ed",
    "#00008b", "#006400","#483d8b","#2f4f4f","#8b0000"
  };

  double tmp = ((double)DGraph.get(className).size()
               /((double)classNames.size()-1))*100;
  int color = (int) Math.floor(tmp/9);
  color = color > colors.length-1 ? colors.length-1 : color;

  return colors[color];
 }

 public double[] getPoint(String className){
  int i = classNames.indexOf(className);
  return nodePoints[i];
 }

 public void createSigmaJsonFile(){
  String sigmaJsonDG = getSigmaJsonDG();
  String path = "./data.js";
  try {
    File f = new File(path);
    f.createNewFile();

    BufferedWriter bWriter = new BufferedWriter(new FileWriter(f));
    bWriter.write("data =\n" + sigmaJsonDG + ";\n");
    bWriter.close();
    System.out.println("Created File: " + path);

  }catch(IOException e){
    e.printStackTrace();
  }
 }

 public void printCycles(){
  System.out.println("Circular Dependencies:");
  cycles.stream()
        .forEach(System.out::println);
 }

 public static void main(String[] args){
  if(args.length < 1){
    System.out.println("\nUsage:\n\tjava ClassDG [directory]\n");
    System.exit(-1);
  }else{
    ClassDG dg = new ClassDG(args[0]);
      dg.printCycles();
      dg.setRandomShape(false);
      dg.createSigmaJsonFile();
  }
 }
}
