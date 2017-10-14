package utils;

import java.awt.geom.Point2D;

public class Hexagon {
	
	//coordinate dei punti
	private double Ox, Oy, Cx, Fx;
	private double Ax, Ay, Bx, Ex,Dy,Dx;
	//raggio
	private double r;
	//id
	private int id;
	
	public Hexagon(int id, double Ox, double Oy, double r) {
		this.id = id;
		this.Ox = Ox;
		this.Oy = Oy;
		//primi due punti (x+l,y) (x-l, y)
		//(Acx,Oy) e (Abx,Oy)
		this.Cx = Ox+r;
		this.Fx = Ox-r;
		this.r = r;
		costruisciEsagono();
	}
	
	private void costruisciEsagono() {
		//punti di intersezione
		//cerchio nostro esagono
		double a = -2*Ox;
		double b = -2*Oy;
		double c = Ox*Ox + Oy*Oy -(r*r);
		//cerchio per trovare i primi 2 punti
		double a1 = -2*Cx;
		double b1 = -2*Oy;
		double c1 = Cx*Cx + Oy*Oy -(r*r);
		double[] f = intersezioneCirconferenze(a,b,c,a1,b1,c1);
		//(Aex,Ady) e (Agx, Afy)
		Bx = f[0];
		Ay = f[3];
		Dx = f[2];
		Dy = f[1];
		System.out.println("first = "+Bx);
		System.out.println(Ay);
		System.out.println(Dx);
		System.out.println(Dy);
		//cerchio per trovate i restanti 2 punti
		double a2 = -2*Fx;
		double b2 = -2*Oy;
		double c2 = Fx*Fx + Oy*Oy -(r*r);
		f = intersezioneCirconferenze(a,b,c,a2,b2,c2);
		//(Adx,Ady) e (Afx,Afy)
		Ax = f[0];
		Ay = f[3];
		Ex = f[2];
		Dy = f[1];
		System.out.println("second = "+Ax);
		System.out.println(Ay);
		System.out.println(Ex);
		System.out.println(Dy);
	}
	
	private double[] intersezioneCirconferenze(double a, double b, double c, double a1, double b1, double c1) {
		double t = a-a1;
		double p = b-b1;
		double q = c-c1;
		
		double k = p*p + t*t;
		double j = 2*p*q - a*p*t + b*t*t;
		double i = q*q - a*t*q + c*t*t;
		double y1 = (-j-Math.sqrt(j*j-4*k*i))/(2*k);
		double y2 = (-j+Math.sqrt(j*j-4*k*i))/(2*k);
		double x1 = (-y1*p - q)/t;
		double x2 = (-y2*p - q)/t;
		double [] res = {x1, y1, x2, y2};
		return res;
	}
	
	public boolean appartenenzaPunto(double x, double y) {
		boolean a = (y <= Ay);
		boolean b = (y >= Dy);
		boolean c = (y-Oy)/(Dy-Oy) - (x-Fx)/(Ex-Fx) <= 0;
		boolean d = (y-Ay)/(Oy-Ay) - (x-Ax)/(Fx-Ax) >= 0;
		boolean e = (y-Ay)/(Oy-Ay) - (x-Cx)/(Cx-Bx) >= 0;
		boolean f = (y - Oy)/(Dy-Oy) - (x-Cx)/(Dx-Cx) <= 0;
		return a && b && c && d && e && f;
	}
	
	public int getID(){return id;}
	
	public Point2D getA(){
		return new Point2D.Double(Ax,Ay);
	}
	
	public Point2D getB(){
		return new Point2D.Double(Bx,Ay);
	}
	
	public Point2D getC(){
		return new Point2D.Double(Cx,Oy);
	}
	
	public Point2D getD(){
		return new Point2D.Double(Dx,Dy);
	}
	
	public Point2D getE(){
		return new Point2D.Double(Ex,Dy);
	}
	
	public Point2D getF(){
		return new Point2D.Double(Fx,Oy);
	}
	
	public Point2D getO(){
		return new Point2D.Double(Ox,Oy);
	}
	
	
/*	public static void main(String[] args) {
		Hexagon hex = new Hexagon(50.0,50.0,7.0);
		System.out.println(hex.appartenenzaPunto(20.0, 20.0));
	}*/
}
