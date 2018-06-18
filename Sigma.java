import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import java.awt.Toolkit;
import java.awt.Dimension;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Sigma<T>{

 int width, height;
 double[][] points;

 final Random rnd = new Random();
 boolean randomShape = false;   

 static final String jsFileName = "data";

 final HashMap<T, List<T>> graph;
 final List<T> nodes;

 public Sigma(HashMap<T, List<T>> graph){
 	this.graph = graph;
 	this.nodes = new ArrayList<>(graph.keySet());
 	setScreenDimentions();
 	calcNodePoints();
 }

 void setScreenDimentions(){
 	points = new double[nodes.size()][2];
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    width = (int)screenSize.getWidth()/8;
    height = (int)screenSize.getHeight()/8;
 }

 void calcNodePoints(){
  int[] center = {width/2, height/2}; 
  double x = center[0];
  double y = height;
  double θ = 360 / (double)nodes.size();
  double r = height/2;

  for(int i = 0; i < nodes.size(); ++i){

    double shrink = .8+
    (((double)graph.get(nodes.get(i)).size()/(double)nodes.size()));

    if(randomShape){
     points[i][0] = rnd.nextDouble();
     points[i][1] = rnd.nextDouble();
    }else{
      double radius = r;
     points[i][0] = shrink*center[0] + (radius*Math.cos((i+1)*θ));
     points[i][1] = shrink*center[1] + (radius*Math.sin((i+1)*θ));
    }

    x = points[i][0];
    y = points[i][1];
  }
 }

 public void setRandomShape(boolean value){
  randomShape = value;
  calcNodePoints();
 }

 private String jsonGraph(){

  int maxSize = 20;
  String sigmaNodes = "{\n  \"nodes\": [";
  String sigmaEdges = "\n ],\n  \"edges\": [";

  for(T node: nodes){
    //sigmaNodes
    double[] p = getPoint(node);
    sigmaNodes +=
     jsonNode(Arrays.asList(node, graph.get(node).size(),
      p[0], p[1], selectColor(node),
      ((graph.get(node).size() /((double)nodes.size()))*maxSize)+(maxSize/2)));
    sigmaNodes += ",";

    //sigmaEdges
    for(T target: graph.get(node)){
     sigmaEdges += jsonEdge(Arrays.asList(node, target));
     sigmaEdges += ",";
    }
  }
   // Remove trailing comma
   sigmaNodes = sigmaNodes.substring(0,sigmaNodes.length()-1);
   sigmaEdges = sigmaEdges.substring(0,sigmaEdges.length()-1);

   return sigmaNodes + sigmaEdges + "\n ]\n}";
 }

 String jsonNode(List<?> fields){
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

 String jsonEdge(List<?> fields){
   if(fields.size() < 2) return "{}";
    String json = "\n   {\n";
    json += "     \"id\": \"" + fields.get(0) + "-" + fields.get(1) + "\",\n";
    json += "     \"source\": \"" + fields.get(0) + "\",\n";
    json += "     \"target\": \"" + fields.get(1) + "\",\n";
    json += "     \"type\": \"arrow\"\n";
    json += "   }";

    return json;
 }

 String selectColor(T node){
  String[] colors = {
    "#8fbc8f","#7fffd4","#ffd700", "#d2691e","#6495ed",
    "#00008b", "#006400","#483d8b","#2f4f4f","#8b0000"
  };

  double tmp = ((double)graph.get(node).size()
               /((double)nodes.size()-1))*100;
  int color = (int) Math.floor(tmp/9);
  color = color > colors.length-1 ? colors.length-1 : color;

  return colors[color];
 }

 double[] getPoint(T node){
  int i = nodes.indexOf(node);
  return points[i];
 }

 public void writeJSFile(String path){
  String jsonG = jsonGraph();

  if(path == null || path.isEmpty())
  	path = "./";

  try {
    File file = new File(path + jsFileName + ".js");
    file.createNewFile();

    BufferedWriter br = new BufferedWriter(new FileWriter(file));
    br.write("data =\n" + jsonG + ";\n");
    br.close();

  }catch(IOException e){
    e.printStackTrace();
  }
 }
}