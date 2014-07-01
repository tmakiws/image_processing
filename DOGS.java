package Jabberwocky;

import java.util.ArrayList;
import java.util.Set;
import java.lang.Math;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import Jabberwocky.Snark;
import Jabberwocky.KeyPoint;
import Jabberwocky.Feature;


public class DOGS{
    
    static double SIGMAi = 0.5;
    static double SIGMA0 = 1.6;
    static int S = 3;
    static boolean IMG_DOUBLED = false;
    static double CONTR_THR = 0.05;
    static double CURV_THR = 10;
    static double EDGE_THR = (CURV_THR+1.0)*(CURV_THR+1.0)/CURV_THR;

    static double ORI_SIG_FCTR= 1.5;
    static double ORI_RADIUS  = 3.0*ORI_SIG_FCTR;
    
    static int DSCR_WIDTH = 4;
    static int DSCR_HIST_BINS = 8;
    static int DSCR_LENGTH = DSCR_WIDTH*DSCR_WIDTH*DSCR_HIST_BINS;
    
    static double DESCR_MAG_THR = 0.2;
    static double DESCR_INT_FCTR= 512.0;

    static int ORI_HIST_BINS = 36;
    
    double[][][][] L;
    double[][][][] DoG;
    int OCT;
    
    class SIFTDescriptor{
	double x;
	double y;
	double scl;
	double arg;
	double[] v;

	KeyPoint ddata;
	    
	public SIFTDescriptor(){
	    v = new double[DSCR_LENGTH];
	    ddata = null;
	}
	public SIFTDescriptor(SIFTDescriptor feat){
	    v = new double[DSCR_LENGTH];
	    ddata = null;
	    x = feat.x;
	    y = feat.y;
	    scl = feat.scl;
	    arg = feat.arg;
	    for(int i = 0; i < DSCR_LENGTH; i++){
		v[i] = feat.v[i];
	    }
	    ddata = new KeyPoint(feat.ddata);
	}
    }

    ArrayList<SIFTDescriptor> keys = new ArrayList<SIFTDescriptor>();

    public DOGS(BufferedImage img){
	double k = Math.pow(2.0, 1.0/S);
	double[] sig = new double[S+3];

	sig[0] = SIGMA0;
	for(int s = 1; s < S+3; s++){
	    double sig_prev = Math.pow(k, s-1)*SIGMA0;
	    double sig_total = sig_prev*k;
	    sig[s] = Math.sqrt(sig_total*sig_total - sig_prev*sig_prev);
	}
	
	OCT =(int)((Math.log(Math.min(img.getWidth(), img.getHeight()))/Math.log(2)) - 2);
	
	L = new double[OCT][S+3][][];
	DoG = new double[OCT][S+2][][];
	
	double[][] base = Snark.gaussianFilterOri(Snark.getPixelTable(img),
					       Math.sqrt(SIGMA0*SIGMA0 - SIGMAi*SIGMAi));
	
	for(int o = 0; o < OCT; o++)
	    for(int s = 0; s < S+3; s++){
		if(o==0 && s==0) L[o][s] = base;
		else if(s==0)    L[o][s] = Snark.downSampling(L[o-1][S]);
		else             L[o][s] = Snark.gaussianFilterOri(L[o][s-1], sig[s]);
	    }

	for(int o = 0; o < OCT; o++)
	    for(int i = 0; i < S+2; i++)
		DoG[o][i] = Snark.sub_img(L[o][i+1], L[o][i]);
    }

    boolean isExtremum(int o, int s,
		       int v, int u){
	double val = DoG[o][s][v][u];
	if(val>0){
	    //極大値を判定
	    for(int ds = s-1;ds <= s+1; ds++)
		for(int du = u-1;du <= u+1; du++)
		    for(int dv = v-1; dv <= v+1; dv++)
			if(val < DoG[o][ds][dv][du]) return false;
	}
	else{
	    //極小値を判定
	    for(int ds = s-1; ds <= s+1; ds++)
		for(int du = u-1; du <= u+1; du++)
		    for(int dv = v-1; dv <= v+1; dv++)
			if(val > DoG[o][ds][dv][du]) return false;    
	}
    
	return true;
    }

    double ABS(double x){
	return (x < 0) ? -x : x;
    }

