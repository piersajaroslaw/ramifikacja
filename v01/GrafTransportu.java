package v01;

//import java.util.Random;


/**
 * 
 * Klasa posiadajaca problem tj liste punktow na R^2 wraz z wartosciami transportu
 * oraz graf polaczen
 * 
 * mapa dostaw i graf sa w scislej zaleznosci
 * 
 * @author Jarosaw Piersa
 * 2007-06-14
 * 		utworzenie
 * 2007-09-18
 * 		krok algorytmu:
 * 		przesuwanie, laczenie, rozlaczanie wierzcholkow
 * 2007-09-19
 * 		krok algorytmu:
 * 		przesuwanie o N(0, sigamKw)^2
 * 2007-10-03
 * 		laczenie wierzcholkow przyjmowane jest z prawdopodobienstwem zaleznym od zmiany kosztu
 * 2008-01-03
 *		GT zostal zamkniety a dalsze modyfikacje prowadzone sa w 
 *		v02.wejscieDoAlgorytmu.java 
 * 
 * 
 * TODO inne modyfikacje jeszcze jakies
 * TODO petla iteracyjna tu a nie z poziomu Okna
 * TODO pstwa modyfikacji (rozdzielanie) uzaleznic od zmiany kosztu <do zrobienia w Graf.java>
 * TODO jakis Component albo Jpanel pozwalajacy na wyswietlanie i nadzorowanie grafu o md
 * 			  ew regulaca sily z jaka sa pryciagane
 * 			+ co ile krokow odswierzc mape
 */
public class GrafTransportu {
	
	private MapaDostaw mapa = null;
	private Graf graf = null;
//	private Random r = null;
	private int t = 1;
	private int T = 1500;
	
	// promien w jakim wyszukiwany jest wierzcholek
	private double promien = 40;
	// pstwo mutacji - laczenie wierzcholkow
//	private double progLaczenie = .2;
//	private double progRozdzielanie = .01;
//	private double progPrzesowanie = .33;
	

	public GrafTransportu(MapaDostaw m) {
		super();
//		r = new Random(System.currentTimeMillis());
		mapa = m;
		graf = new Graf(m);
	}	// konstruktor


	public Graf getGraf() {
		return graf;
	}


	public MapaDostaw getMapa() {
		return mapa;
	}
	
	public void setMapa(MapaDostaw m) {
		mapa = m;
		graf = new Graf(m);
		t = 1;
		MapaOdleglosci.setPromien(40);
	}	// setMapa()

	
	
	
	public int getT() {
		return t;
	}	// getT()


	public int getMaxT() {
		return T;
	}	// getMaxT()

	public void  setMaxT(int maxT) {
		if (maxT<=10){
			System.err.println("GrafTrans.setMaxT(): argument zbyt maly");
			return;
		}	// if
		T = maxT;
		if (t>=T)
			t = T-1;
			
	}	// getMaxT()

	/** wykonuje jeden krok petli
	 * na razie tylko przeciaganie
	 */
	public void nastepnyKrok(){
		if (t+1 <T){
			t++;
		}	// if
		
		if (t%66 == 0){
			double pr  = MapaOdleglosci.getPromien() - 1;
			if (pr < this.promien/2)
				pr = this.promien/2;
			MapaOdleglosci.setPromien( pr );
			graf.obliczMapeOdleglosci();
		}	// if
		
		
		graf.losowaModyfikacjaPrzesuniecieKohonena(t, T);
		graf.losowaModyfikacjaRozlaczWierzcholki(t*.01);
		graf.losowaModyfikacjaPolaczWierzcholki(t*.01, promien);
		graf.losowaModyfikacjaPrzesuniecieLosowe(t*.1);
		graf.losowaModyfikacjaUsuniecieWierzcholka();
		graf.losowaModyfikacjaDodanieWierzcholka();
		
		graf.usunAnomalie();
		graf.obliczKoszt();
	}	// nastepnyKrok
	
	
//	private void modyfikacjaPrzesunWierzcholek(){
//		Punkt cel = graf.getMapaOdleglosci().losujPunkt();
//		Punkt p   = graf.znajdzNajblizszyWierzcholek(cel);
//		graf.przesunWierzcholkiKohonen(p, cel, t, T);
//	}	// modyfikacjaPrzesunWierzcholek()
//	
	
	
}	// class GrafTransportu
