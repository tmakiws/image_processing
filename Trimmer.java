// package Jabberwocky;

// import java.util.ArrayList;
// import java.util.Set;
// import java.util.Arrays;
// import javax.imageio.ImageIO;
// import java.io.File;
// import java.awt.image.BufferedImage;
// import java.io.IOException;


// public class Trimmer{
//     double[][] tbl;
//     byte[][] lv;
//     int w;
//     int h;

//     double scl = 5.0;
    

//     private static double getY(int p){
// 	return ((p >> 16) & 0xff) + ((p >> 8) & 0xff) + (p & 0xff);
//     }
    
//     public Trimmer(String filepath)
// 	throws IOException{
// 	try{
// 	    BufferedImage bi = ImageIO.read(new File(filepath));
// 	    w = bi.getWidth();
// 	    h = bi.getHeight();
	    
// 	    tbl = new double[w][h];
// 	    lv = new byte[w][h];
	    
// 	    for(int i = 0; i < w; i++)
// 		Arrays.fill(lv[i], -127);

// 	    int[] tmp = new int[w*h];
// 	    bi.getRGB(0, 0, w, h, tmp, 0, w);
// 	    for(int i = 0; i < w; i++)
// 		for(int j = 0; j < h; j++)
// 		    tbl[i][j] = getY(tmp[j*w+i]);
	    
// 	}catch(IOException e){
// 	    throw e;
// 	}
//     }

//     void detect(){
	
//     }

//     private static void cornerDet(double[][] src,
// 				  byte[][] lv,
// 				  int sx, int sy,
// 				  int wr){
// 	if(wr < scl) return;
// 	int wm = (int)Math.floor(wr/scl);
// 	double s1 = 0, ave1 = 0;
// 	double s2 = 0, ave2 = 0;
// 	double s3 = 0, ave3 = 0;
// 	double s4 = 0, ave4 = 0;

// 	int bx1 = sx + wr - wm, by1 = sy;
// 	int bx2 = sx, by2 = sy;
// 	int bx3 = sx, by3 = sy + wr - wm;
// 	int bx4 = sx + wr - wm, by4 = sy + wr -wm;
	
// 	for(int i = 0; i < wm; i++){
// 	    for(int j = 0; j < wm; j++){
// 		ave1 = src[i+bx1][j+by1];
// 		ave2 = src[i+bx2][j+by2];
// 		ave3 = src[i+bx3][j+by3];
// 		ave4 = src[i+bx4][j+by4];
// 	    }
// 	}
// 	ave1 /= wm*wm;
// 	ave2 /= wm*wm;
// 	ave3 /= wm*wm;
// 	ave4 /= wm*wm;
	
// 	for(int i = 0; i < wm; i++){
// 	    for(int j = 0; j < wm; j++){
// 		s1 = Math.pow(src[i+bx1][j+by1] - ave1, 2);
// 		s2 = Math.pow(src[i+bx2][j+by2] - ave2, 2);
// 		s3 = Math.pow(src[i+bx3][j+by3] - ave3, 2);
// 		s4 = Math.pow(src[i+bx4][j+by4] - ave4, 2);
// 	    }
// 	}
// 	s1 /= wm*wm;
// 	s2 /= wm*wm;
// 	s3 /= wm*wm;
// 	s4 /= wm*wm;
	
	

	
//     }
			    
			     
    

    

    
// }


