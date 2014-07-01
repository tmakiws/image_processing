package Jabberwocky;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Stack;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;


class QRecurParam{
    QuadTree qt;
    double ave;
    
    public QRecurParam(QuadTree qt, double ave){
	this.qt = qt;
	this.ave = ave;
    }
}




public class QuadTree extends JFrame{
    static int[][] img;
    static long thr = 1000000000L;
    int w;
    int h;

    int x;
    int y;

    QuadTree[] ch;
    
    public QuadTree(BufferedImage src){
	w = src.getWidth();
	h = src.getHeight();
	int[] tmp = src.getRGB(0, 0, w, h, null, 0, w);
	img = new int[w][h];
	
	for(int i = 0; i < tmp.length; i++){
	    img[i%w][i/w] =
		299*((tmp[i]>>16)&255)+587*((tmp[i]>>8)&255)+114*(tmp[i]&255);
	}
	x = w/2;
	y = h/2;
    }

    public QuadTree(int x, int y, int w, int h){
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
	this.ch = null;
    }

    public final void partition(){
	Stack<QRecurParam> st = new Stack<QRecurParam>();
	double ave = 0;
	for(int j = 0; j < h; j++){
	    for(int i = 0; i < w; i++){
		ave += img[i][j];
	    }
	}
	ave /= w*h;

	st.push(new QRecurParam(this, ave));
	while(!st.empty()){
	    QRecurParam rp = st.pop();
	    if(rp.qt.w/2 < 2 || rp.qt.h < 2) continue;
	    boolean flag = false;
	    long v  = 0;
	    int H = rp.qt.h;
	    int W = rp.qt.w;
	    double[] nave = {0, 0, 0, 0};
	    int hsig = H%2;
	    int wsig = W%2;
	    int[][] Hind = {{  0, 0,   H/2+hsig, H/2+hsig},
			    {H/2, H/2, H,     H}};
	    int[][] Wind = {{  0, W/2+wsig,   0, W/2+wsig},
			    {W/2,     W, W/2,     W}};
	    for(int k = 0; k < 3 ; k++){
		for(int j = Hind[0][k]; j < Hind[1][k]; j++){
		    for(int i = Wind[0][k]; i < Wind[1][k]; i++){
			double d = img[i][j] - ave;
			v += d*d;
			nave[k] += img[i][j];
		    }
		}
	    }
	    System.out.println(rp.qt.w+":"+"("+rp.qt.x+","+rp.qt.y+")");
	    if(v > thr){
		rp.qt.ch = new QuadTree[4];
		for(int i = 0; i < 4; i++){
		    int hd = Hind[1][i] - Hind[0][i];
		    int wd = Wind[1][i] - Wind[0][i];
		    rp.qt.ch[i] = new QuadTree(wd/2+wd%2, hd/2+wd%2, wd, hd);
		    st.push(new QRecurParam(rp.qt.ch[i], nave[i]/(hd*wd)));
		}
	    }
	}
    }

    void paint(BufferedImage bi){
	try{
	    int W = bi.getWidth();
	    int H = bi.getHeight();
	    Graphics g = bi.getGraphics();
	    Graphics2D off = bi.createGraphics();
	    off.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 RenderingHints.VALUE_ANTIALIAS_ON);
	    off.setPaint(Color.red);
	    Stack<QuadTree> st = new Stack<QuadTree>();
	    st.push(this);
	
	    while(!st.empty()){
		QuadTree qt = st.pop();
		off.draw(new Ellipse2D.Double(qt.x, qt.y, 1, 1));
		if(qt.ch!=null){
		    off.setPaint(Color.black);
		    off.draw(new Line2D.Double(Math.max(0, qt.x-qt.w/2), qt.y,
					       Math.min(W, qt.x+qt.w/2), qt.y));
		    off.draw(new Line2D.Double(qt.x, Math.max(0, qt.y-qt.h/2), 
					       qt.x, Math.min(W, qt.y+qt.h/2)));
		    off.setPaint(Color.red);
		    for(int i = 0; i < 4; i++){
			st.push(qt.ch[i]);
		    }
		}
	    }
	    g.drawImage(bi, 0, 0, this);
	    ImageIO.write(bi, "jpg", new File("sample.jpg"));
	}catch(IOException e){
	    e.printStackTrace();
	}
    }

    public static void main(String args[]){
	try{
	    BufferedImage src = ImageIO.read(new File("test.jpg"));
	    QuadTree qt = new QuadTree(src);
	    qt.partition();
	    qt.paint(src);
	}catch(IOException e){
	    e.printStackTrace();
	}
    }
    
    
    
}











