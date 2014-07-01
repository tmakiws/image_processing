package Jabberwocky;
    
import java.lang.Math;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Snark{
    
    static double[][] getPixelTable(BufferedImage img){
	int w = img.getWidth();
	int h = img.getHeight();
	int[] ctemp = img.getRGB(0, 0, w, h, null, 0, w);
	double[][] res = new double[h][w];
	for(int x = 0; x < w; x++){
	    for(int y = 0; y < h; y++){
		int p = ctemp[y*w + x];
		res[y][x] =
		    0.299*((p>>16)&255) + 0.587*((p>>8)&255) + 0.114*(p&255);
	    }
	}
	return res;
    }

    public static double[][] downsampling(double[][] src, double rate){
	int H = (int)Math.floor(src.length*rate);
	int W = (int)Math.floor(src[0].length*rate);
	double[][] tmp = new double[H][W];
	for(int x = 0; x < W; x++){
	    for(int y = 0; y < H; y++){
		tmp[y][x] = src[(int)(y/rate)][(int)(x/rate)];
	    }
	}
	return tmp;
    }

    public static double[][] downSampling(double[][] src){
	int H = src.length/2;
	int W = src[0].length/2;
	double[][] tmp = new double[H][W];
	for(int x = 0; x < W; x++){
	    for(int y = 0; y < H; y++){
		tmp[y][x] = src[2*y][2*x];
	    }
	}
	return tmp;
    }

    public static double[][] gaussianFilter(double[][] src, double sig){
	int Wm = (int)(1 + 2 * Math.ceil(3.0 * sig));
	int Rm = (Wm - 1) / 2;
	int W = src[0].length;
	int H = src.length;
	
	double[] msk = new double[Wm];

	double sum = -1.0;
	sig = -0.5/(sig*sig);
	for(int i = 0; i <= Wm/2; i++){
	    double t = Math.exp(sig*i*i);
	    int p = Wm/2+i;
	    msk[p] = t;
	    sum += msk[p]*2;
	}

	sum = 1.0/sum;
	for(int i = 0; i <= Wm/2; i++){
	    int p = Wm/2+i;
	    msk[p] = msk[Wm/2-i] = msk[p]*sum;
	}

	double[][] dst = new double[H][W];
	double[][] tmp = new double[H][W];
	
	for(int x = 0; x < W; x++){
	    for(int y = 0; y < H; y++){
		double agrR = 0;
		double agrG = 0;
		double agrB = 0;
		for(int i = 0; i < Wm; i++){
		    int p = y + i - Rm;
		    if(p < 0 || p >= H) continue;
		    agrR += msk[i]*(((int)Math.ceil(src[p][x]) >> 16) & 255);
		    agrG += msk[i]*(((int)Math.ceil(src[p][x]) >> 8) & 255);
		    agrB += msk[i]*((int)Math.ceil(src[p][x]) & 255);
	    }
		tmp[y][x] = ((int)Math.ceil(agrR)<<16) + ((int)Math.ceil(agrG)<<8) +Math.ceil(agrB);
	    }
	}
	
	for(int x = 0; x < W; x++){
	    for(int y = 0; y < H; y++){
		double agrR = 0;
		double agrG = 0;
		double agrB = 0;
		for(int i = 0; i < Wm; i++){
		    int p = x + i - Rm;
		    if(p < 0 || p >= W) continue;
		    agrR += msk[i]*(((int)Math.ceil(tmp[y][p]) >> 16) & 255);
		    agrG += msk[i]*(((int)Math.ceil(tmp[y][p]) >> 8) & 255);
		    agrB += msk[i]*((int)Math.ceil(tmp[y][p]) & 255);
	    }
		dst[y][x] = ((int)Math.ceil(agrR)<<16) + ((int)Math.ceil(agrG)<<8) +Math.ceil(agrB);
	    }
	}
	return dst;
    }

    static double[][] gaussianFilterIgnoreColor(double[][] src, double sig){
	int Wm = (int)(1 + 2 * Math.ceil(3.0 * sig));
	int Rm = (Wm - 1) / 2;
	int W = src[0].length;
	int H = src.length;
	
	double[] msk = new double[Wm];

	double sum = -1.0;
	sig = -0.5/(sig*sig);
	for(int i = 0; i <= Wm/2; i++){
	    double t = Math.exp(sig*i*i);
	    int p = Wm/2+i;
	    msk[p] = t;
	    sum += msk[p]*2;
	}

	sum = 1.0/sum;
	for(int i = 0; i <= Wm/2; i++){
	    int p = Wm/2+i;
	    msk[p] = msk[Wm/2-i] = msk[p]*sum;
	}

	double[][] res = new double[H][W];
	for(int i = 0; i < H; i++){
	    for(int j = 0; j < W; j++){
		res[i][j] =
		    ((((int)Math.ceil(src[i][j]) >> 16) & 255) + (((int)Math.ceil(src[i][j]) >> 8) & 255) + ((int)Math.ceil(src[i][j]) & 255)) % 255;
	    }
	}

	double[][] tmp = new double[H][W];
	for(int i = 0; i < H; i++){
	    for(int j = 0; j < W; j++){
		double agr = 0;
		for(int k = 0; k < Wm; k++){
		    int p = i + k - Rm;
		    if(p >= H || p < 0) continue;
		    agr += msk[k]*res[p][j];
		}
		tmp[i][j] = agr;
	    }
	}
	
	for(int i = 0; i < H; i++){
	    for(int j = 0; j < W; j++){
		double agr = 0;
		for(int k = 0; k < Wm; k++){
		    int p = j + k - Rm;
		    if(p >= W || p < 0) continue;
		    agr += msk[k]*tmp[i][p];
		}
		res[i][j] = agr;
	    }
	}

	return res;
    }

    static double[][] gaussianFilterOri(double[][] src, double sig){
	int Wm = (int)(1 + 2 * Math.ceil(3.0 * sig));
	int Rm = (Wm - 1) / 2;
	int W = src[0].length;
	int H = src.length;
	
	double[] msk = new double[Wm];

	double sum = -1.0;
	sig = -0.5/(sig*sig);
	for(int i = 0; i <= Wm/2; i++){
	    double t = Math.exp(sig*i*i);
	    int p = Wm/2+i;
	    msk[p] = t;
	    sum += msk[p]*2;
	}

	sum = 1.0/sum;
	for(int i = 0; i <= Wm/2; i++){
	    int p = Wm/2+i;
	    msk[p] = msk[Wm/2-i] = msk[p]*sum;
	}

	double[][] res = new double[H][W];
	double[][] tmp = new double[H][W];
	for(int i = 0; i < H; i++){
	    for(int j = 0; j < W; j++){
		double agr = 0;
		for(int k = 0; k < Wm; k++){
		    int p = i + k - Rm;
		    if(p >= H || p < 0) continue;
		    agr += msk[k]*src[p][j];
		}
		tmp[i][j] = agr;
	    }
	}
	
	for(int i = 0; i < H; i++){
	    for(int j = 0; j < W; j++){
		double agr = 0;
		for(int k = 0; k < Wm; k++){
		    int p = j + k - Rm;
		    if(p >= W || p < 0) continue;
		    agr += msk[k]*tmp[i][p];
		}
		res[i][j] = agr;
	    }
	}

	return res;
    }

    static double[][] sub_img(double[][] a,
			      double[][] b){
	int W = Math.min(a[0].length, b[0].length);
	int H = Math.min(a.length, b.length);
	double[][] dst = new double[H][W];
	for(int x = 0; x < W; x++)
	    for(int y = 0; y < H; y++)
		dst[y][x] = a[y][x] - b[y][x];
	return dst;
    }
}