    void inverse(double[][] mat, double[][] inv){
	double buf;
	for(int i = 0; i < 3; i++){
	    for(int j = 0; j < 3; j++) inv[i][j] = 0;
	    inv[i][i] = 1;
	}

	for(int i = 0; i < 3; i++){
	    buf = 1/mat[i][i];
	    for(int j = 0; j < 3; j++){
		mat[i][j] *= buf;
		inv[i][j] *= buf;
	    }
        
	    for(int j = 0; j < 3; j++)
		if(i != j){
		    buf = mat[j][i];
		    for(int k = 0; k < 3; k++){
			mat[j][k] -= mat[i][k]*buf;
			inv[j][k] -= inv[i][k]*buf;
		    }
		}
	}
    }

    SIFTDescriptor interpExtremum(int o, int s,
				  int u, int v){						
	double[] dx = new double[3];
	double[] dD = new double[3];
	double[][] Hm = new double[3][3];
	double[][] Hi = new double[3][3];

	int W = DoG[o][s][0].length;
	int H = DoG[o][s].length;
	
	int cnt = 0;

	while(cnt < 5){
	    dD[0] = (DoG[o][s][v][u+1]-DoG[o][s][v][u-1])/2.0;   //Dx
	    dD[1] = (DoG[o][s][v+1][u]-DoG[o][s][v-1][u])/2.0;   //Dy
	    dD[2] = (DoG[o][s+1][v][u]-DoG[o][s-1][v][u])/2.0;   //Ds
        
	    double val=DoG[o][s][v][u];

	    double Dxx=(DoG[o][s][v][u+1]+DoG[o][s][v][u-1]-2*val);
	    double Dyy=(DoG[o][s][v+1][u]+DoG[o][s][v-1][u]-2*val);
	    double Dss=(DoG[o][s+1][v][u]+DoG[o][s-1][v][u]-2*val);
	    double Dxy=(DoG[o][s][v+1][u+1]-DoG[o][s][v+1][u-1]
			-DoG[o][s][v-1][u+1]+DoG[o][s][v-1][u-1])/4.0;
	    double Dxs=(DoG[o][s+1][v][u+1]-DoG[o][s+1][v][u-1] 
			-DoG[o][s-1][v][u+1]+DoG[o][s-1][v][u-1])/4.0;
	    double Dys=(DoG[o][s+1][v+1][u]-DoG[o][s+1][v-1][u]
			-DoG[o][s-1][v+1][u]+DoG[o][s-1][v-1][u])/4.0;
	    
	    //Hesse Matrix
	    Hm[0][0]=Dxx; Hm[0][1]=Dxy; Hm[0][2]=Dxs;
	    Hm[1][0]=Dxy; Hm[1][1]=Dyy; Hm[1][2]=Dys;
	    Hm[2][0]=Dxs; Hm[2][1]=Dys; Hm[2][2]=Dss;

	    inverse(Hm, Hi);

	    dx[0]=-(Hi[0][0]*dD[0]+Hi[0][1]*dD[1]+Hi[0][2]*dD[2]);
	    dx[1]=-(Hi[1][0]*dD[0]+Hi[1][1]*dD[1]+Hi[1][2]*dD[2]);
	    dx[2]=-(Hi[2][0]*dD[0]+Hi[2][1]*dD[1]+Hi[2][2]*dD[2]);

	    if(ABS(dx[0])<0.5 && ABS(dx[1])<0.5 && ABS(dx[2])<0.5) break;

	    u+=(int)Math.round(dx[0]);
	    v+=(int)Math.round(dx[1]);
	    s+=(int)Math.round(dx[2]);

	    if(s<1 || s>S || u<1 || v<1 || u>=W-1 || v>=H-1) return null;
        
	    cnt++;
	}

	if(cnt>=5) return null;
    
	//サブピクセル推定
	double Dpow=DoG[o][s][v][u]+0.5*(dD[0]*dx[0]+dD[1]*dx[1]+dD[2]*dx[2]);
	if(ABS(Dpow)<CONTR_THR/S) return null;

	double trc=Hm[0][0]+Hm[1][1];
	double det=Hm[0][0]*Hm[1][1]-Hm[0][1]*Hm[1][0];

	if(det<=0 || trc*trc/det>=EDGE_THR) return null;

	SIFTDescriptor feat = new SIFTDescriptor();
	feat.ddata = new KeyPoint();
	feat.ddata.u = u;
	feat.ddata.v = v;
	feat.ddata.o = o;
	feat.ddata.s = s;
	feat.ddata.ds = dx[2];
	feat.ddata.scl_octv = SIGMA0*Math.pow(2.0, (s+dx[2])/S);

	feat.x = (u+dx[0])*Math.pow(2.0, o);
	feat.y = (v+dx[1])*Math.pow(2.0, o);

	feat.scl = SIGMA0*Math.pow(2.0, o+(s+dx[2])/S);

	return feat;
    }

