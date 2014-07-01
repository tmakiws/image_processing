package Jabberwocky;

import Jabberwocky.Feature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class MadHatter{
    private static int magic = 10007;
    private static int num = 10;
    private List<List<Feature>> tbl;
    private List<List<List<Feature>>> tbls;
    private List<Feature> as;
    private List<Double> bs;
    private double r;

    
    public MadHatter(ArrayList<Feature> keyps, double r){
	this.r = r;

	tbls = new ArrayList<List<List<Feature>>>();
	as = new ArrayList<Feature>();
	bs = new ArrayList<Double>();
	

	int dims = (keyps.get(0)).v.length;
	int nSample = keyps.size();

	double[] means = new double[dims]; 
	double[] sigma = new double[dims];
	double[] vals = new double[nSample];
	for(int d = 0; d < dims; d++){ 
	    means[d] = 0.0; 
	    sigma[d] = 0.0;
	    for(int i = 0; i < nSample; i++){
		vals[i] = keyps.get(i).v[d];
		sigma[d] += vals[i];
	    }
	    Arrays.sort(vals);
	    means[d] = vals[vals.length/2 + 1];
	    sigma[d] = sigma[d] / (double)nSample; 
	    sigma[d] = sigma[d] - means[d] * means[d]; 
	}
	for(int i = 0; i < num; i++){
	    makeHashTbl(keyps, means, sigma);
	}
    }

    void makeHashTbl(ArrayList<Feature> keyps,
		     double[] means,
		     double[] sigma){

	Random rand = new Random(System.currentTimeMillis());
	int dims = means.length;
	Feature na = new Feature(0, 0, 0, 0);
	na.v = new Double[dims];
	for(int d = 0; d < dims; d++){ 
	    double val = sigma[d] * rand.nextGaussian() + means[d];
	    na.v[d] = val;
	}
	Double b = rand.nextDouble() * r;
	
	ArrayList<List<Feature>> ntbl = new ArrayList<List<Feature>>(); 
	for(int i = 0; i < magic; i++){ 
	    ntbl.add(new ArrayList<Feature>()); 
	} 
	for(int i = 0; i < keyps.size(); i++) { 
	    int hash = calcHash(keyps.get(i), na, b); 
	    ntbl.get(hash).add(keyps.get(i));
	}
	tbls.add(ntbl);
	as.add(na);
	bs.add(b);
    }

    public int calcHash(Feature v, Feature a, Double b){
	int hash = ((int)Math.floor((a.dotP(v) + b)/r))%magic;
	if(hash < 0) hash += magic;
	return hash;
    }

    public String search(Feature item){
	HashMap<String, Integer> tabu =
	    new HashMap<String, Integer>();
	
	for(int j = 0; j < num; j++){
	    int hash = calcHash(item, as.get(j), bs.get(j));
	    List<Feature> list = (tbls.get(j)).get(hash);
	    int size = list.size(); 
	    int dims = item.v.length;

	    double minval = Double.MAX_VALUE; 
	    int index = -1;
	    for(int i=0; i<size; i++){ 
		Feature data = list.get(i); 
		Double val = item.histInter(data);
		if(minval > val){ 
		    minval = val; 
		    index = i;
		}
	    }
	    if(index == -1){
		continue;
	    }
	    String id = (list.get(index)).imageId;
	    if(!tabu.containsKey(id)){
		tabu.put(id, 1);
	    }
	    else{
		tabu.put(id, tabu.get(id)+1);
	    }
	}
	int maxval = -1;
	String id = null;
	for(Map.Entry<String, Integer> e : tabu.entrySet()){
	    if(e.getValue() > maxval){
		maxval = e.getValue();
		id = e.getKey();
	    }
	}
	return id;
    }
}
