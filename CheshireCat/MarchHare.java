package Jabberwocky.CheshireCat;

import java.awt.image.BufferedImage; 
import java.util.Random;
import java.util.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import Jabberwocky.CheshireCat.ColorImage;

public class MarchHare extends JFrame{
    ColorImage ci;
    double[][] entrp;
    // static double magic = 2/(3+Math.sqrt(5));
    static double magic = 1.0/50;
    static int samplingN = 3;
    
    public MarchHare(BufferedImage src){
	ci = new ColorImage(src);
	entrp = new double[ci.w][ci.h];
	for(int i = 0; i < ci.w; i++)
	    for(int j = 0; j < ci.h; j++)
		entrp[i][j] = 0;
    }

    private void calcEntropy(){
	double[] k;
	int Wm = (int)Math.floor(magic*Math.min(ci.w, ci.h));
	if(Wm % 2 == 0 && Wm > 0) Wm--;
	Wm = 5;//const
	int Rm = Wm/2;

	k = new double[Wm];

	double ave = 0;
	for(int i = 0; i < ci.w; i++){
	    for(int j = 0; j < ci.h; j++){
		ave += ci.body[i][j].getGray();
	    }
	}
	ave /= ci.w*ci.h;
	double sig = 0;
	int n = ci.w*ci.h;
	for(int i = 0; i < ci.w; i++){
	    for(int j = 0; j < ci.h; j++){
		sig += (int)Math.pow((ci.body[i][j].getGray() - ave), 2)/n;
	    }
	}
	
	ave *= Math.sqrt(3);

	Random rand = new Random(System.currentTimeMillis());
	for(int s = 0; s < samplingN; s++){
	    double sum = 0;
	    for(int l = 0; l < Wm; l++){
		k[l] = rand.nextGaussian()+1;
		sum += k[l];
	    }
	    sum *= Wm / Math.sqrt(3);
	    for(int l = 0; l < Wm; l++)
		k[l] /= sum;
	    
	    double[][] tmp = new double[ci.w][ci.h];
	    for(int i = 0; i < ci.w; i++){
		for(int j = 0; j < ci.h; j++){
		    double agr = 0;
		    for(int l = 0; l < Wm; l++){
			int p = i + l - Rm;
			if( p >= ci.w || p < 0)
			    continue;
			agr += k[l]*ci.body[p][j].getGray();
		    }
		    tmp[i][j] = agr;
		}
	    }
	    for(int i = 0; i < ci.w; i++){
		for(int j = 0; j < ci.h; j++){
		    double agr = 0;
		    for(int l = 0; l < Wm; l++){
			int p = j + l - Rm;
			if( p >= ci.h || p < 0)
			    continue;
			agr += k[l]*tmp[i][p];
		    }
		    agr = -Math.pow((agr - ave)/ave, 2) / 2;
		    entrp[i][j] += Math.pow(Math.E, agr)*agr;
		}
	    }
	}
    }

    void foolCalcEntr(){
	double ave = 127*127*3;
	double sig = 0;
	for(int i = 0; i < ci.w; i++){
	    for(int j = 0; j < ci.h; j++){
		entrp[i][j] = Math.pow(127*ci.body[i][j].getGray() - ave, 2);
		sig += entrp[i][j];
	    }
	}
	sig /= ci.w*ci.h;
	for(int i = 0; i < ci.w; i++){
	    for(int j = 0; j < ci.h; j++){
		entrp[i][j] = -entrp[i][j]/(2*sig*sig);
		entrp[i][j] = entrp[i][j] * Math.log(entrp[i][j]);
	    }
	}
    }

    void makeMask(){
    }


    void paint(BufferedImage bi, ArrayList<Map.Entry> es){
	try{
	    Graphics g = bi.getGraphics();
	    Graphics2D off = bi.createGraphics();
	    off.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 RenderingHints.VALUE_ANTIALIAS_ON);
	    

	    off.setPaint(Color.white);
	    int c = 0;
	    int lim = (int)Math.floor(bi.getWidth()*bi.getHeight()*0.10);
	    boolean f = false;
	    for(Map.Entry e : es){
		if(c < 10000){
		    Integer p = (Integer)e.getKey();
		    off.draw(new Ellipse2D.Double(p%10000, p/10000, 1, 1));
		}
		c++;
	    }
	    g.drawImage(bi, 0, 0, this);
	    ImageIO.write(bi, "jpg", new File("mh.jpg"));
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    public static void main(String args[]){
	try{
	    BufferedImage bi = ImageIO.read(new File("who.jpg"));
	    MarchHare mh = new MarchHare(bi);
	    mh.calcEntropy();
	    HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
	    for(int i = 0; i < mh.ci.w; i++){
		for(int j = 0; j < mh.ci.h; j++){
		    tmp.put(i+10000*j, mh.entrp[i][j]);
		}
	    }
	    ArrayList<Map.Entry> es = new ArrayList<Map.Entry>(tmp.entrySet());
	    Collections.sort(es, new Comparator(){
		    public int compare(Object obj1, Object obj2){
			Map.Entry e1 = (Map.Entry)obj1;
			Map.Entry e2 = (Map.Entry)obj2;
			return ((Double)e2.getValue()).compareTo((Double)e1.getValue());
		    }
		});
	    mh.paint(bi, es);
	    
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    
}