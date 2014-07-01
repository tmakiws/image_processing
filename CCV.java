package Jabberwocky;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import Jabberwocky.Snark;

public class CCV{
    int w;
    int h;
    static int limit = 200;

    int id;
    int[] data = new int[129];

    static int[][] filter = {
	{1, 2, 1},
	{2, 4, 2},
	{1, 2, 1}};
    
    public CCV(BufferedImage imgsrc){
        w = imgsrc.getWidth();
        h = imgsrc.getHeight();
        //サイズ正規化
        if(w < h){
            w = w * limit / h;
            h = limit;
        }else{
            h = h * limit / w;
            w = limit;
        }
	
    }

    int[] gaussianFilter(int[] ctemp){
	int[] ctbl = new int[ctemp.length];
	for(int y = 0; y < h; ++y){
            for(int x = 0; x < w; ++x){
                int tr = 0;
                int tg = 0;
                int tb = 0;
                int t = 0;
                for(int i = -1; i < 2; ++i){
                    for(int j = -1; j < 2; ++j){
                        if(y + i < 0) continue;
                        if(x + j < 0) continue;
                        if(y + i >= h) continue;
                        if(x + j >= w) continue;
                        t += filter[i + 1][j + 1];
                        int adr = (x + j) + (y + i) * w;
                        tr += filter[i + 1][j + 1] * ((ctemp[adr] >> 16) & 255);
                        tg += filter[i + 1][j + 1] * ((ctemp[adr] >> 8)  & 255);
                        tb += filter[i + 1][j + 1] * ( ctemp[adr]        & 255);
                    }
                }
                ctbl[x + y * w] = ((tr / t) << 16) + ((tg / t) << 8) + tb / t;
            }
        }
	return ctbl;
    }

    void colorReduce(int[] ctemp, int[] ctbl){
	for(int i = 0; i < ctbl.length; ++i){
            int r = (ctemp[i] >> 16) & 192;
            int g = (ctemp[i] >> 8) & 192;
            int b = ctemp[i] & 192;
            ctbl[i] = (r << 16) + (g << 8) + b;
        }
    }

    int[][] tagging(int[] ctbl){
	int[][] lbl= new int[w][h];
        id = 0;
        for(int y = 0; y < h; ++y){
            for(int x = 0; x < w; ++x){
                int col = ctbl[y * w + x];
                if(y > 0){
                    if(x > 0){
                        if(ctbl[(y - 1) * w + x - 1] == col){
                            lbl[x][y] = lbl[x - 1][y - 1];
                            continue;
                        }
                    }
                    if(ctbl[(y - 1) * w + x] == col){
                        lbl[x][y] = lbl[x][y - 1];
                        continue;
                    }
                    if(x < w - 1){
                        if(ctbl[(y - 1) * w + x + 1] == col){
                            lbl[x][y] = lbl[x + 1][y - 1];
                            continue;
                        }
                    }
                }
                if(x > 0){
                    if(ctbl[y * w + x - 1] == col){
                        lbl[x][y] = lbl[x - 1][y];
                        continue;
                    }
                }
                lbl[x][y] = id;
                ++id;
            }
        }
	return lbl;
    }

    void tabulate(int[] ctbl, int[][] lbl){
	int[] count = new int[id];
        int[] color = new int[id];
        for(int x = 0; x < w; ++x){
            for(int y = 0; y < h; ++y){
                count[lbl[x][y]]++;
                color[lbl[x][y]] = ctbl[y * w + x];
            }
        }        
        for(int i = 0; i < id; ++i){
            int d = color[i];
            color[i] = (((d >> 22) & 3) << 4) + (((d >> 14) & 3) << 2) + ((d >> 6) & 3);
            if(count[i] < 20){
                data[color[i] * 2 + 1] ++;
            }else{
                data[color[i] * 2] ++;
            }
        }
    }

    void extractCCV(BufferedImage bi){
	int[] ctemp = bi.getRGB(0, 0, w, h, null, 0, w);
	int[] ctbl = gaussianFilter(ctemp);
	colorReduce(ctbl, ctemp);
	int[][] lbl = tagging(ctbl);
	tabulate(ctbl, lbl);
    }
}