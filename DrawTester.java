package Jabberwocky;

import java.lang.Math;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import Jabberwocky.DOGS;
import Jabberwocky.DOGS.SIFTDescriptor;
import Jabberwocky.Feature;
import Jabberwocky.KDTree;
import Jabberwocky.MadHatter;

class DefaultHashMap<K,V> extends HashMap<K,V> {
  protected V defaultValue;
  public DefaultHashMap(V defaultValue) {
    this.defaultValue = defaultValue;
  }
  @Override
  public V get(Object k) {
    V v = super.get(k);
    return ((v == null) && !this.containsKey(k)) ? this.defaultValue : v;
  }
}

public class DrawTester extends JFrame{
    ArrayList<Feature> sifts = new ArrayList<Feature>();
    KDTree tr;
    int num;
    MadHatter mh;
    HashMap<String, Integer> trains = new HashMap<String, Integer>();
    String[] names = {"sz", "gs", "md"};
    
    public static void main(String[] args){
	if(args.length == 0){
	    System.out.println("args number error!");
	    System.exit(-1);
	}
	    
	DrawTester dt = new DrawTester();

	dt.trains.put("sz", 15);
	dt.trains.put("gs", 15);
	dt.trains.put("md", 15);

	if(args[0].equals("write")){
	    dt.writeSIFTFile();
	}
	else if(args[0].equals("kdperp")){
	    if(args.length > 1 && args[1].equals("sf")){
		dt.loadFastSIFTFile();
	    }
	    else{
		dt.loadSIFTFile();
	    }
	    dt.tr = new KDTree(dt.sifts);
	    dt.tr.perpetuation("kdtree.obj");
	}
	else{
	    dt.num = 21;
	    if(args.length > 2 && args[2].equals("sf")){
		dt.loadFastSIFTFile();
	    }
	    else{
		dt.loadSIFTFile();
	    }
	    
	    dt.tr = KDTree.loadObject("kdtree.obj");
	    dt.tr.temp = new Feature[dt.sifts.size()];
	    dt.sifts.toArray(dt.tr.temp);

	    dt.mh = new MadHatter(dt.sifts, 0.01);
	    
	    String[] res;
	    if(args[0].equals("normal")){
		res = dt.searchRanking(args[1]);
	    }
	    else if(args[0].equals("lsh")){
		res = dt.searchRankingLSH(args[1]);
	    }
	    else{
		res = dt.searchRankingKD(args[1]);
	    }
	}
    }

    String printFeatVec(double[] vec){
	String res = ""+vec[0];
	for(int i = 1; i < vec.length; i++){
	    res += " " + vec[i];
	}
	return res;
    }

    void writeSIFTFile(){
	num = 0;
	try{
	    for(int k = 0; k < names.length; k++){
		for(int i = 1;i < trains.get(names[k])+1; i++){
		System.out.println(i);
		try{
		    BufferedImage bi
			= ImageIO.read(new File("simg/"+names[k]+"/"+i+".jpg"));
		    DOGS dgs = new DOGS(bi);
		    dgs.extractSIFT();
		    OutputStreamWriter osw
			= new OutputStreamWriter(
			   new BufferedOutputStream(
			    new FileOutputStream(
			     new File("ssift/"+names[k]+"/"+i+".sift"))));
		    System.out.println(dgs.keys.size());
		    for(int j = 0; j < dgs.keys.size(); j++){
			SIFTDescriptor d = dgs.keys.get(j);
			osw.write(d.x+" "+d.y+" "+d.scl+" "+d.arg+"\n");
			osw.write(printFeatVec(d.v)+"\n");
		    }
		    osw.close();
		    num++;
		}catch(IOException e){
		    return;
		}}}
	}catch(Exception e){
	    return;
	}}


