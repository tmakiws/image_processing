package Jabberwocky.CheshireCat;

public class YCbCr{
    int y;
    int cb;
    int cr;

    public int getY(int pix){
	return (int)Math.floor(0.2990*((pix >> 16) & 0xff) + 0.5870*((pix >> 8) & 0xff) + 0.110*(pix & 0xff));
    }

    public YCbCr(int y, int cb, int cr){
	this.y = y;
	this.cb = cb;
	this.cr = cr;
    }

    public YCbCr(int pix){
	int r = (pix >> 16) & 0xff;
	int g = (pix >> 8) & 0xff;
	int b = pix & 0xff;
	y = (int)Math.floor(0.2990*r + 0.5870*g + 0.110*b);
	cb = (int)Math.floor(-0.1687*r - 0.3313*g + 0.5000*b + 128);
	cr = (int)Math.floor(0.5000*r - 0.4187*g - 0.0813*b + 128);
    }
}