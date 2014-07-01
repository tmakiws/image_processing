package Jabberwocky.CheshireCat;

import java.awt.image.BufferedImage; 
import Jabberwocky.CheshireCat.ARGB;
import Jabberwocky.CheshireCat.YCbCr;


import javax.imageio.ImageIO;
import java.io.*;

public class ColorImage{
    ARGB[][] body;
    int[] hist;
    int w;
    int h;

    public ColorImage(BufferedImage src){
	w = src.getWidth();
	h = src.getHeight();
	body = new ARGB[w][h];
	
	int[] tbl = src.getRGB(0, 0, w, h, null, 0, w);
	for(int i = 0; i < tbl.length; i++){
	    body[i%w][i/w] = new ARGB(tbl[i]);
	}
    }

    final int[] getRGB(){
	int[] res = new int[w*h];
	for(int i = 0; i < w*h; i++){
	    res[i] = (body[i%w][i/w]).getPix();
	}
	return res;
    }

    final void creduce(int lim){
	for(int i = 0; i < w; i++){
	    for(int j = 0; j < h; j++){
		(body[i][j]).reduce(lim);
	    }
	}
    }

    public void getHist(){
	creduce(4);
	hist = new int[64];
	for(int i = 0; i < 64;) hist[i++] = 0;

	for(int i = 0; i < w; i++){
	    for(int j = 0; j < h; j++){
		hist[(body[i][j]).encodePix(2)]++;
	    }
	}
    }

    
    public static void paint(){
	try{
	    BufferedImage bi = ImageIO.read(new File("che.jpg"));

	    int w = bi.getWidth();
	    int h = bi.getHeight();

	    BufferedImage res = new BufferedImage(w, h, bi.getType());
	    ColorImage ci = new ColorImage(bi);

	    ci.getHist();
	    ci.creduce(64*256);

	    
	    int[] tbl = ci.getRGB();
	    
	    res.setRGB(0, 0, w, h, tbl, 0, w);
	    ImageIO.write(res, "jpg", new File("sample.jpg"));
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    public static void main(String args[]){
	ColorImage.paint();
    }
}