    void scaleSpaceExtrema(){
	double pre_thr = 0.5 * CONTR_THR/S;
	for(int o = 0; o < OCT; o++){
	    int W = DoG[o][0][0].length;
	    int H = DoG[o][0].length;

	    for(int s = 1; s < S+1; s++)
		for(int u = 1; u < W-1; u++)
		    for(int v = 1; v < H-1; v++){
			if(ABS(DoG[o][s][v][u]) < pre_thr) continue;
			if(isExtremum(o, s, v, u)){
			    SIFTDescriptor feat = interpExtremum(o, s, u, v);
			    if(feat != null) keys.add(feat);
			}
		    }
	}
    }
    
    void oriHist(KeyPoint dat, double[] hst){
	double[][] Li = L[dat.o][dat.s];
	int RAD = (int)Math.round(ORI_RADIUS*dat.scl_octv);
	double sig = ORI_SIG_FCTR*dat.scl_octv;

	for(int i=0; i<ORI_HIST_BINS; i++) hst[i] = 0;

	int W = Li[0].length;
	int H = Li.length;

	sig = 2.0*sig*sig;

	for(int i=-RAD; i<=RAD; i++){
	    int px = i + dat.u;
	    if(px <= 0 || px >= W-1 ) continue;

	    for(int j=-RAD; j<=RAD; j++){
		int py = j + dat.v;
		if(py <= 0 || py >= H-1) continue;

		double dx = Li[py][px+1]-Li[py][px-1];
		double dy = Li[py+1][px]-Li[py-1][px];
		double pow = Math.sqrt(dx*dx+dy*dy);
		double arg = (Math.atan2(dy, dx)+ Math.PI)/(2*Math.PI);

		double w = Math.exp(-(i*i+j*j)/sig);
		int bin = (int)Math.round(ORI_HIST_BINS*arg);

		bin = (bin < ORI_HIST_BINS) ? bin : 0;

		hst[bin] += w*pow;
	    }
	}
    }
    
    void calc_orientation(){
	int N = keys.size();
	double[] hst = new double[ORI_HIST_BINS];

	for(int i = 0; i < N; i++){
	    SIFTDescriptor feat = keys.get(0);
	    oriHist(feat.ddata, hst);

	    for(int j=0; j<2; j++){
		double prv = hst[ORI_HIST_BINS-1], tmp;
		double h0 = hst[0];
		for(int bin=0; bin<ORI_HIST_BINS; bin++){
		    tmp = hst[bin];
		    hst[bin] = 0.25*prv+0.5*hst[bin]+0.25*hst[(bin+1)%ORI_HIST_BINS];
		    prv=tmp;
		}
	    }
	    
	    double max_h = Double.MIN_VALUE;
	    for(int k = 0; k < ORI_HIST_BINS;) max_h = Math.max(max_h, hst[k++]);
	    max_h *= 0.8;

	    for(int bin = 0; bin < ORI_HIST_BINS; bin++){
		int l = (bin==0) ? ORI_HIST_BINS-1 : bin-1;
		int r = (bin+1)%ORI_HIST_BINS;

		if(hst[bin] > hst[l] && hst[bin] > hst[r] && hst[bin] >= max_h){
		    double ori =bin + 0.5*(hst[l]-hst[r])/(hst[l]-2.0*hst[bin]+hst[r]);
		    ori = (ori<0)?ORI_HIST_BINS+ori:(ori>=ORI_HIST_BINS)?ori-ORI_HIST_BINS:ori;
		    SIFTDescriptor new_feat = new SIFTDescriptor(feat);
		    new_feat.arg = 2*Math.PI*ori/ORI_HIST_BINS-Math.PI;
		    keys.add(new_feat);
		}
	    }
	    keys.remove(0);
	}
    }

    void interpolate_hist(double[][][] hst, double pow,
			  double ubin, double vbin, double obin){
	int u0 = (int)Math.floor(ubin);
	int v0 = (int)Math.floor(vbin);
	int o0 = (int)Math.floor(obin);

	double du = ubin-u0;
	double dv = vbin-v0;
	double d0 = obin-o0;

	for(int u = 0; u <= 1; u++){
	    int pu = u0 + u;
	    if(pu < 0 || pu >= DSCR_WIDTH) continue;

	    double pow_u = pow*( (u==0) ? 1.0-du:du );
	    for(int v = 0; v <= 1; v++){
		int pv = v0 + v;
		if(pv < 0 || pv >= DSCR_WIDTH) continue;

		double pow_v = pow_u * ( (v==0) ? 1.0-dv:dv );
		for(int o = 0; o <= 1; o++){
		    int po = (o0 + o) % DSCR_HIST_BINS;

		    double pow_o = pow_v* ( (o==0) ? 1.0-d0:d0 );
		    hst[pu][pv][po] += pow_o;
		}
	    }
	}
    }


