package Jabberwocky;
import Jabberwocky.DOGS.SIFTDescriptor;
import Jabberwocky.Feature;
import java.util.*;
import java.io.*;

class RecurParam{
    KDTree tr;
    KDTree parent;
    int d;
    int from;    
    int to;
    public RecurParam(KDTree t, int a, int b, int c, KDTree p){
	tr = t;
	d = a;
	from = b;
	to = c;
	parent = p;
    }
}

public class KDTree implements Serializable{
    static int dim = 128;
    Integer root;
    KDTree parent;
    KDTree rtree;
    KDTree ltree;
    int depth;
    static Feature[] temp;

    public KDTree(){
	rtree = null;
	ltree = null;
    }

    public KDTree(ArrayList<Feature> keyps){
	temp = new Feature[keyps.size()];
	keyps.toArray(temp);
	Integer[] ids = new Integer[keyps.size()];
	for(int i = 0; i < temp.length; i++)
	    ids[i] = i;
	System.out.println(ids.length);
	parent = null;
	depth = 0;
	constructTree(ids, 0, temp.length, 0);
    }

    int getRoot(Integer[] ids, int from, int to, int d){
	if(to-from <= 0) return -1;
	final int axis = d % dim;

	//with (select order by ) and partition(limit offset),
	//being able to process parallel
	Arrays.sort(ids, from, to, new Comparator(){
		public int compare(Object obj1, Object obj2){
		    Integer id1 = (Integer)obj1;
		    Integer id2 = (Integer)obj2;
		    Double val1 = temp[id1].v[axis];
		    Double val2 = temp[id2].v[axis];


		    if(val1==null){
			System.out.println("here1");
		    }
		    if(val2==null){
			System.out.println("here2");
		    }
		    
		    return val2.compareTo(val1);
		}
	    });
	return (int)(to-from)/2;
    }

    void constructTree(Integer[] ids, int from, int to, int d){
	int medPos = getRoot(ids, from, to, d);
	if(medPos == -1){
	    root = -1;
	    return;
	}
	root = ids[medPos];
	
	rtree = new KDTree();
	ltree = new KDTree();
	Stack<RecurParam> st = new Stack<RecurParam>();
	st.push(new RecurParam(ltree, d+1, medPos+1, to, this));
	st.push(new RecurParam(rtree, d+1, from, medPos, this));
	int c = 0;
	while(!st.empty()){
	    RecurParam rp = st.pop();
	    if(rp.to - rp.from == 1){
		rp.tr.parent = rp.parent;
		rp.tr.root = ids[rp.from];
		c++;
	    }
	    else{
		medPos = getRoot(ids, rp.from, rp.to, rp.d);
		if(medPos == -1){
		    rp.tr.parent = rp.parent; 
		    rp.tr.root = -1;
		}
		else{
		    // System.out.println((c++)+":"+rp.from+", "+rp.to+", "+ids[rp.from+medPos]);
		    rp.tr.root = ids[rp.from+medPos];
		    rp.tr.parent = rp.parent;
		    rp.tr.ltree = new KDTree();
		    rp.tr.ltree.depth = d+1;
		    st.push(new RecurParam(rp.tr.ltree, rp.d+1,
					   rp.from+medPos+1, rp.to,
					   rp.tr));
		    rp.tr.rtree = new KDTree();
		    rp.tr.rtree.depth = d+1;
		    st.push(new RecurParam(rp.tr.rtree, rp.d+1,
					   rp.from, rp.from+medPos,
					   rp.tr));
		}
	    }
	}
    }

