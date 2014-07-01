package Jabberwocky;

public class DimensionError extends Exception{
	private int code;
	public DimensionError(int code, String message){
	    super(message);
	    this.code = code;
	}
	public int getCode(){
	    return code;
	}
}