package puzzle;

public class ConvColor {
	public static void rgb2labOld(int R, int G, int B, double []lab) {
		//http://www.brucelindbloom.com
		  
		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		   
		float Xr = 0.964221f;  // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;
		
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (float) Math.pow(xr, 1/3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (float) Math.pow(yr, 1/3.);
		else
		fy = (float) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (float) Math.pow(zr, 1/3.);
		else
			fz = (float) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		lab[0] = ((2.55*Ls + .5)/255.0*100.0);//*65280
		lab[1] = (((as + .5)+128)/255.0*255.0);//*65280
		lab[2] = (((bs + .5)+128)/255.0*255.0);//*65280
	
	} 
	
	public static void rgb2lab(int R, int G, int B, double[]lab){
		double RR,GG,BB, X, Y, Z, fX, fY, fZ,T,Y3,L,a,b;
		
		//Threshold
		T = 0.008856;
		
		//RGB scaling
		RR = R/255.f;
		GG = G/255.f;
		BB = B/255.f;
		
		//RGB to XYZ
		
		X = 0.412453*RR + 0.35758*GG + 0.180423*BB;
		Y = 0.212671*RR + 0.71516*GG + 0.072169*BB;
		Z = 0.019334*RR + 0.119193*GG + 0.950227*BB;
		
		X = X/0.950456;
		Z = Z/1.088754;
		
		if (X>T){
			fX = Math.pow(X,(1/3.f));
		}else{
			fX = (7.787*X + 16.f/116.f);
		}
		
		//Compute L
		Y3 = Math.pow(Y,(1/3.f));
		if (Y>T){
			fY = Y3;
			L = (116*Y3 - 16.f);
		}else{
			fY = (7.787*Y + 16.f/116.f);
			L = (903.3*Y);
		}
		if (Z>T){
			fZ = Math.pow(Z,(1/3.f));
		}else{
			fZ = (7.787*Z + 16.f/116.f);
		}
		
		//Compute a and b	
		a = 500*(fX - fY);
		b = 200*(fY - fZ);
		
		lab[0] = L;
		
//		lab[1] = 100*(a+86.181)/184.416;//For Normalizing - Otherwise delete this!
//		lab[2] = 100*(b+107.862)/202.338;; //For Normalizing - Otherwise delete this!
		
		//Without normalizing
		lab[1] = a;
		lab[2] = b;
	}
	public static void lab2rgb(double L, double a, double b, int[]rgb)
	{
		if (L==0 && a==0 && b==0)
		{rgb[0] = 0; rgb[1] = 0; rgb[2] = 0; return;}
		
//		a = 184.416*a/100 - 86.181; //For Normalizing - Otherwise delete this!
//		b = 202.338*b/100 - 107.862;//For Normalizing - Otherwise delete this!

	    double X, Y, Z, fX, fY, fZ,t1,t2,RR,GG,BB;
	    
	    t1 = 0.008856;
	    t2 = 0.206893;
	    
	    //Compute Y
	    fY = Math.pow((L+16)/116.f,3);
	    if (fY>t1){
	    	Y=fY;
	    	fY = Math.pow(fY,1.f/3.f);
	    	
	    }else{
	    	fY=L/903.3;
	    	Y = fY;
	    	fY = fY*7.787 + 16.f/116.f;
	    }
	    
	    
	  //Compute X
	    fX = a/500 + fY;
	    if (fX>t2){
	    	X = Math.pow(fX,3);
	    }else{
	    	X = (fX - 16.f/116.f)/7.787;
	    }
	    
	  //Compute Z
	    fZ = fY - b/200.f;
	    
	    if (fZ>t2){
	    	Z = Math.pow(fZ, 3);
	    }else{
	    	Z = (fZ - 16.f/116.f)/7.787;
	    }
	    

	    X *= 0.950456;
	    Z *= 1.088754;
	    

	    RR =  (3.240479*X - 1.537150*Y - 0.498535*Z);
	    GG = (-0.969256*X + 1.875992*Y + 0.041556*Z);
	    BB =  (0.055648*X - 0.204043*Y + 1.057311*Z);

	    RR = Math.max(0,RR);
	    RR = Math.min(1,RR);
	    GG = Math.max(0,GG);
	    GG = Math.min(1,GG);
	    BB = Math.max(0,BB);
	    BB = Math.min(1,BB);
	    
	    rgb[0] = (int)Math.round(255*RR);
	    rgb[1] = (int)Math.round(255*GG);
	    rgb[2] = (int)Math.round(255*BB);
	}
	
	public static void main(String[] args) {
		int[]rgb = {255,0,0};
		System.out.println("rgb: "+ rgb[0]+" "+rgb[1]+" "+rgb[2]);
		double lab[]=new double[3];
		rgb2lab(rgb[0],rgb[1],rgb[2],lab);
		System.out.println("lab: "+lab[0]+" "+(lab[1])+" "+(lab[2]));
		lab2rgb(lab[0],lab[1],lab[2],rgb);
		System.out.println("rgb: "+ rgb[0]+" "+rgb[1]+" "+rgb[2]);	
		System.out.println();
		
		lab[0] = 50; lab[1] = 100; lab[2] = -100;
		System.out.println("lab: "+lab[0]+" "+(lab[1])+" "+(lab[2]));
		lab2rgb(lab[0],lab[1],lab[2],rgb);
		System.out.println("rgb: "+ rgb[0]+" "+rgb[1]+" "+rgb[2]);
		rgb2lab(rgb[0],rgb[1],rgb[2],lab);
		System.out.println("lab: "+lab[0]+" "+(lab[1])+" "+(lab[2]));
	}
}