    static Double[] parseline(String line){
	ArrayList<Double> res = new ArrayList<Double>();
	String rest = line.trim();
	int pos = pos = rest.indexOf(" ");
	for(;pos != -1;pos = rest.indexOf(" ")){
	    res.add(Double.parseDouble(rest.substring(0, pos)));
	    rest = rest.substring(pos+1);
	}
	res.add(Double.parseDouble(rest));
	return res.toArray(new Double[res.size()]);
    }
    
    
    void loadSIFTFile(){
	num = 0;
	String buf = "";
	try{
	    for(int j = 0; j < names.length; j++){
		for(int i = 1; i < trains.get(this.names[j])+1; i++){
		    try{
			BufferedReader br
			    = new BufferedReader(
			       new FileReader(
			        new File("ssift/"+names[j]+"/"+i+".sift")));
		buf = br.readLine();

		for(;buf!=null;){
		    Double[] ps = parseline(buf);
		    Feature nf = new Feature(ps[0], ps[1], ps[2], ps[3]);
		    nf.v = parseline(br.readLine());
		    nf.imageId = names[j] + i;
		    sifts.add(nf);
		    buf=br.readLine();
		}
		br.close();
		num++;
	    }catch(IOException e){
		return;
	    }}
	    
	}
	}catch(Exception e){
	    return;
	}
    }

    void loadFastSIFTFile(){
	num = 0;
	String buf = "";
	for(int j = 0; j < names.length; j++){
	    for(int i = 1; i < trains.get(this.names[j])+1; i++){
		try{
		    BufferedReader br
			= new BufferedReader(
	   	           new FileReader(
			    new File("ssift/"+names[j]+"/"+i+".keys")));

		    Double[] head = parseline(br.readLine());

		    for(int k = 0; k < head[0]; k++){
			buf = br.readLine();
			Double[] ps = parseline(buf);
			Feature nf = new Feature(ps[0], ps[1], ps[2], ps[3]);  
			nf.v = new Double[128];
			int ind = 0;
			for(int l = 0; l < 8; l++){
			    Double[] pl = parseline(br.readLine());
			    for(int m = 0; m < pl.length; m++){
				nf.v[ind++] = pl[m];
			    }
			}
			nf.imageId = names[j] + i;
			sifts.add(nf);
			br.readLine();
		    }
		    br.close();
		    num++;
	    }catch(IOException e){
		return;
		}}}}
    


    double euclidDist(double[] u, Double[] v){
	double res = 0;
	for(int i = 0; i < u.length; i++){
	    res += Math.pow(u[i]-v[i], 2);
	}
	return res;
    }

    String searchContainer(SIFTDescriptor feat){
	String res = null;
	double[] v = feat.v;
	double curDist = Double.MAX_VALUE;
	for(int i = 0; i < sifts.size(); i++){
	    Feature s = sifts.get(i);
	    double candDist = euclidDist(v, s.v);
	    if(curDist > candDist){
		curDist = candDist;
		res = s.imageId;
	    }
	}
	return res;
    }

   void tabulate(DefaultHashMap<String, Integer> ranking){
	DefaultHashMap<String, Integer> nameVoting
	    = new DefaultHashMap<String, Integer>(0);
	for(Map.Entry<String, Integer> e : ranking.entrySet()){
	    String name = e.getKey().substring(0, 2);
	    nameVoting.put(name, nameVoting.get(name) + e.getValue());
	}
	for(Map.Entry<String, Integer> e : nameVoting.entrySet()){
	    String name = e.getKey().substring(0, 2);
	    System.out.println(name + ":" + e.getValue());
	}
    }


    String[] searchRanking(String filename){
	String[] res = new String[num];
	System.out.println(filename);
	try{
	    DefaultHashMap<String, Integer> ranking
		= new DefaultHashMap<String, Integer>(0);
	    BufferedImage bi = ImageIO.read(new File(filename));
	    DOGS dgs = new DOGS(bi);
	    dgs.extractSIFT();
	    for(int i = 0; i < dgs.keys.size(); i++){
		SIFTDescriptor d = dgs.keys.get(i);
		String id = searchContainer(d);
		if(id==null) continue;
		ranking.put(id, ranking.get(id)+1);
	    }

	    List<Map.Entry> entries = new ArrayList<Map.Entry>(ranking.entrySet());
	    Collections.sort(entries, new Comparator(){
		    public int compare(Object obj1, Object obj2){
			Map.Entry ent1 = (Map.Entry)obj1;
			Map.Entry ent2 = (Map.Entry)obj2;
			Integer val1 = (Integer)ent1.getValue();
			Integer val2 = (Integer)ent2.getValue();
			return val2.compareTo(val2);
		    }
		});
	    tabulate(ranking);
	    int c = 0;
	    for(Map.Entry entry : entries){
		res[c++] = (String)entry.getKey();
	    }
	    
 	}catch(Exception e){
	    e.printStackTrace();
	}
	
	return res;
    }