    void descrHist(SIFTDescriptor feat, double[][][] hst){
	double[][] Li = L[feat.ddata.o][feat.ddata.s];
	for(int i = 0; i < DSCR_WIDTH; i++)
	    for(int j = 0; j < DSCR_WIDTH; j++)
		for(int k = 0; k < DSCR_HIST_BINS; k++)
		    hst[i][j][k] = 0;

	double arg0 = feat.arg;
	double cos_t = Math.cos(arg0);
	double sin_t = Math.sin(arg0);
	double scl = feat.ddata.scl_octv;
	double denom = -1.0/(DSCR_WIDTH*DSCR_WIDTH*0.5);

	double bins_per_rad = DSCR_HIST_BINS/(2*Math.PI);
    
	double hst_w = 3.0*scl;
	int hst_r=(int)(hst_w*Math.sqrt(2)*(DSCR_WIDTH+1.0)*0.5+0.5);
	int W = Li[0].length;
	int H = Li.length;

	double p2 = 2*Math.PI; 

	for(int i = -hst_r; i <= hst_r; i++){
	    int px = i + feat.ddata.u;
	    if(px <= 0 || px >= W-1) continue;

	    for(int j = -hst_r; j <= hst_r; j++){
		int py = j + feat.ddata.v;
		if(py <= 0 || py >= H-1) continue;
		double u_rot = ( i*cos_t+j*sin_t)/hst_w;
		double v_rot = (-i*sin_t+j*cos_t)/hst_w;
		double u_bin = u_rot+DSCR_WIDTH/2.0-0.5;
		double v_bin = v_rot+DSCR_WIDTH/2.0-0.5;
		if(u_bin>-1.0 && u_bin<DSCR_WIDTH && v_bin>-1.0 && v_bin<DSCR_WIDTH){
		    double dx = Li[py][px+1]-Li[py][px-1];
		    double dy = Li[py+1][px]-Li[py-1][px];
		    double pow = Math.sqrt(dx*dx+dy*dy);
		    double arg = Math.atan2(dy,dx);
		    arg -= arg0;

		    while(arg < 0.0) arg += p2;
		    while(arg >= p2) arg -= p2;
		    double o_bin = arg*bins_per_rad;
		    double w = Math.exp(denom*(u_rot*u_rot+v_rot*v_rot));
		    interpolate_hist(hst, w*pow, u_bin, v_bin, o_bin);
		}
	    }
	}
    }

    void normalizeDescr(SIFTDescriptor feat){
	double sum = 0;
	for(int i = 0; i < DSCR_LENGTH; i++)
	    sum += feat.v[i]*feat.v[i];
	if(sum != 0){
	    sum = 1.0/Math.sqrt(sum);
	    for(int i = 0; i < DSCR_LENGTH; i++)
		feat.v[i] *= sum;
	}
    }

    void histToDescr(SIFTDescriptor feat, double[][][] hst){
	int cnt = 0;
	for(int i = 0; i < DSCR_WIDTH; i++){
	    for(int j = 0; j < DSCR_WIDTH; j++){
		for(int k = 0; k < DSCR_HIST_BINS; k++){
		    feat.v[cnt++] = hst[i][j][k];
		}
	    }
	}
	normalizeDescr(feat);
	for(int i = 0; i < DSCR_LENGTH; i++){
	    if(feat.v[i] > DESCR_MAG_THR) feat.v[i] = DESCR_MAG_THR;
	}

	normalizeDescr(feat);
	for(int i = 0; i < DSCR_LENGTH; i++)
	    feat.v[i] = Math.min(255, (int)(DESCR_INT_FCTR*feat.v[i]));
    }

    void computeDescriptors(){
	double[][][] hst = new double[DSCR_HIST_BINS][DSCR_WIDTH][DSCR_HIST_BINS];
	for(int i = 0; i < keys.size(); i++){
	    SIFTDescriptor f = keys.get(i);
	    descrHist(f, hst);
	    histToDescr(f, hst);
	}
    }

    void extractSIFT(){
	scaleSpaceExtrema();
	calc_orientation();
	computeDescriptors();
	L = null;
	DoG = null;
    }
}

