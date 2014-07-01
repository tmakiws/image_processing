package Jabberwocky.CheshireCat;

public class ARGB{
    int a;
    int r;
    int g;
    int b;
    
    public ARGB(int a, int r, int g, int b){
	this.a = a; this.r = r;
	this.g = g; this.b = b;
    }

    public ARGB(int pix){
	a = (pix >> 24) & 0xff;
	r = (pix >> 16) & 0xff;
	g = (pix >> 8) & 0xff;
	b = pix & 0xff;
    }

    public int getPix(){
	return (a << 24) + (r << 16) + (g << 8) + b;
    }

    public int encodePix(int step){
	return (r << (2*step)) + (g << step) + b;
    }

    private void addr(ARGB pix){
	this.a = (this.a + pix.a) % 255;
	this.r = (this.r + pix.r) % 255;
	this.g = (this.g + pix.g) % 255;
	this.b = (this.b + pix.b) % 255;
    }

    private void add(ARGB pix){
	this.a += pix.a;
	this.r += pix.r;
	this.g += pix.g;
	this.b += pix.b;
    }

    private void subr(ARGB pix){
	this.a = Math.max(0, (this.a - pix.a) % 255);
	this.r = Math.max(0, (this.r - pix.r) % 255);
	this.g = Math.max(0, (this.g - pix.g) % 255);
	this.b = Math.max(0, (this.b - pix.b) % 255);
    }

    private void sub(ARGB pix){
	this.a -= pix.a;
	this.r -= pix.r;
	this.g -= pix.g;
	this.b -= pix.b;
    }

    void reduce(int lim){
	double scl = lim / 256.0;
	a = (int)Math.floor(scl*Math.max(0, a));
	r = (int)Math.floor(scl*Math.max(0, r));
	g = (int)Math.floor(scl*Math.max(0, g));
	b = (int)Math.floor(scl*Math.max(0, b));
    }

    public int getGray(){
	return r + g + b;
    }

    public String toString(){
	return "a: " + a + " r: " + r + " g: " + g + " b: " + b;
    }
}
