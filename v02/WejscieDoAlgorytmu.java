package v02;

//import java.util.Random;

import v01.MapaDostaw;
import v01.MapaOdleglosci;


/**
 * 
 * Klasa posiadajaca problem tj liste punktow na R^2 wraz z wartosciami transportu
 * oraz graf polaczen
 * 
 * mapa dostaw i graf sa w scislej zaleznosci
 * 
 * @author Jarosaw Piersa
 * 2008-01-03
 * 		utworzenie (powstalo z v01.GrafTransportu)
 * 2008-02-23
 * 		dorzucono temperature jako modyfikowalna i sprawdzalna
 * 
 */
public class WejscieDoAlgorytmu {
	
	private MapaDostaw mapa = null;
	private GrafWrazZPrzeplywem graf = null;
//	private Random r = null;
	private int t = 1;
	private int maxIteracjiKohonen = 1000;
	private final double  temperaturaMAX=1000;;
	private double temperatura = 0.01;
	
	private double promien = 40;
	private boolean czyDopuszczoneLaczenie = true;
	private boolean czyDopuszczonePrzesuwanieKohonena = true;
	private boolean czyDopuszczonePrzesuwanieLosowe = true;
	private boolean czyDopuszczoneModyfikowaniePrzeplywu = true;
	private boolean czyDopuszczoneModyfikowanieRozgalezien = true;
	
	
	private double skalarTemperaturyPrzyPrzesunieciach = 0.5;

	public WejscieDoAlgorytmu(MapaDostaw m) {
		super();
//		r = new Random(System.currentTimeMillis());
		mapa = m;
		graf = new GrafWrazZPrzeplywem(m);
	}	// konstruktor

	/* **********************************************
	 * 
	 *  DOSTEP DO POL KLASY
	 * 
	 * **********************************************/

	public boolean isCzyDopuszczoneLaczenie() {
		return czyDopuszczoneLaczenie;
	}

	public void setCzyDopuszczoneLaczenie(boolean czyDopuszczoneLaczenie) {
		this.czyDopuszczoneLaczenie = czyDopuszczoneLaczenie;
	}

	public boolean isCzyDopuszczonePrzesuwanieKohonena() {
		return czyDopuszczonePrzesuwanieKohonena;
	}

	public void setCzyDopuszczonePrzesuwanieKohonena(
			boolean czyDopuszczonePrzesuwanieKohonena) {
		this.czyDopuszczonePrzesuwanieKohonena = czyDopuszczonePrzesuwanieKohonena;
	}

	public boolean isCzyDopuszczonePrzesuwanieLosowe() {
		return czyDopuszczonePrzesuwanieLosowe;
	}

	public void setCzyDopuszczonePrzesuwanieLosowe(
			boolean czyDopuszczonePrzesuwanieLosowe) {
		this.czyDopuszczonePrzesuwanieLosowe = czyDopuszczonePrzesuwanieLosowe;
	}

	public boolean isCzyDopuszczoneModyfikowaniePrzeplywu() {
		return czyDopuszczoneModyfikowaniePrzeplywu;
	}

	public void setCzyDopuszczoneModyfikowaniePrzeplywu(
			boolean czyDopuszczoneModyfikowaniePrzeplywu) {
		this.czyDopuszczoneModyfikowaniePrzeplywu = czyDopuszczoneModyfikowaniePrzeplywu;
	}

	public boolean isCzyDopuszczoneModyfikowanieRozgalezien() {
		return czyDopuszczoneModyfikowanieRozgalezien;
	}

	public void setCzyDopuszczoneModyfikowanieRozgalezien(
			boolean czyDopuszczoneModyfikowanieRozgalezien) {
		this.czyDopuszczoneModyfikowanieRozgalezien = czyDopuszczoneModyfikowanieRozgalezien;
	}

	public GrafWrazZPrzeplywem getGraf() {
		return graf;
	}


	public MapaDostaw getMapa() {
		return mapa;
	}
	
	public void setMapa(MapaDostaw m) {
		mapa = m;
		graf = new GrafWrazZPrzeplywem(m);
		t = 1;
		MapaOdleglosci.setPromien(promien);
	}	// setMapa()

	public double getTemperatura() {
		return temperatura;
	}


	public void setTemperatura(double temperatura) {
		if (temperatura>=.001 && temperatura <= temperaturaMAX)
			this.temperatura = temperatura;
	}


	public int getT() {
		return t;
	}	// getT()


	public int getMaxT() {
		return maxIteracjiKohonen;
	}	// getMaxT()

	public void  setMaxT(int maxT) {
		if (maxT<=10){
			System.err.println("GrafTrans.setMaxT(): argument zbyt maly");
			return;
		}	// if
		maxIteracjiKohonen = maxT;
			
	}	// getMaxT()

	
	public double getPromien() {
		return promien;
	}

	public void setPromien(double promien) {
		if (promien>0){
			this.promien = promien;
			MapaOdleglosci.setPromien(promien);
		}	//
	}	// setPromien

	/** wykonuje jeden krok petli
	 * na razie tylko przeciaganie
	 */
	public void nastepnyKrok(){
//		System.out.printf("nastkrok p ");
		t++;
		
		temperatura = temperatura + .01 <= temperaturaMAX ? temperatura + .001 : temperaturaMAX;
//		temperatura = Math.log(t+1);
		if (temperatura > temperaturaMAX){
			temperatura = temperaturaMAX;
		}	// if
		
		if (t%50 == 0){
//			double pr  = MapaOdleglosci.getPromien() - 1;
//			if (pr < this.promien/2)
//				pr = this.promien/2;
//			MapaOdleglosci.setPromien( pr );
			graf.obliczMapeOdleglosci();
		}	// if
		

		
		graf.losowaModyfikacjaUsuniecieWierzcholka();
		graf.losowaModyfikacjaDodanieWierzcholka();
		
//		System.out.printf(" 1 dod us ");
		
		if (czyDopuszczoneLaczenie){
			graf.losowaModyfikacjaPolaczWierzcholki(temperatura, promien);
			graf.losowaModyfikacjaRozlaczWierzcholki(temperatura);
		}	// fi
		
//		System.out.printf(" 2 pol rozl ");
		
		if (czyDopuszczoneModyfikowaniePrzeplywu){
			graf.losowaModyfikacaZmnienPrzeplyw(temperatura);
		}	// fi
		
//		System.out.printf(" 3  przeplyw ");
		
		if (czyDopuszczonePrzesuwanieKohonena){
			graf.losowaModyfikacjaPrzesuniecieKohonena(t, maxIteracjiKohonen);
		}	// if
		
//		System.out.printf(" 4 kohonen ");
		
		if (czyDopuszczonePrzesuwanieLosowe){
			graf.losowaModyfikacjaPrzesuniecieLosowe(temperatura * skalarTemperaturyPrzyPrzesunieciach);
		}	// if
		
		if (czyDopuszczoneModyfikowanieRozgalezien){
			graf.losowaModyfikacjaPrzesuniecieRozgalezienia(temperatura);
		}	// if
		
//		System.out.printf(" 5 rozgalezienia ");
		
		if (t % 5 ==0){
			graf.usunAnomalie();
		}	// if
		
		graf.obliczKoszt();
//		System.out.printf(" 6 anomalie  \n ");
	}	// nastepnyKrok
	
	
}	// class GrafTransportu