    KDTree find(Feature feat){
	KDTree cur = this;
	int dir = -1;
	for(int d = depth; d < dim; d++){
	    if(cur.ltree == null && cur.rtree == null)
		break;
	    int det = feat.v[d].compareTo(temp[cur.root].v[d]);
	    if(det < 0 && cur.rtree != null && cur.rtree.root != -1){
		cur = cur.rtree;
		dir = 0;
	    }
	    else if(det >= 0 && cur.ltree != null && cur.ltree.root != -1){
		cur = cur.ltree;
		dir = 1;
	    }
	    else
		break;
	}

	if(dir == -1 || cur.root == -1) return cur;
	KDTree p = cur.parent;
	KDTree cand = null;
	Double ds = feat.distTo(temp[cur.root]);
	Double tmpD;
	
	while(p!=null){
	    // System.out.println(ds);
	    tmpD = feat.distTo(temp[p.root]);
	    if(tmpD < ds){
		Double candd = Double.MAX_VALUE;
		if(dir == 0
		   && p.parent.rtree != null
		   && p.parent.rtree.root != -1){
		    cand = p.parent.rtree.find(feat);
		    candd = feat.distTo(temp[cand.root]);
		}
		else if(p.parent.ltree != null
			&& p.parent.ltree.root != -1){
		    cand = p.parent.ltree.find(feat);
		    candd = feat.distTo(temp[cand.root]);
		}
		if(candd < ds){
		    ds = candd;
		    cur = cand;
		}
	    }
	    p = p.parent;
	}
	return cur;
    }

    private final KDTree binarySearch(Feature feat){
	KDTree cur = this;
	int dir = -1;
	for(int d = depth; d < dim; d++){
	    if(cur.ltree == null && cur.rtree == null)
		break;
	    if(cur.root == -1) return cur;
	    int det = feat.v[d].compareTo(temp[cur.root].v[d]);
	    if(det < 0 && cur.rtree != null && cur.rtree.root != -1){
		cur = cur.rtree;
		dir = 0;
	    }
	    else if(det >= 0 && cur.ltree != null && cur.ltree.root != -1){
		cur = cur.ltree;
		dir = 1;
	    }
	    else
		break;
	}
	return cur;
    }

    final KDTree find_loop(Feature feat){
	Stack<KDTree> st = new Stack<KDTree>();
	st.push(this);
	HashMap<Integer, Double> seen = new HashMap<Integer, Double>();
	HashSet<Integer> seenleaf = new HashSet<Integer>();
	KDTree res = null;
	Double ds = Double.MAX_VALUE;
	
	int c = 0;
	while( !st.empty() ){
	    KDTree start = st.pop();	    
	    KDTree cand = start.binarySearch(feat);
	    if(cand.root == -1) continue;
	    Double cd;
	    if(seen.containsKey(cand.root)){
		cd = seen.get(cand.root);
	    }
	    else{
		cd = feat.distTo(temp[cand.root]);
		seen.put(cand.root, cd);
	    }
	    
	    KDTree p = cand.parent;
	    KDTree prev = cand;
	    while(p != null){
		c++;
		Double pd;
		if(seen.containsKey(p.root)){
		    pd = seen.get(p.root);
		}
		else{
		    pd = feat.distTo(temp[p.root]);
		    seen.put(p.root, pd);
		}
		if(pd < cd){
		    if(pd < ds){
			ds = pd;
			res = p;
		    }
		    if(p.ltree.root == prev.root && p.rtree.root != -1 && !seenleaf.contains(p.rtree.root)){
			seenleaf.add(p.rtree.root);
			st.push(p.rtree);
		    }
		    else if(p.rtree.root == prev.root && p.ltree.root != -1 && !seenleaf.contains(p.ltree.root)){
			seenleaf.add(p.ltree.root);
			st.push(p.ltree);
		    }
		}
		else if(cd < ds){
		    ds = cd;
		    res = cand;
		}
		prev = p;
		p = p.parent;
	    }
	}
	// System.out.println(c);
	return res;
    }

    void perpetuation(String path){
	try{
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
	    oos.writeObject(this);
	}catch(IOException e){
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    static KDTree loadObject(String path){
	try{
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	    return (KDTree)ois.readObject();
	}catch(Exception e){
	    e.printStackTrace();
	    return null;
	}
    }

    void printTree(){
	for(int i = 0; i < depth; i++)
	    System.out.print("-");
	System.out.println(this.root);
	if(this.rtree != null){
	    for(int i = 0; i < depth+1; i++)
		System.out.print("-");
	    this.rtree.printTree();
	}
	if(this.ltree != null){
	    for(int i = 0; i < depth+1; i++)
		System.out.print("-");
	    this.ltree.printTree();
	}
    }
}







