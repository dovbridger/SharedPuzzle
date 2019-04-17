package oldPuzzle;

public class LABVector {
	double vec[][];
	int l;

	public LABVector(double[][] _vec) {
		vec=_vec;
		l=vec.length;
	}
	
	public LABVector blur()
	{
		double vres[][]=new double[l][3];
		vres[0][0]=(vec[0][0]+vec[1][0])/2;
		vres[0][1]=(vec[0][1]+vec[1][1])/2;
		vres[0][2]=(vec[0][2]+vec[1][2])/2;
		for(int i=1;i<l-1;i++){
			vres[i][0]=(vec[i-1][0]+vec[i][0]+vec[i+1][0])/3;
			vres[i][1]=(vec[i-1][1]+vec[i][1]+vec[i+1][1])/3;
			vres[i][2]=(vec[i-1][2]+vec[i][2]+vec[i+1][2])/3;
		}
		vres[l-1][0]=(vec[l-2][0]+vec[l-1][0])/2;
		vres[l-1][1]=(vec[l-2][1]+vec[l-1][1])/2;
		vres[l-1][2]=(vec[l-2][2]+vec[l-1][2])/2;
		return new LABVector(vres);
	}
	
	public LABVector sub(LABVector v2)
	{
		double vres[][]=new double[l][3];
		for(int i=0;i<l;i++){
			vres[i][0]=vec[i][0]-v2.vec[i][0];
			vres[i][1]=vec[i][1]-v2.vec[i][1];
			vres[i][2]=vec[i][2]-v2.vec[i][2];
		}
		return new LABVector(vres);
	}
	
	public LABVector sub2(LABVector v2)
	{
		double vres[][]=new double[l][3];
		for(int i=0;i<l;i++){
			vres[i][0]=vec[i][0]-v2.vec[i][0];
			if(vres[i][0]>20)
				vres[i][0]=0;
			vres[i][1]=vec[i][1]-v2.vec[i][1];
			if(vres[i][1]>20)
				vres[i][1]=0;
			vres[i][2]=vec[i][2]-v2.vec[i][2];
			if(vres[i][2]>20)
				vres[i][2]=0;
		}
		return new LABVector(vres);
	}
	
	public LABVector add(LABVector v2)
	{
		double vres[][]=new double[l][3];
		for(int i=0;i<l;i++){
			vres[i][0]=vec[i][0]+v2.vec[i][0];
//			if(vres[i][0]<0)
//				vres[i][0]=0;
//			if(vres[i][0]>65535)
//				vres[i][0]=65535;
			vres[i][1]=vec[i][1]+v2.vec[i][1];
//			if(vres[i][1]<0)
//				vres[i][1]=0;
//			if(vres[i][1]>65535)
//				vres[i][1]=65535;
			vres[i][2]=vec[i][2]+v2.vec[i][2];
//			if(vres[i][2]<0)
//				vres[i][2]=0;
//			if(vres[i][1]>65535)
//				vres[i][1]=65535;
		}
		return new LABVector(vres);
	}
	
	public double sum()
	{
		double sum1=0,sum2=0,sum3=0;
		for(int i=0;i<l;i++){
			sum1+=vec[i][0];
			sum2+=vec[i][1];
			sum3+=vec[i][2];
		}
		return (sum1+sum2+sum3);//3*Math.max(sum1,sum2/2+sum3/2);
	}
	
	public double avgGrad()
	{
		double sum=0;
		for(int i=0;i<l-1;i++){
			sum+=Math.abs(vec[i][0]-vec[i+1][0]);
			sum+=Math.abs(vec[i][1]-vec[i+1][1]);
			sum+=Math.abs(vec[i][2]-vec[i+1][2]);
		}
		return sum/l/3;
	}

	public LABVector pow(double p)
	{
		double vres[][]=new double[l][3];
		for(int i=0;i<l;i++){
			vres[i][0]=Math.pow(vec[i][0],p);
			vres[i][1]=Math.pow(vec[i][1],p);
			vres[i][2]=Math.pow(vec[i][2],p);
		}
		return new LABVector(vres);
	}

	public LABVector abs()
	{
		double vres[][]=new double[l][3];
		for(int i=0;i<l;i++){
			vres[i][0]=Math.abs(vec[i][0]);
			vres[i][1]=Math.abs(vec[i][1]);
			vres[i][2]=Math.abs(vec[i][2]);
		}
		return new LABVector(vres);
	}
	
	public static void main(String[] args) {
		double a[][]=new double[3][2];
		a[0][0]=1;
		a[1][0]=1;
		a[2][0]=1;
		a[0][1]=1;
		a[1][1]=1;
		a[2][1]=1;
		new LABVector(a);
	}
}
