package Jabberwocky;

public class MyMatrix{
    int r;
    int c;
    
    double[][] m;
	
    public MyMatrix(int rown, int coln){
	r = rown;
	c = coln;
	m = new double[rown][coln];
    }

    public MyMatrix(double[][] src){
	r = m.length;
	c = m[0].length;
	m = src;
    }

    double det(){
	double det=1.0, buf;
	
	double[][] core = new double[r][r];

	for(int i = 0; i < r; i++)
	    for(int j = 0; j < c; j++)
		core[i][j] = m[i][j];
	
	for(int i = 0; i < r; i++){
	    for(int j = 0; j < c; j++){
		if(j < i){
		    buf = core[i][j]/core[j][j];
		    for(int k = 0; k < c; k++){
			core[i][k] -= core[j][k]*buf;
		    }
		}
	    }
	}

	for(int i = 0; i < r; i++){
	    det *= core[i][i];
	}

	return det;
    }

    MyMatrix inverse(){
	if(r != c) return null;

	double[][] inv = new double[r][r];
	double[][] tmp = new double[r][r];


	
	for(int i = 0; i < r; i++){
	    for(int j = 0; j < r; j++){
		inv[i][j] = (i == j) ? 1.0 : 0.0;
		tmp[i][j] = m[i][j];
	    }
	}

	double buf;
	for(int i = 0; i < r; i++){
	    buf = 1 / inv[i][i];
	    for(int j = 0; j < r; j++){
		tmp[i][j] *= buf;
		inv[i][j] *= buf;
	    }
	    for(int j = 0; j < r; j++){
		if(i == j) continue;
		buf = tmp[j][i];
		for(int k = 0; k < r; k++){
		    tmp[j][k] -= tmp[i][k]*buf;
		    inv[j][k] -= inv[i][k]*buf;
		}
	    }
	}

	return new MyMatrix(inv);
    }
    
    MyMatrix transp(){
	double[][] res = new double[c][r];
	for(int i = 0; i < r; i++){
	    for(int j = 0; j < c; j++){
		res[j][i] = m[i][j];
	    }
	}
	return new MyMatrix(res);
    }

    MyMatrix prod(MyMatrix rm){
	if(c != rm.r || r != rm.c)
	    return null;


	double[][] res = new double[r][rm.c];

	for(int i = 0; i < r; i++){
	    for(int j = 0; j < rm.c; j++){
		double agr = 0;
		for(int k = 0; k < c; k++){
		    agr += m[i][k]*rm.m[k][j];
		}
		res[i][j] = agr;
	    }
	}
	
	return new MyMatrix(res);
    }

    public String toString(){
	String res = "[";
	for(int i = 0; i < r; i++){
	    for(int j = 0; j < c - 1; j++){
		res += m[i][j];
		res += ", ";
	    }
	    res += m[i][c-1] + "\n";
	}
	res += "]";
	return res;
    }

    public static void main(String args[]){
	double[][] src = {{1, 1},
			  {1, 1}};
	System.out.println(src);
    }
    
    
}