import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
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
  return
   "{\n  \"nodes\": ["+

     nodes
      .stream()
      .map(this::jsonNode)
      .collect(Collectors.joining(","))+

   "\n ],\n  "+
   "\"edges\": ["+

     nodes
      .stream()
      .map(node ->
        graph.get(node).stream()
             .map(target -> jsonEdge(node, target))
             .collect(Collectors.joining(",")))
      .filter(edge -> !edge.isEmpty())
      .collect(Collectors.joining(","))+

   "\n ]\n}";
 }

 String jsonNode(T node){
  return
    "\n   {\n"
    + "    \"id\": \"" + node + "\",\n"
    + "    \"label\": \""+node+"("+ graph.get(node).size()+")"+"\",\n"
    + "    \"x\": " + getPoint(node)[0] + ",\n"
    + "    \"y\": " + getPoint(node)[1] + ",\n"
    + "    \"color\": \"" + selectColor(node) + "\",\n"
    + "    \"size\": " + pointSize(node) + "\n"
    + "   }";
 }

 String jsonEdge(T node, T target){
  return
    "\n   {\n"
    + "     \"id\": \"" + node + "-" + target + "\",\n"
    + "     \"source\": \"" + node + "\",\n"
    + "     \"target\": \"" + target + "\",\n"
    + "     \"type\": \"arrow\"\n"
    + "   }";
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

 double pointSize(T node){
  int maxSize = 20;
  return ((graph.get(node).size()
          /((double)nodes.size()))
          *maxSize)
          +(maxSize/2);
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
