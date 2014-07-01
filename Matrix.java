
public class Matrix{
    int r;
    int c;
    
    double[][] m;
	
    public Matrix(int coln, int rown){
	r = rown;
	c = coln;
	m = new double[rown][coln];
    }

    public Matrix(double[][] src){
	r = src.length;
	c = src[0].length;
	m = src;
    }

    private double get(int x, int y){
	return m[y][x];
    }

    void inverse(double[][] src, double[][] dst){
	int len = src.length;
	double buf;
	for(int i = 0; i < len; i++){
	    for(int j = 0; j < len; j++) dst[i][j] = 0;
	    dst[i][i] = 1;
	}

	for(int i = 0; i < len; i++){
	    buf = 1/src[i][i];
	    for(int j = 0; j < len; j++){
		src[i][j] *= buf;
		dst[i][j] *= buf;
	    }
        
	    for(int j = 0; j < len; j++)
		if(i != j){
		    buf = src[j][i];
		    for(int k = 0; k < 3; k++){
			src[j][k] -= src[i][k]*buf;
			dst[j][k] -= dst[i][k]*buf;
		    }
		}
	}
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

    Matrix inverse(){
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

	return new Matrix(inv);
    }
    
    Matrix transp(){
	double[][] res = new double[c][r];
	for(int i = 0; i < r; i++){
	    for(int j = 0; j < c; j++){
		res[j][i] = m[i][j];
	    }
	}
	return new Matrix(res);
    }

    Matrix prod(Matrix rm){
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
	
	return new Matrix(res);
    }

    public String toString(){
	String res = "[\n";
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
	double[][] src = {{2, 1},
			  {0, 1}};
	double[][] srcx = {{1, 0},
			   {1, 2}};
	Matrix mat = new Matrix(src);
	Matrix matx = new Matrix(srcx);
				  
	System.out.println((mat.inverse()).det());
    }
    
    
}