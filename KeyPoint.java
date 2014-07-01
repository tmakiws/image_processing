package Jabberwocky;

class KeyPoint{
    int u;
    int v;
    int o;
    int s;
    double ds;
    double scl_octv;
    public KeyPoint(){
    }
    public KeyPoint(KeyPoint kp){
	u = kp.u;
	v = kp.v;
	o = kp.o;
	s = kp.s;
	ds = kp.ds;
	scl_octv = kp.scl_octv;
    }
}
