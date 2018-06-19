import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
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
 boolean randomCoords = false;

 static final String jsFileName = "data";

 final HashMap<T, List<T>> graph;
 final List<T> nodes;

 public Sigma(HashMap<T, List<T>> graph){
    this.graph = graph;
    this.nodes = new ArrayList<>(graph.keySet());
    computeScreenSize();
    computeCoords();
 }

 void computeScreenSize(){
    points = new double[nodes.size()][2];
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    width = (int)screenSize.getWidth()/8;
    height = (int)screenSize.getHeight()/8;
 }

 void computeCoords(){
  int[] center = {width/2, height/2};
  double x = center[0], y = height,
   θ = 360 / (double)nodes.size(), r = height/2;

  for(int i = 0; i < nodes.size(); ++i){
    double scale = .8+
    (((double)graph.get(nodes.get(i)).size()/(double)nodes.size()));

    if(randomCoords){
     points[i][0] = rnd.nextDouble();
     points[i][1] = rnd.nextDouble();
    }else{
     points[i][0] = scale*center[0] + (r*Math.cos((i+1)*θ));
     points[i][1] = scale*center[1] + (r*Math.sin((i+1)*θ));
    }

    x = points[i][0];
    y = points[i][1];
  }
 }

 String jsonGraph(){
  return
   "{\n  "+
   "\"nodes\": ["+

     nodes
      .stream()
      .map(this::jsonNode)
      .collect(Collectors.joining(","))+

   "\n ],\n  "+
   "\"edges\": ["+

     nodes
      .stream()
      .map(this::jsonEdges)
      .filter(edges -> !edges.isEmpty())
      .collect(Collectors.joining(","))+

   "\n ]"+
 "\n}";
 }

 String jsonNode(T u){
  return
    "\n   {\n"
    + "    \"id\": \""+ u +"\",\n"
    + "    \"label\": \""+u+"("+ graph.get(u).size()+")"+"\",\n"
    + "    \"x\": "+ point(u)[0] +",\n"
    + "    \"y\": "+ point(u)[1] +",\n"
    + "    \"color\": \""+ color(u) +"\",\n"
    + "    \"size\": "+ pointSize(u) +"\n"
    + "   }";
 }

 String jsonEdge(T u, T v){
  return
    "\n   {\n"
    + "     \"id\": \""+ u +"-"+ v +"\",\n"
    + "     \"source\": \""+ u +"\",\n"
    + "     \"target\": \""+ v +"\",\n"
    + "     \"type\": \"arrow\"\n"
    + "   }";
 }

 String jsonEdges(T u){
  return graph
    .get(u)
    .stream()
    .map(v -> jsonEdge(u,v))
    .collect(Collectors.joining(","));
 }

 final String[] colors = {
   "#8fbc8f","#7fffd4","#ffd700", "#d2691e","#6495ed",
   "#00008b", "#006400","#483d8b","#2f4f4f","#8b0000"
 };

 String color(T node){
  double tmp = ((double)graph.get(node).size()
               /((double)nodes.size()-1))*100;
  int color = (int) Math.floor(tmp/9);
  color = color > colors.length-1 ? colors.length-1 : color;

  return colors[color];
 }

 double[] point(T node){
  int i = nodes.indexOf(node);
  return points[i];
 }

 double pointSize(T node){
  int maxSize = 20;
  return ((graph.get(node).size()
          /((double)nodes.size()))
          *maxSize)
          +(maxSize/2);
 }

 public void writeJSFile(String path, String filename){
  String jsonG = jsonGraph();

  if(path == null || path.isEmpty())
    path = "./";

  if(filename == null || filename.isEmpty())
    filename = jsFileName;

  try {
    File file = new File(path + filename + ".js");
    file.createNewFile();

    BufferedWriter br = new BufferedWriter(new FileWriter(file));
    br.write(filename + " =\n" + jsonG + ";\n");
    br.close();

  }catch(IOException e){
    e.printStackTrace();
  }
 }

 public void randomCoords(boolean value){
  randomCoords = value;
  computeCoords();
 }
}
