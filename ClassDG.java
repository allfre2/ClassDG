import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

 List<String> classNames;
 HashMap<String, List<String>> DGraph;

 // Screen dimentions
 int width, height;

 double[][] nodePoints;

 boolean randomShape = false;

 String[] sigmaScripts = {"./sigma.min.js", "./sigma.parsers.json.min.js"};

 public ClassDG(String path){

  DGraph = new HashMap<>();

  List<File> javaFiles =
    Arrays .asList( (new File(path)).listFiles() )
           .stream()
           .filter(f -> f.getName().endsWith(".java"))
           .collect(Collectors.toList());

  classNames =
   javaFiles.stream()
            .map(this::simpleFileName)
            .collect(Collectors.toList());

  for(int i = 0; i < javaFiles.size(); ++i){
   List<String> dependencies = new ArrayList<>();
    try(BufferedReader br = new BufferedReader(new FileReader(javaFiles.get(i)))){
     String line;
     List<String> tmp;
      while((line = br.readLine()) != null){
       tmp = matches(line, classNames, i);

        for(String str: tmp){
          if(!dependencies.contains(str)){
            dependencies.add(str);
          }
        }
       }
      }catch(IOException e){
       e.printStackTrace();
      }
     DGraph.put(classNames.get(i), dependencies);
   }
    nodePoints = new double[classNames.size()][2];
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    width = (int)screenSize.getWidth();
    height = (int)screenSize.getHeight();
    calcNodePoints();
 }

 public void setRandomShape(boolean value){
  randomShape = value;
  calcNodePoints();
 }

 public HashMap<String, List<String>> getDG(){
  return this.DGraph;
 }

 public void printAdjTable(){
  for(Map.Entry<String, List<String>> entry: DGraph.entrySet()){
   System.out.println(entry.getKey() + " -> " + entry.getValue());
  }
 }

 private String simpleFileName(File f){
  return f.getName().replaceAll("\\.java", "");
 }

 private List<String> matches(String line, List<String> options, int exclude){
  List<String> dependencies = new ArrayList<>();
  for(int i = 0; i < options.size(); ++i){
   if(i != exclude && line.matches(".*\\W+"+options.get(i)+"\\W+.*")){
    dependencies.add(options.get(i));
   }
  }
  return dependencies;
 }

 private void calcNodePoints(){
  int[] center = {width/2, height/2}; 
  double x = center[0];
  double y = height;
  double θ = 360 / (double)classNames.size();
  double r = height/2;
  int maxAdjEdges = classNames.size()-1;

  for(int i = 0; i < classNames.size(); ++i){

    double shrink = //1;
      1.0 - (double)(DGraph.get(classNames.get(i)).size()/maxAdjEdges);

    if(randomShape){
     nodePoints[i][0] = new Random().nextDouble();
     nodePoints[i][1] = new Random().nextDouble();
    }else{
     nodePoints[i][0] = center[0] + (r*Math.cos((i+1)*θ));
     nodePoints[i][1] = center[1] + (r*Math.sin((i+1)*θ));
     nodePoints[i][0] *= shrink;
     nodePoints[i][1] *= shrink;
    }

    x = nodePoints[i][0];
    y = nodePoints[i][1];
  }
 }

 private static String removeLastChars(String s, int n){
  return s.substring(0, s.length()
                         - ((n < 0 || n > s.length()) ? 0: n));
 }

 public String getSigmaJsonDG(){

  String sigmaJsonDG = "{\n  \"nodes\": [";
  int maxAdjEdges = classNames.size()-1;
  String[] colors = {"#8fbc8f","#7fffd4","#ffd700",
                     "#d2691e","#6495ed","#00008b",
                     "#006400","#483d8b","#2f4f4f","#8b0000"};
  for(int i = 0; i < classNames.size(); ++i){
    String name = classNames.get(i);
    double tmp = ((double)DGraph.get(name).size()/(double)maxAdjEdges)*100;
    int color = (int) Math.floor(tmp/9);
    color = color > colors.length-1 ? colors.length-1 : color;
    sigmaJsonDG += "\n   {\n    ";
    sigmaJsonDG += "\"id\": \"" + classNames.get(i) + "\",\n";
    sigmaJsonDG += "    \"label\": \"" + name + "(" + DGraph.get(name).size() + ")" + "\",\n";
    sigmaJsonDG += "    \"x\": " + nodePoints[i][0] + ",\n";
    sigmaJsonDG += "    \"y\": " + nodePoints[i][1] + ",\n";
    sigmaJsonDG += "    \"color\": \"" + colors[color] + "\",\n";
    sigmaJsonDG += "    \"size\": 10\n";
    sigmaJsonDG += "   },";
  }
   sigmaJsonDG = removeLastChars(sigmaJsonDG,1); // remove trailing comma
   sigmaJsonDG += "\n ],\n";

  sigmaJsonDG += "  \"edges\": [";
  for(int i = 0; i < classNames.size(); ++i){
    String name = classNames.get(i);
   for(String target: DGraph.get(name)){
    sigmaJsonDG += "\n   {\n";
    sigmaJsonDG += "     \"id\": \"" + name + "-" + target + "\",\n";
    sigmaJsonDG += "     \"source\": \"" + name + "\",\n";
    sigmaJsonDG += "     \"target\": \"" + target + "\",\n";
    sigmaJsonDG += "     \"type\": \"arrow\"\n";
    sigmaJsonDG += "   },";
   }
  }
   sigmaJsonDG = removeLastChars(sigmaJsonDG,1); // remove trailing comma
   sigmaJsonDG += "\n ]\n}";

   return sigmaJsonDG;
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

 public String jsonDG(){

  String jsonStr = "{";

  for(Map.Entry<String, List<String>> entry: DGraph.entrySet()){
   jsonStr += "\n  \"" + entry.getKey() + "\": [";
   List<String> dep = entry.getValue();

   for(int i = 0; i < dep.size(); ++i){
    jsonStr += "\"" + dep.get(i) + "\", ";
   }

    jsonStr = removeLastChars(jsonStr,2);
    jsonStr += "],";
  }

  jsonStr = removeLastChars(jsonStr,1);
  jsonStr += "\n}\n";

  return jsonStr;
 }

 public static void main(String[] args){
  if(args.length < 1){
    System.out.println("\nUsage:\n\tjava ClassDG [directory]\n");
  }else{
    ClassDG dg = new ClassDG(args[0]);
      dg.setRandomShape(false);
      dg.createSigmaJsonFile();
  }
 }
}