    String[] searchRankingKD(String filename){
	String[] res = new String[num];
	try{
	    DefaultHashMap<String, Integer> ranking
		= new DefaultHashMap<String, Integer>(0);
	    System.out.println(filename);

	    BufferedImage bi = ImageIO.read(new File(filename));
	    DOGS dgs = new DOGS(bi);
	    dgs.extractSIFT();
	    for(int i = 0; i < dgs.keys.size(); i++){
		Feature d = new Feature(dgs.keys.get(i));
		KDTree kd = tr.find_loop(d);
		if(kd.root==-1) continue;
		String id = tr.temp[kd.root].imageId;
		ranking.put(id, ranking.get(id)+1);
	    }

	    List<Map.Entry> entries = new ArrayList<Map.Entry>(ranking.entrySet());
	    Collections.sort(entries, new Comparator(){
		    public int compare(Object obj1, Object obj2){
			Map.Entry ent1 = (Map.Entry)obj1;
			Map.Entry ent2 = (Map.Entry)obj2;
			Integer val1 = (Integer)ent1.getValue();
			Integer val2 = (Integer)ent2.getValue();
			return val2.compareTo(val2);
		    }
		});
	    tabulate(ranking);
	    int c = 0;
	    for(Map.Entry entry : entries){
		res[c++] = (String)entry.getKey();
	    }
	    
	}catch(Exception e){
	    e.printStackTrace();
	}
	return res;
    }

    String[] searchRankingLSH(String filename){
	String[] res = new String[num];
	try{
	    DefaultHashMap<String, Integer> ranking
		= new DefaultHashMap<String, Integer>(0);
	    
	    BufferedImage bi = ImageIO.read(new File(filename));
	    DOGS dgs = new DOGS(bi);
	    dgs.extractSIFT();
	    for(int i = 0; i < dgs.keys.size(); i++){
		Feature d = new Feature(dgs.keys.get(i));
		String id = mh.search(d);
		if(id == null){
		    KDTree kd = tr.find_loop(d);
		    if(kd.root==-1) continue;
		    id = tr.temp[kd.root].imageId;
		}
		ranking.put(id, ranking.get(id)+1);
	    }

	    List<Map.Entry> entries = new ArrayList<Map.Entry>(ranking.entrySet());
	    Collections.sort(entries, new Comparator(){
		    public int compare(Object obj1, Object obj2){
			Map.Entry ent1 = (Map.Entry)obj1;
			Map.Entry ent2 = (Map.Entry)obj2;
			Integer val1 = (Integer)ent1.getValue();
			Integer val2 = (Integer)ent2.getValue();
			return val2.compareTo(val2);
		    }
		});
	    tabulate(ranking);
	    int c = 0;
	    for(Map.Entry entry : entries){
		//		System.out.println(entry.getKey()+":"+entry.getValue());
		res[c++] = (String)entry.getKey();
	    }
	    
	}catch(Exception e){
	    e.printStackTrace();
	}
	return res;
    }
    

    


    void paint(){
	try{
	    BufferedImage bi = ImageIO.read(new File("image-1.jpg"));
	    DOGS dgs = new DOGS(bi);
	    dgs.extractSIFT();
	    System.out.println(dgs.keys.size());
	    Graphics g = bi.getGraphics();
	    Graphics2D off = bi.createGraphics();
	    off.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 RenderingHints.VALUE_ANTIALIAS_ON);
	    
	    off.setPaint(Color.red);
	    double p2 = 2*Math.PI;
	    for(int i = 0; i < dgs.keys.size(); i++){
		SIFTDescriptor kp = dgs.keys.get(i);
		// if(kp.scl < 20) continue;
		for(int j = 0; j < 20; j++){
		    double x = kp.x + kp.scl*Math.cos((j/19.0)*p2);
		    double y = kp.y + kp.scl*Math.sin((j/19.0)*p2);
		    off.draw(new Ellipse2D.Double(x, y, 1, 1));
		}
		off.draw(new Line2D.Double(kp.x, kp.y,
					   kp.x+kp.scl*Math.cos(kp.arg),
					   kp.y+kp.scl*Math.sin(kp.arg)));
	    }

	    g.drawImage(bi, 0, 0, this);

	    ImageIO.write(bi, "jpg", new File("sample.jpg"));
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
