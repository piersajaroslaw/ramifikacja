package v01;

import java.util.Random;

/** Klasa do przetrzymywania odleglosci punktu na R^2 od grafu
 * 
 * 
 * 2007-09-15
 * 		utworzenie
 * 2007-09-18
 * 		losowanie punktow
 * 2007-09-19
 * 		tworzenie nowej mapy z uwzglednieniem poprzedniej (srednia)
 * 
 * TODO: pomyslec nad losowaniem z vectora zamiast z plaszczyzny - szybciej
 * 
 * @author Jaros�aw Piersa
 *
 */
public class MapaOdleglosci {
	private static double promien = 30;
	private Random r; 
	private float mapa[][] = null;
	
	public MapaOdleglosci(){
		super();
		r = new Random(System.currentTimeMillis());
		mapa = new float[(int)Punkt.MAX_X][(int)Punkt.MAX_Y];
	}	// konstruktor

	public MapaOdleglosci clone(){
		MapaOdleglosci m = new MapaOdleglosci();
		m.r = new Random(System.currentTimeMillis());
		m.mapa = new float[mapa.length][mapa[0].length];
		for (int i=0; i< mapa.length; i++)
			for (int j=0; j<mapa[0].length; j++)
				m.mapa[i][j] = mapa[i][j];
		return m;
	}	// clone
	
	public static double getPromien() {
		return promien;
	}	// getPromien

	public static void setPromien(double promien) {
		if (promien >= 0)
			MapaOdleglosci.promien = promien;
	}	// setPromien

	/** Package Private
	 * @return zwraca tablice
	 */
	public float[][] getMapa() {
		return mapa;
	}
	
	public void zaznaczOdleglosciWokolPunktu(Punkt p){
		for (int i= (int)-promien; i<promien; i++){
			int x = (int)  p.getX() + i;
			if (x<0|| x>=(int) Punkt.MAX_X)
				continue;
			
			for (int j= (int)-promien; j<=promien; j++){
				int y = (int)  p.getY() + j;
				if (y<0|| y>=(int) Punkt.MAX_Y)
					continue;
				
				if (i*i+j*j >= promien*promien)
					continue;
				
				//mapa[x][y] += 1- (Math.sqrt(i*i+j*j))/promien;
				
				if (i==0 && j==0)
					mapa[x][y] += 1;
				else
					mapa[x][y] += 2/(float)Math.sqrt(i*i+j*j);

				
			}	// for j
		}	// for i
	}	// zaznadzOdleglosci
	
	
	/** losuje punkt z zaznaczonego obszaru
	 * @return losowy punkt z zaznaczonego obszaru
	 */
	public Punkt losujPunkt(){
		int i = 0;
		while (true){
			i++;
			int x = r.nextInt((int) Punkt.MAX_X);
			int y = r.nextInt((int)Punkt.MAX_Y);
			double d = r.nextDouble();
			if (d < mapa[x][y]){
				return new Punkt(x,y);
			}	// fi
		}	// while
	}	// losujPunkt()
	
	/** wyciaga srednia z dwuch map
	 * zapisuje wynik w biezacym obiekcie this!!!
	 * @param m druga mapa
	 */
	public void ujednolicenie(MapaOdleglosci m){
		if (mapa.length != m.mapa.length){
			System.err.println("MapaOdl.Ujednolicenie(): niezgodny wymiar");
		}	// if
		if (mapa[0].length != m.mapa[0].length){
			System.err.println("MapaOdl.Ujednolicenie(): niezgodny wymiar");
		}	// if
		
		int x = mapa.length;
		int y = mapa[0].length;
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				mapa[i][j] = (mapa[i][j] + m.mapa[i][j]) / 2;
			}	// for i
		}	// for j
	}	// ujednolicenie()
	
}	// mapaOdleglosci
