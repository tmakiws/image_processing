package Jabberwocky;
import java.lang.Math;
import Jabberwocky.DOGS.SIFTDescriptor;

public class Feature{
    String imageId;
    double x;
    double y;
    double scl;
    double arg;
    Double[] v;
    public Feature(SIFTDescriptor sd){
	x = sd.x;
	y = sd.y;
	scl = sd.scl;
	arg = sd.arg;
	v = new Double[sd.v.length];
	for(int i = 0; i < v.length; i++){
	    v[i] = sd.v[i];
	}
    }
    public Feature(double a, double b, double c, double d){
	x = a; y = b; scl = c; arg = d;
    }
    Double distTo(Feature p){
    	Double res = 0.0;
    	for(int i = 0; i < v.length; i++){
    	    res += Math.pow(v[i]-p.v[i], 2);
    	}
    	return res;
    }
    
    Double histInter(Feature p){
    	Double res = 0.0;
    	for(int i = 0; i < v.length; i++){
    	    res += Math.max(v[i], p.v[i]);
    	}
    	return res;
    }
    
    Double dotP(Feature p){
	Double res = 0.0;
	for(int i = 0; i < v.length; i++){
	    res += v[i]*p.v[i];
	}
	return res;
    }
}
