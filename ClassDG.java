import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * creates a hashmap that contains
 * a class "dependency" graph
 */
public class ClassDG{

 List<File> javaFiles;

 HashMap<String, List<String>> DGraph;
 HashSet<List<String>> cycles;
 Sigma<String> sigma;

 public ClassDG(String[] paths){
  for(String path: paths)
    extractFiles(path);
  buildGraph();
 }

 public ClassDG(String path){
  extractFiles(path);
  buildGraph();
 }

 public ClassDG(HashMap<String, List<String>> graph){
  this.DGraph = graph;
  detectCycles();
  sigma = new Sigma<String>(DGraph);
 }

 void extractFiles(String path){
  if(javaFiles == null)
    javaFiles = new ArrayList<>();

  List<File>
   files =
     Arrays
      .asList((new File(path))
      .listFiles())
      .stream()
      .filter(f -> !f.isDirectory() && f.getName().endsWith(".java"))
      .collect(Collectors.toList());

  HashSet<File> fileSet = new HashSet<>(javaFiles);
  fileSet.addAll(files);
  javaFiles = new ArrayList<>(fileSet);
 }

 void buildGraph(){

  DGraph = new HashMap<>();

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

   sigma = new Sigma<String>(DGraph);
 }

 String getClassName(File f){
  return f.getName().replaceAll("\\.java", "");
 }

 String readFile(File file){
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

 void detectCycles(){
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

 List<String> recordCycle(List<String> visited,
                          String start,
                          String end)
 {
  Stack<String> path = new Stack<>();
  path.push(start);

  return followPath(visited, path, end);
 }

 List<String> followPath(List<String> visited,
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

 HashMap<String, List<String>> cycleGraph(){
  HashMap<String, List<String>> cycleG = new HashMap<>();

  for(List<String> cycle: cycles){
    List<String> tmp = new ArrayList<>();
    for(int i = 0; i < cycle.size(); ++i){
     if(!cycleG.containsKey(cycle.get(i))){
      cycleG.put(cycle.get(i), new ArrayList<String>());
     }

     tmp = cycleG.get(cycle.get(i));
     int target = (i+1) % cycle.size();

     if(!tmp.contains(cycle.get(target)))
      tmp.add(cycle.get(target));
    }
  }
  return cycleG;
 }

 public void createCyclesFile(String path, String filename){
  new ClassDG(cycleGraph()).createFile(path, filename);
 }

 public void createFile(String path, String filename){
  sigma.writeJSFile(path, filename);
 }

 public void printAdjTable(){
  for(Map.Entry<String, List<String>> entry: DGraph.entrySet()){
   System.out.println(entry.getKey() + " -> " + entry.getValue());
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
    ClassDG dg = new ClassDG(args);
      dg.printCycles();
      dg.createFile("./", "data");
      dg.createCyclesFile("./", "cycles");
  }
 }
}
