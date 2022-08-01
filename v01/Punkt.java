package v01;

import java.awt.Color;

/** Punkt na R^2
 * 2007-06-08
 * 		utworzenie klasy
 * 		mniejsze lub wi�ksze pozniejsze poprawki "przy okazji" 
 * @author Jaros�aw Piersa
 */
// TODO pomyslec nad zroznicowaniem dokladnosci wykrywania krawedzi
// intensywnie pomyslec nad wprowadzeniem indeksow id

public class Punkt {
	private double x, y;
	private int typ = TYP_INNY;
	private double przeplyw = 0;
	
	public static final double MAX_X = 300, MAX_Y =300;
	public static final int TYP_ZRODLO = 1;
	public static final int TYP_ODBIORCA = 2;
	public static final int TYP_INNY = 3;
	// z pewnych powodow nie chcemy miec zbyt blisko lezacych punktow
	private static final double DOMYSLNA_MIN_ODLEGLOSC = 0.5;
	private static final double ZWIEKSZONA_MIN_ODLEGLOSC = 4;
	private static double MIN_ODLEGLOSC = 0.5;
	public static final Color KOLOR_PUNKTU[] = {Color.GRAY, Color.GREEN, Color.RED, Color.BLUE};
	
	public Punkt(double x, double y){
		setX(x);
		setY(y);
	}	//konstuktor
	
	public Punkt(double x, double y, int typ){
		setX(x);
		setY(y);
		setTyp(typ);
	}	//konstuktor
	
	public Punkt(double x, double y, int typ, double przeplyw){
		setX(x);
		setY(y);
		setTyp(typ);
	}	//konstuktor
	
	public Punkt(){
		setX(Math.random()* MAX_X);
		setY(Math.random()* MAX_Y);
	}	//  konstruktor
	
	public Punkt(int typ){
		setX(Math.random()* MAX_X);
		setY(Math.random()* MAX_Y);
		setTyp(typ);
	}	// konstruktor

	/** zmienia dokladnosc metody equals przy porownywaniu punktow
	 * uzyteczne np przy obliczeniach pozycji punktow z klokniec na panel
	 * 
	 * stosowac z zachowaniem szczegolnej ostroznosci!!! 
	 * 
	 * @param arg0 true gdy punkty maja byc silnie rozrozniane
	 */
	public static void setPrecyzyjnePorownywaniePunktow(boolean arg0){
		if (arg0 == true)
			MIN_ODLEGLOSC = DOMYSLNA_MIN_ODLEGLOSC;
		else 
			MIN_ODLEGLOSC = ZWIEKSZONA_MIN_ODLEGLOSC;
	}	// precyzja porownywania
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		if (x>=0 && x<MAX_X)
			this.x = x;
	}
	
	public double getY() {
		return y;
	}

	public void setY(double y) {
		if (y>=0 && y< MAX_Y)
			this.y = y;
	}

	/** Przesuwa punkt o wektor (dx, dy)^t
	 * @param dx przesuniecie w poziomie
	 * @param dy przesuniecie w pionie
	 */
	public void przesun(double dx, double dy){
		setX(x + dx);
		setY(y + dy);
	}	// przesun
	
	public int getTyp() {
		return typ;
	}

	public void setTyp(int typ) {
		if (typ >=1 && typ <4)
			this.typ = typ;
	}

//	public double getPrzeplyw() {
//		return przeplyw;
//	}

	/** Przeplyw >=0
	 * @param przeplyw nowy przeplyw >=0
	 */
	public void setPrzeplyw(double przeplyw) {
		if (przeplyw >= 0)
			this.przeplyw = przeplyw;
		else {
			System.err.println("Punkt.setPrzeplyw(): argument dp < 0");
			przeplyw = 0;
		}	// else
	}	// setPrzeplyw
	
	/** zwieksza przeplyw o dp, nowy przeplyw przez wezel musie byn nieujemny
	 * @param dp
	 */
	public void dodajPrzeplyw(double dp){
		if (przeplyw + dp >=0 )
			przeplyw += dp;
		else{
			System.err.println("Punkt.dodajPrzeplyw(): argument dp < 0");
			przeplyw = 0;
		}	// else
	}	// dodajPrzeplyw

	/** porownanie z punktem q
	 * UWAGA dwa punkty sa uznawane za tozsame jezeli sa oddalone o mniej niz MIN_ODLEGLOSC!!!
	 * @param q punkt do porowania
	 */
	public boolean equals(Punkt q){
		if (odlegloscKw(this, q) <  MIN_ODLEGLOSC * MIN_ODLEGLOSC)
			return true;
		return false;
	}	// equals
	
	/** reprezentacja napisowa
	 */
	public String toString(){
		return String.format("(%3.0f,%3.0f)", x, y);
	}
	
	/** klonowanie punktu
	 * uzywac z zachowaniem szczegolnej ostroznosci, 
	 * klonowane punkty nie sa rozroznialne w grafie!!
	 */
	public Punkt clone(){
		return new Punkt(x, y, typ, przeplyw);
	}
	
	/** odleglosc euklidesowa miedzy punktami a i b
	 * @param a, b punkty
	 * @return odleglosc(a, b)
	 */
	public static double odleglosc(Punkt a, Punkt b){
		return (Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)));
	}	// odleglosc
	
	/** kwadrat odleglosci euklidesowej miedzy punktami a i b
	 * nie ma sqrt wiec jest szybszy (do porownywania)
	 * @param a, b punkty
	 * @return odleglosc(a, b)
	 */
	protected static double odlegloscKw(Punkt a, Punkt b){
		return ((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
	}	// odleglosc
}	// class
