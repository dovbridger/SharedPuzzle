package oldPuzzle;


public class MGC {
	static float calc(LABVector p1_l1, LABVector p1_l2, LABVector p2_l1, LABVector p2_l2){
		LABVector gL = p1_l1.sub(p1_l2);
		LABVector gLR = p1_l1.sub(p2_l1);
		double mueGL[][]=mean(gL);
		double covGL[][]=cov(gL,mueGL);
		double invcovGL[][]=inv(covGL);
		float res=mult(gLR.sub(new LABVector(mueGL)),invcovGL);
		return res;
	}

	private static float mult(LABVector gLR, double[][] invcovGL) {
		float res=0;
		for(int i=0;i<28;i++){
			res+=
					gLR.vec[i][0]*(gLR.vec[i][0]*invcovGL[0][0]+gLR.vec[i][1]*invcovGL[1][0]+gLR.vec[i][2]*invcovGL[2][0])
					+gLR.vec[i][1]*(gLR.vec[i][0]*invcovGL[0][1]+gLR.vec[i][1]*invcovGL[1][1]+gLR.vec[i][2]*invcovGL[2][1])
					+gLR.vec[i][2]*(gLR.vec[i][0]*invcovGL[0][2]+gLR.vec[i][1]*invcovGL[1][2]+gLR.vec[i][2]*invcovGL[2][2]);
		}
		return res;
	}

	private static double[][] inv(double[][] covGL) {
		double invcovGL[][]=new double[3][3];
		double det=(covGL[0][0]*(covGL[1][1]*covGL[2][2]-covGL[1][2]*covGL[2][1])
				-covGL[0][1]*(covGL[2][2]*covGL[0][1]-covGL[1][2]*covGL[2][0])
				+covGL[0][2]*(covGL[1][0]*covGL[2][1]-covGL[1][1]*covGL[2][0]));
		if(det<0.0000001)
			det=1;
		invcovGL[0][0]=(covGL[1][1]*covGL[2][2]-covGL[1][2]*covGL[2][1])/det;
		invcovGL[1][0]=-(covGL[1][0]*covGL[2][2]-covGL[1][2]*covGL[2][0])/det;
		invcovGL[2][0]=(covGL[1][0]*covGL[2][1]-covGL[1][1]*covGL[2][0])/det;
		invcovGL[0][1]=-(covGL[0][1]*covGL[2][2]-covGL[0][2]*covGL[2][1])/det;
		invcovGL[1][1]=(covGL[0][0]*covGL[2][2]-covGL[0][2]*covGL[2][0])/det;
		invcovGL[2][1]=-(covGL[0][0]*covGL[2][1]-covGL[0][1]*covGL[2][0])/det;
		invcovGL[0][2]=(covGL[0][1]*covGL[1][2]-covGL[0][2]*covGL[1][1])/det;
		invcovGL[1][2]=-(covGL[0][0]*covGL[1][2]-covGL[0][2]*covGL[1][0])/det;
		invcovGL[2][2]=(covGL[0][0]*covGL[1][1]-covGL[0][1]*covGL[1][0])/det;
		return invcovGL;
	}

	private static double[][] cov(LABVector gL, double[][] mueGL) {
		double covGL[][]=new double[3][3];
		LABVector gL2 = gL.sub(new LABVector(mueGL)); 
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				for(int k=0;k<28;k++){
					covGL[i][j]+=gL2.vec[k][i]*gL2.vec[k][j];
				}
				covGL[i][j]/=28;
			}
		}
		return covGL;
	}

	private static double[][] mean(LABVector gL) {
		float m1=0,m2=0,m3=0;
		for(int i=0;i<28;i++){
			m1+=gL.vec[i][0];
			m2+=gL.vec[i][1];
			m3+=gL.vec[i][2];
		}
		m1/=28;
		m2/=28;
		m3/=28;
		double mue[][]=new double[28][3];
		for(int i=0;i<28;i++){
			mue[i][0]=m1;
			mue[i][1]=m2;
			mue[i][2]=m3;
		}
		return mue;
	}
}
