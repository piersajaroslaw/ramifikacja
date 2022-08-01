package v01;

import v02.ModywikacjaGrafuException;

/** 
 * 2007-06-08
 * 		wstepny szic
 * 2007-09-02
 * 		lekka zmiana struktury, wlaczaenie klasy do projektu
 * 2007-09-13
 * 		dodano regulacje dokladnosci porownywania punktow
 * 		Stosowac z zachowaniem szczegolnej ostroznosci
 * 
 * @author Jaros�aw Piersa
 *
 */
public class Krawedz {
	private Punkt a, b;
	private int przeplyw = 0;
	private static final double progBledu = .00001;
	
	public Krawedz(Punkt a, Punkt b){
		this.a = a;
		this.b = b;
	}
	
	public Krawedz(Punkt a, Punkt b, int przeplyw){
		this.a = a;
		this.b = b;
		setPrzeplyw(przeplyw);
	}
	
	public Krawedz clone(){
		return new Krawedz(a.clone(), b.clone(), przeplyw);
	}	// clone
	
	/**
	 * @return dlugosc krawedzi
	 */
	public double getDlugosc(){
		return Punkt.odleglosc(a,b);
	}	// getDlugosc
	
	/**
	 * @return pierwszy wierzcholek krawedzi (zrodlowy)
	 */
	public Punkt getA() {
		return a;
	}

	public void setA(Punkt a) {
		this.a = a;
	}

	/**
	 * @return drugiwierzcholek krawezi (koncowy)
	 */
	public Punkt getB() {
		return b;
	}

	public void setB(Punkt b) {
		this.b = b;
	}
	
	/**
	 * @return przeplyw przez krawedz
	 */
	public int getPrzeplyw() {
		return przeplyw;
	}

	/** Przeplyw >=0
	 * @param przeplyw nowy przeplyw >=0
	 */
	public void setPrzeplyw(int przeplyw) {
		if (przeplyw >= 0)
			this.przeplyw = przeplyw;
		else {
			System.err.println("Krawedz.setPrzeplyw(): argument dp < 0");
			przeplyw = 0;
		}	// else
	}	// setPrzeplyw
	

	/** zwieksza przeplyw o dp, nowy przeplyw przez wezel musie byn nieujemny
	 * @param dp
	 */
	public void zwiekszPrzeplyw(int dp){
		if (przeplyw + dp >=0 ){
			przeplyw += dp;
		} else {
			if (Math.abs(przeplyw + dp) > progBledu){
				ModywikacjaGrafuException.wypiszStanWywolania("Krawedz.zwiekszPrzeplyw(): przeplyw + dp = "+ (przeplyw + dp) +" < 0");
			}	// if
			przeplyw = 0;
		}	// else
	}	// dodajPrzeplyw
	
	
	public String toString(){
		return String.format("[ %s   %s: %f ]", a, b, przeplyw);
	}
}	// class Krawedz
