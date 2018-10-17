package puzzle;

public class LABVector {
	Pixel vec[];
	int l;
	
//	static double burnFactor;

	public LABVector(Pixel[] _vec) {
		vec=_vec;
		l=vec.length;
	}
	
	public LABVector sub(LABVector v2)
	{
		Pixel vres[] = Pixel.copy(vec);
		for(int i=0;i<l;i++){
			vres[i].sub(v2.vec[i],Global.useImportance);
		}
		return new LABVector(vres);
	}
	
	
	public LABVector add(LABVector v2){
		Pixel vres[] = Pixel.copy(vec);
		for(int i=0;i<l;i++){
			vres[i].add(v2.vec[i]);
		}
		return new LABVector(vres);
	}
	
	public double sum()
	{
		double factor = 0;
		int realPixels = 0;
		double sum1=0,sum2=0,sum3=0, sumSaliency=0;
		for(int i=0;i<l;i++){
			if(vec[i].burnt){
				factor = Global.burnFactor;
			}
			else{
				factor = 1-Global.burnFactor;
				realPixels++;	
			}
			sum1+=factor*vec[i].L;
			sum2+=factor*vec[i].A;
			sum3+=factor*vec[i].B;
			sumSaliency+=factor*vec[i].saliency*Global.SALIENCY_FACTOR;
		}
		double realFraction = (double)realPixels/l;
		return (sum1+sum2+sum3+sumSaliency)/((realFraction*(1-Global.burnFactor)+(1-realFraction)*Global.burnFactor));//Dov - Normalize to a value representing all pixels
	}
	
	public double sumWithImportance()
	{
		double factor = 0;
		double factorSum = 0;
		double sum1=0,sum2=0,sum3=0, sumSaliency=0;
		for(int i=0;i<l;i++){
			factor = vec[i].importance;
			sum1+=factor*vec[i].L;
			sum2+=factor*vec[i].A;
			sum3+=factor*vec[i].B;
			sumSaliency+=factor*vec[i].saliency*Global.SALIENCY_FACTOR;
			factorSum = factorSum + factor;
		}
		return (sum1+sum2+sum3+sumSaliency)/(factorSum);
	}
	
	public LABVector mult(int n){
		Pixel vres[] = Pixel.copy(vec);
		for (int i=0; i<l; i++){
			vres[i].mult(n);
		}
		return new LABVector(vres);
	}
	
	public LABVector getDy(){
		Pixel vres[] = Pixel.copy(vec);
		vres[l-1].burnt = true;
		for (int i=0; i<l-1; i++){
			vres[i] = Pixel.sub(vec[i],vec[i+1]);
		}
		return new LABVector(vres);
	}
	
	public LABVector shiftUp(){
		Pixel vres[] = new Pixel[l];
		vres[l-1] = new Pixel();
		vres[l-1].burnt = true;
		for (int i=1; i<l; i++){
			vres[i-1] = new Pixel(vec[i]);
		}
		return new LABVector(vres);
	}
		
	public LABVector shiftDown(){
		Pixel vres[] = new Pixel[l];
		vres[0] = new Pixel();
		vres[0].burnt = true;
		for (int i=1; i<l; i++){
			vres[i] = new Pixel(vec[i-1]);
		}
		return new LABVector(vres);
	}

	public LABVector pow(double p)
	{
		Pixel vres[] = Pixel.copy(vec);
		for(int i=0;i<l;i++){
			vres[i].L=(float)Math.pow(vec[i].L,p);
			vres[i].A=(float)Math.pow(vec[i].A,p);
			vres[i].B=(float)Math.pow(vec[i].B,p);
		}
		return new LABVector(vres);
	}

	public LABVector abs()
	{
		Pixel[] vres = Pixel.copy(vec);
		for(int i=0;i<l;i++){
			vres[i].abs();
		}
		return new LABVector(vres);
	}
	
	public static LABVector[][] approximateVector(LABVector[] previous){
	//[][0]:side	[][1]:upward	[2]:downwrad
	//[0][] close-close		[1][]:far-close 	[2][]:close-far		[3][]:far-far
		LABVector[][]result = new LABVector[4][3];
		for (int i=0; i<4; i++){
			int prevIndex = i%2;
			int distance = 2;
			switch (i){
			case 0:
				distance = 1;
				break;
			case 3:
				distance = 3;
				break;
			}
			LABVector dx = previous[prevIndex].sub(previous[prevIndex+1]);
			LABVector dyUp = previous[prevIndex].getDy();
			LABVector dyDown = dyUp.mult(-1).shiftDown();
			result[i][0] = previous[prevIndex].add(dx.mult(distance));
			result[i][1] = previous[prevIndex].shiftUp().add(dx.shiftUp().mult(distance)).add(dyUp.shiftUp());
			result[i][2] = previous[prevIndex].shiftDown().add(dx.shiftDown().mult(distance)).add(dyDown.shiftDown());
		}
		
		return result;
		
	}

	public void print(){
		for (int i=0; i<l; i++){
			System.out.print(i+": ");
			System.out.print(vec[i].L+" ");
			System.out.print(vec[i].A+" ");
			System.out.print(vec[i].B+" ");
			System.out.println("   burnt = "+vec[i].burnt);
		}
		System.out.println("sum = "+sum());
		System.out.println();
	}
	public static void print(LABVector[][] v){
		for (int i=0; i<v.length; i++){
			for (int j=0; j<v[0].length; j++){
				System.out.println("i = "+i+"   j = "+j);
				v[i][j].print();
			}
		}
	}
	
	public int countReal(){
		int count = 0;
		for (int i=0; i<l; i++){
			if (!vec[i].burnt){
				count = count + 1;
			}
		}
		return count;
	}
}
