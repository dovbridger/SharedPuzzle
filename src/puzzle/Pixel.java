package puzzle;



public class Pixel {
	public float L;
	public float A;
	public float B;
	public float importance;
	public float saliency;
	public boolean burnt = false;
	
//	public static float lFactor = 1;
//	public static int diffNorm = 2;
	
	public Pixel (double _l, double _a, double _b, double _importance){
		L = (float)_l;
		A = (float)_a;
		B = (float)_b;
		importance = (float)_importance;
		saliency = 0;
		
	}
	
	public Pixel (Pixel[][]pixels){ //creates an average pixel from a block
		int numPixels = pixels.length*pixels[0].length;
		Pixel p = new Pixel();
		for (int i=0; i<pixels.length; i++){
			for (int j=0; j<pixels[0].length; j++){
				p.add(pixels[i][j]);
			}
		}
		p.mult(1.f/numPixels);
		p.importance = p.importance/numPixels;
		p.saliency = p.saliency/numPixels;
		L = p.L;
		A = p.A;
		B = p.B;
		importance = p.importance;
		saliency = p.saliency;
	}
	
	public Pixel (int r, int g, int b, float _importance){

		double[]lab = new double[3];
		ConvColor.rgb2lab(r,g,b,lab);
		L = (float)lab[0];
		A = (float)lab[1];
		B = (float)lab[2];
		importance = _importance;
		saliency = 0;
	}
	
	public Pixel (Pixel p){
		L = p.L;
		A = p.A;
		B = p.B;
		burnt = p.burnt;
		importance = p.importance;
		saliency = p.saliency;
	}
	
	public Pixel(){
		L = 0;
		A = 0;
		B = 0;
		importance = 0;
		saliency = 0;
	}
	
	public void mult(double m){
		L *= m;
		A *= m;
		B *= m;
	}
	
	public void add (Pixel p){
		L += p.L;
		A += p.A;
		B += p.B;
		burnt = burnt || p.burnt;
		importance += p.importance;
		saliency+=p.saliency;
	}
	
	public float sum(){
		return L+A+B;
	}
	
	public void sub (Pixel p,boolean addImportance){
		L -= p.L;
		A -= p.A;
		B -= p.B;
		saliency -= p.saliency;
		burnt = burnt || p.burnt;
		if(addImportance){
			importance += p.importance;
		}else{
			importance-=p.importance;
		}
	}
	
	public void abs (){
		L = Math.abs(L);
		A = Math.abs(A);
		B = Math.abs(B);
	}
	
	public static Pixel add(Pixel p1, Pixel p2){
		float l,a,b,imp,sal;
		l = p1.L + p2.L;
		a = p1.A + p2.A;
		b = p1.B + p2.B;
		imp = p1.importance+p2.importance;
		sal = p1.saliency + p2.saliency;
		Pixel result = new Pixel(l,a,b,imp);
		result.burnt = p1.burnt || p2.burnt;
		result.saliency = sal;
		return result;
	}
	
	public static Pixel sub(Pixel p1, Pixel p2){
		float l,a,b,imp,sal;
		l = p1.L - p2.L;
		a = p1.A - p2.A;
		b = p1.B - p2.B;
		imp = p1.importance+p2.importance;
		sal = p1.saliency - p2.saliency;
		Pixel result = new Pixel(l,a,b,imp);
		result.burnt = p1.burnt || p2.burnt;
		result.saliency = sal;
		
		return result;
	}
	
	public static float getSingleGrad(Pixel p1, Pixel p2){
		Pixel grad = Pixel.sub(p1, p2);
		grad.abs();
		return grad.sum();
	}
	
	public float squareDiff(Pixel p){
		float result = 0;
		result += Math.abs(Global.lFactor*Math.pow(p.L-L, Global.diffNorm));
		result += Math.abs(Math.pow(p.A-A, Global.diffNorm));
		result += Math.abs(Math.pow(p.B-B, Global.diffNorm));
		if (Global.SALIENCY_FACTOR>0){
			result += Math.abs(Global.SALIENCY_FACTOR*Math.pow(100*(p.saliency-saliency), Global.diffNorm));
		}
		return result;
	}
	
	public static float linearDisimilarity(Pixel p1far, Pixel p1close, Pixel p2close){
		Pixel diff = Pixel.sub(p1close, p1far);
		diff.add(p1close); // an approximation of p2close
		Pixel result = Pixel.sub(diff, p2close);
		result.abs();
		return result.sum();	
	}
	
	public int[] getRGB(){
		int[]rgb = new int[3];
		ConvColor.lab2rgb(L,A,B,rgb);
		return rgb;
	}
	
	public static Pixel[] copy(Pixel[] pArray){
		Pixel[] result = new Pixel[pArray.length];
		for (int i=0; i<pArray.length; i++){
			result[i] = new Pixel(pArray[i]);
		}
		return result;
	}
	
	public static Pixel[][] copy(Pixel[][] pArray){
		Pixel[][] result = new Pixel[pArray.length][pArray[0].length];
		for (int i=0; i<pArray.length; i++){
			result[i] = Pixel.copy(pArray[i]);
		}
		return result;
	}
	
	public static Pixel[][][] copy(Pixel[][][] pArray){
		Pixel[][][] result = new Pixel[pArray.length][pArray[0].length][pArray[0][0].length];
		for (int i=0; i<pArray.length; i++){
			result[i] = Pixel.copy(pArray[i]);
		}
		return result;
	}
	public static Pixel[][][] doube2pixel(float[][][][] dParts, float[][] _importance){
		
		Pixel[][][] parts = new Pixel[dParts.length][dParts[0].length][dParts[0][0].length];
		for (int part=0; part<dParts.length; part++){
			for (int i=0; i<dParts[0].length; i++){
				for (int j=0; j<dParts[0][0].length; j++){
					parts[part][i][j] = new Pixel(dParts[part][i][j][0],dParts[part][i][j][1],dParts[part][i][j][2],_importance[i][j]);
				}
			}
		}
		return parts;
	}
	public static void initWithZeros(Pixel[][] pArray){
		for (int i=0; i<pArray.length; i++){
			initWithZeros(pArray[i]);
		}
	}

	public static void initWithZeros(Pixel[] pArray){
		for (int i=0; i<pArray.length; i++){
			pArray[i] = new Pixel();
		}
	}
	
	public static void add(Pixel[][] pArray, float val){
		for (int i=0; i<pArray.length; i++){
			for (int j=0; j<pArray[0].length; j++){
				pArray[i][j].add(new Pixel(val,val,val,1));
			}
		}
	}
	
	public static void mult(Pixel[][] pArray, float m){
		for (int i=0; i<pArray.length; i++){
			for (int j=0; j<pArray[0].length; j++){
				pArray[i][j].mult(m);
			}
		}
	}
	
	public static void print(Pixel[][] pArray,int lab){
		System.out.println();
		for (int i=0; i<pArray.length; i++){
			for (int j=0; j<pArray[0].length; j++){
				switch(lab){
				case 0:
					System.out.print(Math.round(pArray[i][j].L)+" ");
					break;
				case 1:
					System.out.print(Math.round(pArray[i][j].A)+" ");
					break;
				default:
					System.out.print(Math.round(pArray[i][j].B)+" ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void print(){
		System.out.println(L+" "+A+" "+B);
	}
}
