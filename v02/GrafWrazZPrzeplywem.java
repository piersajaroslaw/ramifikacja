package v02;

import java.awt.Component;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.Vector;

import v01.Krawedz;
import v01.MapaDostaw;
import v01.Punkt;

/** Ten graf zawiera dodatkowo mape dostaw i MOZE ja modyfikowac!!
 * 
 * Rozszerza v01.Graf
 * Projekt zostal rozdzielony dalsze zmiany beda pisane TU 
 * (chyba ze cos przestanie tu dzialac to sie wroci do starego kodu)
 * 
 * 2008-01-03
 * 		utworzenie, szukanie scierzek i jednoznacznych sciezek, klonowanie obiektu
 * 		testowanie czy krawedz nalezy do dokladnie jednej sciezki (DJS)
 * 2008-01-04
 * 		modyfikowanie przeplywu <tu dodac tu odjac>, klonowanie liczenie kosztu
 * 2008-01-05
 * 		tworzenie nowych scierzek, poprawki, duzo poprawek
 * 2008-01-06
 * 		usuwanie sciezek o malym przeplywie
 * 2008-02-14
 * 		dorzucenie do kosztu skladowej rozbieznosci przeplywu faktycznego i oczekiwanego do odbiorcy
 * 2008-02-24
 * 		modyfikacja przeplywu gdy nie istnieje sciazka z i1 do i2 
 * 		wredne bylo...
 * 2008-05-19
 * 		z trzy dosc istotne korekty, ktore znacznie zwiekszyly stabilnosc
 * 
 * @author piersaj
 *
 * UWAGA do wersji 2.0
 * sprobowac aniholacji krawedzi 
 *
 */
public class GrafWrazZPrzeplywem extends v01.Graf implements Cloneable {
	private MapaDostaw mapaDostaw;
	private static double skalarKoszt1 = 1;
	private static double skalarKoszt2 = 1;
	private static int  d = 1;
	private static final double progBledu = .00001;
	
	public GrafWrazZPrzeplywem(MapaDostaw m){
		super(m);
		this.mapaDostaw = m;
		resetujKosztPoczatkowy();
	}	// konstruktor
	
	/* DOSTEP DO POL
	 */
	public MapaDostaw getMapaDostaw() {
		return mapaDostaw;
	}	// getmapaDostaw()
	
	public static double getD() {
		return d;
	}

	/** z pwenych powodow to D powinno byc potega 2 
	 * 0 <=d <1
	 */
	public static void setD(int d) {
		if (d>=0 && d<1)
			GrafWrazZPrzeplywem.d = d;
	}

	public static double getSkalarKoszt1() {
		return skalarKoszt1;
	}	// getSkalarKoszt2()
	
	public static void setSkalarKoszt1(double skalarKoszt1) {
		if (skalarKoszt1>=0)
			GrafWrazZPrzeplywem.skalarKoszt1 = skalarKoszt1;
	}	// setSkalarKoszt1()

	public static double getSkalarKoszt2() {
		return skalarKoszt2;
	}	// getSkalarKoszt2()

	public static void setSkalarKoszt2(double skalarKoszt2) {
		if (skalarKoszt2>=0)
			GrafWrazZPrzeplywem.skalarKoszt2 = skalarKoszt2;
	}	// setSkalarKoszt1()

	/* 
	 *  METODY PRZECIAZONE
	 */
	
	/** oblicza koszt grafu
	 * @overides v01.Graf.obliczKoszt()
	 * koszt = 
	 * 
	 * 		c1 * sum( e %in E,  dl(e)*przeplyw(e)^%alpha ) + 
	 * 		c2 * sum( z %in Zrodla, (faktycznyPrzeplyw(z) - zadanyPrzeplyw(z))^2  )
	 * 
	 */
	@Override
	public void obliczKoszt(){
		double k1 = obliczKoszt1();
		double k2 = obliczKoszt2();
		//System.out.printf("k1 =  %.2f  k2 =%.2f  sum =%.2f\n", k1, k2, k1+k2);
		koszt = skalarKoszt1 * k1 + skalarKoszt2 * k2;
	}	// obliczKoszt

	/** oblicza koszt i zwraca lancuch opisujacy koszt jako suma dwoch skladowych
	 * @return
	 */
	public String obliczKosztStr(){
		double k1 = obliczKoszt1();
		double k2 = obliczKoszt2();
		//System.out.printf("k1 =  %.2f  k2 =%.2f  sum =%.2f\n", k1, k2, k1+k2);
		koszt = skalarKoszt1 * k1 + skalarKoszt2 * k2;
		return String.format("Koszt = %.2f + %.2f = %.2f", skalarKoszt1 * k1, skalarKoszt2 * k2, koszt);
	}	// obliczKosztStr;
	
	/** liczy koszt krawedzi
	 * sum( e %in E,  dl(e)*przeplyw(e)^%alpha )
	 * @return skladowa koszu architektury grafu
	 */
	private double obliczKoszt1(){
		double kosztTmp = 0;
		try {
			for (Vector<Krawedz> v : sasiedzi){
				for (Krawedz k : v){
					kosztTmp += obliczKosztKrawedzi(k);
				}	// for Krawedz
			}	// for Vector
		}	catch (ConcurrentModificationException e){
			//System.err.println(e);
		}	// catch
		return kosztTmp;
	}	// obliczKoszt1
	
	/** liczy koszt odchylen faktycznych wyplywow ze zrodel od wyplywow zadanych
	 * sum( o %in odbiorcy, (faktycznyPrzyplywDo(o) - zadanyPrzyplyw(o))^2)
	 * @return skladowa wyplywu ze zrodel
	 */
	private double obliczKoszt2(){
		double koszt = 0;
		if (mapaDostaw==null){
			return 0;
		}	// fi
		
		for (int i=0; i<mapaDostaw.getIlOdbiorcow(); i++){
			double suma = 0;
			for (int j=0; j<mapaDostaw.getIlZrodel(); j++){
				suma += mapaDostaw.getTransport(j, i) /* * mapaDostaw.getIntensywnoscZrodla(j) */;
			}	// for j
			suma = (suma - mapaDostaw.getIntensywnoscOdbiorcy(i)) * (suma - mapaDostaw.getIntensywnoscOdbiorcy(i));
			koszt += suma;
		}	// for i
		return koszt;
	}	// oblicz koszt2()
	
	/** przeciaza v01.Graf.getPodgladGrafu()
	 * @return zwraca komponent reprezentujacy graf
	 */
	@Override  
	public Component getPodgladGrafu(){
		return new PanelWyswietlajacyGrafZPrzeplywem( this );
	}	// getPodglad()
	
	/** klonowanie obiektu
	 * uzywac z zachowaniem szczegolnej ostroznosci!
	 */
	public GrafWrazZPrzeplywem clone(){
		GrafWrazZPrzeplywem g = new GrafWrazZPrzeplywem(mapaDostaw);
		g.n = n;
		
		g.koszt = koszt;
		g.wierzcholki = new Vector<Punkt>();
		for (Punkt p : wierzcholki)
			g.wierzcholki.add(p.clone());
		
		
		g.sasiedzi = new Vector<Vector<Krawedz>>();
		for (int i=0; i< sasiedzi.size(); i++){
			g.sasiedzi.add(new Vector<Krawedz>());
			for (Krawedz k: sasiedzi.get(i))
				g.dodajKrawedz( g.indeks(k.getA()), g.indeks(k.getB()), k.getPrzeplyw());
		}	// for
		if (mapaOdl == null)
			g.mapaOdl = null;
		else
			g.mapaOdl = mapaOdl;
		// mapaOdl.clone()
		g.r = new Random(System.currentTimeMillis());
		g.kosztPoczatkowy = kosztPoczatkowy;
		
		g.mapaDostaw = mapaDostaw.clone();
		return g;
	}	// clone

	private void kopiujZ(GrafWrazZPrzeplywem g){
		n = g.n;
		koszt = g.koszt;
		wierzcholki = new Vector<Punkt>();
		for (Punkt p : g.wierzcholki)
			wierzcholki.add(p.clone());
		
		sasiedzi = new Vector<Vector<Krawedz>>();
		for (int i=0; i< g.sasiedzi.size(); i++){
			sasiedzi.add(new Vector<Krawedz>());
			for (Krawedz k: g.sasiedzi.get(i))
				dodajKrawedz( g.indeks(k.getA()), g.indeks(k.getB()), k.getPrzeplyw());
		}	// for
		if (g.mapaOdl == null)
			mapaOdl = null;
		else
			mapaOdl = g.mapaOdl;
		// mapa.odl.clone()
		kosztPoczatkowy = g.kosztPoczatkowy;
		mapaDostaw = g.mapaDostaw.clone();
//		skalarKoszt1 = GrafWrazZPrzeplywem.skalarKoszt1;
//		skalarKoszt2 = GrafWrazZPrzeplywem..skalarKoszt2;
	}	// kopiujZ()

	/** Zwraca sciezke z p1 do p2 lub null jezeli sciezka nie istnieje!
	 * Mozna stosowac bez sprawdzania istnienia jakiejkolwiek sciezki ale jest to malo wskazane
	 * @param p1 wierzcholek startowy
	 * @param p2 wierzcholek koncowy (p1 != p2)
	 * @return sciezka z p1 do p2 lub null
	 */
	public Vector<Punkt> getSciezka(Punkt p1, Punkt p2){
		if (!czyWierzcholekIstnieje(p1) || !czyWierzcholekIstnieje(p2) )
			return null;
		if (p1.equals(p2))
			return null;
		
		int tablicaPoprzednikow[] = new int[n];
		for (int i=0; i<n; i++){
			tablicaPoprzednikow[i] = -1;
		}	// for
		
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		
		Vector<Punkt> stos = new Vector<Punkt>();
		stos.add(p1);
		
		tablicaPoprzednikow[i1]= i1;
		
		// DSF
		while (stos.size()!=0){
			Punkt p = stos.elementAt(stos.size()-1);
			stos.remove(stos.size()-1);
			for (Krawedz k: getSasiedzi(indeks(p))){
				Punkt s = k.getB();
				if (tablicaPoprzednikow[indeks(s)]== -1){
					tablicaPoprzednikow[indeks(s)]= indeks(p);
					stos.add(s);
				}	
			}	// for sasiedzi
		}	// while
		
		if (tablicaPoprzednikow[i2] == -1){
			return null;
		}
		
		Vector<Punkt> ret = new Vector<Punkt>();
		ret.add(p2);
		int i = tablicaPoprzednikow[i2];
		while (i!=i1){
			ret.insertElementAt(getWierzcholek(i), 0);
			i = tablicaPoprzednikow[i];
		}	// while
		ret.insertElementAt(p1, 0);
		return ret;
	}	// getSciezka()
	
	
	/** czy istnieje dokladnie jedna sciezka z p1 do p2 
	 * NIESYMETRYCZNE!!!
	 * 
	 * @param p1 punkt startowy
	 * @param p2 punkt koncowy
	 * @return true wtw gdy istnieje dokladnie jedna sciezka z p1 do p2
	 * 		   jezeli p1==p2 zwraca false!
	 */
	public boolean czyIstniejeDokladnieJednaSciezka(Punkt p1, Punkt p2){
		if (czyIstniejeSciezka(p1, p2) == false){
			return false;
		}	// if
		if (p1.equals(p2))
			return false;
		
		Vector<Punkt> v = getSciezka(p1, p2);
		for (int i=0; i<v.size()-1; i++){
			GrafWrazZPrzeplywem g = this.clone();
			int indeks1 = indeks(v.get(i));
			int indeks2 = indeks(v.get(i+1));
			g.usunKrawedz(indeks1, indeks2);
			if (g.czyIstniejeSciezka(p1, p2) == true){
				return false;
			}	// fi
			g = null;
		}	// for
		return true;
	}	// czyIstniejeDokladnieJednaSciezka()

	
	
	/** 
	 * @param k testowana krawedz
	 * @return true wtw gdy dana krawedz nalezy do dokladnie jednej sciezki
	 * UWAGA: PRZEPLYW WSZEDZIE!!!! TO JEST przeplyw z macierzy razy intensywnosc zrodla do poprawki w rozpiskach!!!!
	 */
	public boolean czyKrawedzNalezyDoDokladnieJednejSciezki(Krawedz k){
		// 1. czy z tej krawedzi da sie dojsc do dokladnie jednego odbiorcy
		// 2. czy do tej krawedzi da sie dojsc z dokladnie jednego zrodla
		int licznikOdb = 0;
		int licznikZr = 0;
		for (Punkt p : wierzcholki ){
			if (p.getTyp() == Punkt.TYP_ODBIORCA){
				if (czyIstniejeSciezka(k.getB(), p));
				licznikOdb++;
			}	// if typ_odbiorca
			if (p.getTyp() == Punkt.TYP_ZRODLO){
				if (czyIstniejeSciezka(p, k.getA()));
				licznikZr++;
			}	// if typ_odbiorca
		}	// for
		
		
		if (licznikOdb==0 || licznikZr==0){
			System.err.printf("v02.grafWZP.czyKrawedzNalezyDoDJS(): il zr = %d il od = %d kr=%s", 
					licznikZr, licznikOdb, k);
		}	// if
		
		if (licznikZr == 1 && licznikOdb == 1)
			return true;
		else
			return false;
	}	// czyKrawedzNalezyDoDokladnieJednejSciezki()

	/*
	 *  LOSOWE MODYFIKACJE
	 */

	/**
	 * @param beta Temperatura Odwrotna = 1/T (patrz dynamika Boltzmana)
	 * 		Beta>0 i wraz z postepem algorytmu powinno rosnac
	 */
	public void losowaModyfikacaZmnienPrzeplyw(double beta){
		beta = beta /10;
		GrafWrazZPrzeplywem g = this.clone();
		
		
		
		// jak 1 odbiorca to nic nie mozna zmienic
		if (g.mapaDostaw.getIlOdbiorcow() ==1){
			return;
		}	// if
		
		// losujemy zrodlo
		int i1 = r.nextInt(g.mapaDostaw.getIlZrodel());
		Punkt p1 = g.mapaDostaw.getZrodlo(i1);
		
		// losujemy odbiorce ale takiego zeby byl do niego wplyw ze zrodla
		// jezeli nie ma przeplywy na (i1 i3) to niec nie da sie zrobic
		int i2, i3;
		do {
			i3 = r.nextInt(g.mapaDostaw.getIlOdbiorcow());
		} while (g.mapaDostaw.getTransport(i1, i3) == 0);
		
		// wybieramy drugiego odbiorce zeby i2 != i3
		do {
			i2 = r.nextInt(g.mapaDostaw.getIlOdbiorcow());
		}	while (i2 == i3);
		
		Punkt p2 = g.mapaDostaw.getOdbiorca(i2);
		Punkt p3 = g.mapaDostaw.getOdbiorca(i3);
		
		
//		int w1 = g.indeks(p1);
//		int w2 = g.indeks(p2);
//		int w3 = g.indeks(p3);
		
		// i? - indeksy w macierzach transportu!!! ? -te zrodlo (?=1) lub wierzcholek (?=2,3)
		// p? - punkt na plaszczyznie
		// w? indeks wierzcholka w grafie 
		// ZACHOWAC SZCZEGOLNA OSTROZNOSC!!!!
		
		
		// jak nie istneje dokladnie jedna sciezka z i1 do i3 to nic nie da sie zrobic
		if (g.czyIstniejeDokladnieJednaSciezka(p1, p3) == false){
			return;
		}	// if
		

		// jezeli sciezka NIE ISTNIEJE to przesuwa tak z x4 ten przeplyw
		boolean czyIstniejeSciezka = g.czyIstniejeSciezka(p1, p2); 
		int tempd = d;
		if (czyIstniejeSciezka == false){
			tempd = tempd*4<=1 ? tempd*4 : 1;
		}	// if
		
		// jezeli nie moze zdjac z krawedzi (i1, i3) d przeplywu to zdejmuje ile sie da
		if (g.mapaDostaw.getTransport(i1, i3) < tempd){
			tempd = g.mapaDostaw.getTransport(i1, i3);
		}	// fi
		
		
		
		
		if (czyIstniejeSciezka == false ){
			// sciezka i1 -- i2 nie istnieje
			g.zmienTransportJezeliSciezkaDoI2NieIstnieje(i1, i2, i3, tempd);
		} else if (g.czyIstniejeDokladnieJednaSciezka(p1, p2) == true){
			// istnieje dokladnie jedna sciezka i1 -- i2
			g.zwiekszPrzeplywNaSciezce(p1, p2, tempd);
			g.zmniejszPrzeplywNaSciezce(p1, p3, tempd);
		}	else {
			// wiecej niz jedna sciezka i1 -- i2 -> nic sie nie da zrobic
			return;
		}	// else
		
		g.usunWiszaceWierzcholki();

		// akceptowanie lub odrzucanie zmiany
		
		obliczKoszt();
		g.obliczKoszt();
		double koszt1 = getKoszt();
		double koszt2 = g.getKoszt();
		
		if (koszt2 < koszt1){
//			System.out.printf("zaakceptowano\n");
			kopiujZ(g);
			return;
		}	// if
		
		double prog = Math.exp(-beta*(koszt2-koszt1) );
		
		if (r.nextDouble() < prog){
//			System.out.printf("zaakceptowano prog = %f\n", prog);
			kopiujZ(g);
			return;
		}	// if
//		System.out.printf("odrzucono roznica %f ,  prog = %f\n", koszt1 - koszt2, prog);
	}	// modyfikacaZmnienPrzeplyw()
	
	/** przenosi transport z i1-i3 na i1--i2
	 * sciezka z i1--i2 nie istnieje
	 * 
	 * @param i1 zrodlo
	 * @param i2 odbiorca do ktorego istnieje polaczeni
	 * @param i3 odbiorca do ktorego nie istnieje polaczenia
	 * @param tempd - procent intensywnosci zrodla ktory jest przenoszony
	 * tempd \in [0..1]
	 */
	private void zmienTransportJezeliSciezkaDoI2NieIstnieje( int i1, int i2, int i3,
			int tempd){
		
		if (tempd<0){
			System.err.printf("grafZPrzeplywem.zmienTransportJesli...(): niepoprawne tempd %d\n", tempd);
			return;
		}	// if
		
		Punkt p1 = mapaDostaw.getZrodlo(i1);
		Punkt p2 = mapaDostaw.getOdbiorca(i2);
		Punkt p3 = mapaDostaw.getOdbiorca(i3);
		
//		int w1 = indeks(p1);
//		int w2 = indeks(p2);
//		int w3 = indeks(p3);
		
		// i? - indeksy w macierzach transportu!!! ? -te zrodlo (?=1) lub wierzcholek (?=2,3)
		// p? - punkt na plaszczyznie
		// w? indeks wierzcholka w grafie 
		// ZACHOWAC SZCZEGOLNA OSTROZNOSC!!!!
		
		
		// nie ma sciezki z 1 do 2
		if (mapaDostaw.getTransport(i1, i2) != 0 ){
			System.err.printf("zmienTransportJesli...(): sciezka %s %s nie istnieje ale przeplyw nie jest zerowy %f\n",
					p1, p2, mapaDostaw.getTransport(i1, i2) );
			return;
		}	// if errcheck
		

		
		// towrzenie sciezki
		Punkt p = znajdzNajblizszyWezel(p1, p2);
		int zmianaNaKrawedziach = Math.min(tempd , mapaDostaw.getIntensywnoscZrodla(i1));
		
		if (p==null || p.equals(p1)){
			// z jakiegos powodu nie wybrano p tworzymy sciezke bezposrednia
			// te metody zmieniaja tablice przeplywu
			stworzSciezke(p1, p2, tempd);
			zmniejszPrzeplywNaSciezce(p1, p3, tempd);
			return;
		}	// fi	
		
		Vector<Punkt> s3 = getSciezka(p1, p3);
		Vector<Punkt> s2 = getSciezka(p1, p);
		try {
			
			// te metody nie zmieniaja tablicy transportu
			mapaDostaw.setTransport(i1, i2, mapaDostaw.getTransport(i1, i2) + tempd);
			dodajSciezke(p, p2, zmianaNaKrawedziach);
			zwiekszPrzeplywNaSciezce(s2, zmianaNaKrawedziach);
			
			mapaDostaw.setTransport(i1, i3, mapaDostaw.getTransport(i1, i3) - tempd);
			zwiekszPrzeplywNaSciezce(s3, -zmianaNaKrawedziach);
		}	catch (ModywikacjaGrafuException e){
			e.printStackTrace();
		}	// try
	}	// zmienTransportJezeliSciezkaDo i1 i2 nie istnieje
	
	
	
	
	/** zwieksza przeplyw na sciezce od p1 do p2 o d>=0
	 * wykonywalne gdy istnieje dokladnie jedna sciezka z p1 do p2
	 * no i dodatkowo modyfikuje tablice przeplywu
	 * @param p1 poczatek sciezki, zrodlo
	 * @param p2 koniec sciezki, odbiorca
	 * @param val  o ile zwiekszyc d>=0 i powinno byc "male"
	 */
	private void zwiekszPrzeplywNaSciezce(Punkt p1, Punkt p2, int val){
		if (p1.getTyp()!=Punkt.TYP_ZRODLO || p2.getTyp()!=Punkt.TYP_ODBIORCA){
			System.err.printf("v02.GrafZ.zwiekszPrzeplywNaSciezce(): bledny typ wezlow %s %s\n",
					p1, p2);
				return;
		}	// fi
		if (czyIstniejeDokladnieJednaSciezka(p1, p2) == false){
			System.err.printf("v02.GrafZ.zwiekszPrzeplywNaSciezce(): Sciezka nie jest jedyna %s %s\n",
				p1, p2);
			return;
		}	// fi
		if (val<0 ){
			System.err.printf("v02.GrafZ.zwiekszPrzeplywNaSciezce(): niepoprawne d = %f  \n", val);
			return;
		}	// fi
		
		Vector<Punkt> v = getSciezka(p1, p2);
		int i1 = indeks(p1);
		
		// zwiekszamy transport na sciezce z p1 do p2
		// TODO min val, mapaDostaw.getIntensywnoscZrodla()???
		int zmianaNaKrawedziach = val; //*mapaDostaw.getIntensywnoscZrodla(  mapaDostaw.indeksZrodla(p1));
		
		for (int i=0; i<v.size()-1; i++){
			int i2 = indeks(v.get(i+1));
			this.getKrawedz(i1, i2).zwiekszPrzeplyw(zmianaNaKrawedziach);
			i1 = i2;
		}	// for
		int iz = mapaDostaw.indeksZrodla(p1);
		int io = mapaDostaw.indeksOdbiorcy(p2);
		
		
		// uwaga zmiana na krawedzi wynosi : d* intensywnosc zrodla
		int nowyTransportNaMapie = mapaDostaw.getTransport(iz, io) + val;
		mapaDostaw.setTransport(iz, io, nowyTransportNaMapie);
		
	}	// zwiekszPrzeplywNaSciezce()
	
	
	
	
	/** tworzy sciezke  od p1 do p2 o przeplywie d>=0
	 * wykonywalne gdy nie istnieje jakakolwiek sciezka z p1 do p2
	 * no i dodatkowo modyfikuje tablice przeplywu
	 * @param p1 poczatek sciezki, zrodlo
	 * @param p2 koniec sciezki, odbiorca
	 * @param przeplyw - przeplyw przez sciezke,  o ile zwiekszyc przeplyw>=0 i powinno byc "male"
	 * 				   - przeplyw jest wzgledny i dotyczy mapy transportu
	 */
	private void stworzSciezke(Punkt p1, Punkt p2, int val){
		if (p1.getTyp()!=Punkt.TYP_ZRODLO || p2.getTyp()!=Punkt.TYP_ODBIORCA){
			System.err.printf("v02.GrafZ.stworzSciezke(): bledny typ wezlow %s %s\n",
					p1, p2);
				return;
		}	// fi
		if (czyIstniejeSciezka(p1, p2) == true){
			System.err.printf("v02.GrafZ.stworzSciezke(): Sciezka [ %s ... %s ] juz istnieje \n",
				p1, p2);
			return;
		}	// fi
		if (val<=0){
			System.err.printf("v02.GrafZ.stworzSciezke(): niepoprawny przeplyw d = %d\n", val);
			return;
		}	// fi

		int iz = mapaDostaw.indeksZrodla(p1);
		int io = mapaDostaw.indeksOdbiorcy(p2);
		if (mapaDostaw.getTransport(iz, io) >0){
			System.err.printf("v02.GrafZ.stworzSciezke(): przeplyw z p1 do p2 juz jest niezerowy! %s %s\n",
					p1, p2);
			return;
		}	// fi
		
		mapaDostaw.setTransport(iz, io, val);
		
		
		// tworzymy lancuszek z p1 do p2
		dodajSciezke(p1, p2,  val* mapaDostaw.getIntensywnoscZrodla(iz));
	}	// stworzSciezke
	
	/** tu nie ma zalozenia o tym ze p1 jest zrodlem a p2 odbiorca ale
	 * TO NIE MODYFIKUJE MAPY TRANSPORTU
	 * uzywac z zachowaniem szczegolnej odtroznosci!!!
	 * 
	 * @param val - przeplyw na sciezce, TO NIE JEST  przeplyw w mapietransportu
	 */
	private void dodajSciezke(Punkt p1, Punkt p2, int val){
		// tworzymy lancuszek z p1 do p2
		double dyst = Punkt.odleglosc( p1 , p2 );
		double odstep = 25;
		int n = (int) (dyst / odstep);
		
		int pierwszyIndeks = indeks(p1);
		int ostatniIndeks = indeks(p2);
		int poprzedniIndeks = pierwszyIndeks;
		double x1 = p1.getX();
		double x2 = p2.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		
		for (int k=1; k<n; k++){
			
			Punkt nowy = new Punkt( x1 + (k*1.0/n) * (x2 - x1) , y1 + (k*1.0/n) * (y2-y1) , 
					Punkt.TYP_INNY, val );
			
			if (czyWierzcholekIstnieje(nowy) == false){
				dodajWierzcholek(nowy);
				int nowyIndeks = indeks(nowy);
				dodajKrawedz( poprzedniIndeks, nowyIndeks, val );
				poprzedniIndeks = nowyIndeks;
			}	// if
		}	// for k
		dodajKrawedz(poprzedniIndeks,ostatniIndeks, val);
}	// stworzSciezke
	
	
	/** zmniejsza przeplyw na sciezce od p1 do p2 o d>=0
	 * wykonywalne gdy istnieje dokladnie jedna sciezka z p1 do p2
	 * i dodatkowo przeplyw z p1 do p2
	 * no i dodatkowo modyfikuje tablice przeplywu
	 * 
	 * @param p1 poczatek sciezki, zrodlo
	 * @param p2 koniec sciezki, odbiorca
	 * @param d  o ile zmniejszyc d>=0 i powinno byc "male" i d<=1
	 */
	private void zmniejszPrzeplywNaSciezce(Punkt p1, Punkt p2, int d){
		if (p1.getTyp()!=Punkt.TYP_ZRODLO || p2.getTyp()!=Punkt.TYP_ODBIORCA){
			System.err.printf("v02.GrafZ.zmniejszPrzeplywNaSciezce(): bledny typ wezlow %s %s\n",
					p1, p2);
				return;
		}	// fi
		
		if (czyIstniejeDokladnieJednaSciezka(p1, p2) == false){
			System.err.printf("v02.GrafZ.zmniejszPrzeplywNaSciezce(): Sciezka nie jest jedyna %s %s\n",
				p1, p2);
			return;
		}	// fi
		
		if (d < 0){
			System.err.printf("v02.GrafZ.zmniejszPrzeplywNaSciezce(): d = %d <0\n", d);
			return;
		}	// fi
		
		int iz = mapaDostaw.indeksZrodla(p1);
		int io = mapaDostaw.indeksOdbiorcy(p2);
		
		// tu korzystamy w wartosci unromowanej
		if (mapaDostaw.getTransport(iz, io) < d){
			System.err.printf("v02.GrafZ.zmniejszPrzeplywNaSciezce():transport z p1 do p2 jest za maly %f\n", 
					mapaDostaw.getTransport(iz, io));
			return;
		}	// fi
		
		// zmienimy transport na mapie dostaw 
		int nowyTransport = mapaDostaw.getTransport(iz, io) - d;
		mapaDostaw.setTransport(iz, io, nowyTransport);
		
		
		Vector<Punkt> v = getSciezka(p1, p2);
		int i1 = indeks(p1);
		
		// zmieniamy transport na sciezce z p1 do p2
		// JP dlaczego razy intensywnosc zrodla i co jezeli przeplyw  na krawedzi spelnia
		// d < przeplyw < d* intensywnoscZrodla??
		int zmianaNaKrawedziach = d /*mapaDostaw.getIntensywnoscZrodla(iz) */;

		for (int i=0; i<v.size()-1; i++){
			int i2 = indeks(v.get(i+1));
			if (getKrawedz(i1, i2).getPrzeplyw() >= zmianaNaKrawedziach)
				getKrawedz(i1, i2).zwiekszPrzeplyw(-zmianaNaKrawedziach);
			else{
				if (Math.abs((getKrawedz(i1, i2).getPrzeplyw() - zmianaNaKrawedziach )) > progBledu){
					ModywikacjaGrafuException.wypiszStanWywolania( String.format(
							"v02.GrafZPrzeplywem.zmniejszPrzeplywNaSciezce(): krawedz ma za maly przeplyw k=%s d=%f\n", 
							getKrawedz(i1, i2), zmianaNaKrawedziach
						)	// String.Format()
					);	// wypiszStanWywolania
				}	// if   wydruk kontrolny tylko gdy roznica jest duza (nie jest tylko bledem numerycznym)
				
				getKrawedz(i1, i2).setPrzeplyw(0);
			}	// if
			
			// usuwanie krawedzi
			if (getKrawedz(i1, i2).getPrzeplyw() < progBledu){
				usunKrawedz(i1, i2);
			}	// fi
			i1 = i2;
		}	// for
		
	}	// zmniejszPrzeplywNaSciezce()
	
	
	
	/** znajduje punkt najblizszy do celu ale taki aby byl polaczony sciezka ze zrodlem
	 * taym wierzcholkem nie moze byc odbiorca
	 * @param cel
	 * @param zrodlo
	 * @return zwraca Punkt w grafie najblizszy dp celu i wychodzacy ze zrodla, moze byc samo 
	 *  zrodlo jezeli nie ma lepszego
	 *  
	 *  zwraca null w wypadku bledow wierzcholek nie istnieje lub  istnieje sciezka
	 */
	private Punkt znajdzNajblizszyWezel(Punkt zrodlo, Punkt cel){
		if (!czyWierzcholekIstnieje(cel)){
			System.err.printf("GrafzPrzeplywem.znajdzWezel(): cel nie istnieje\n");
			return null;
		}	// fi
		if (!czyWierzcholekIstnieje(zrodlo)){
			System.err.printf("GrafzPrzeplywem.znajdzWezel(): zrodlo nie istnieje\n");
			return null;
		}	// fi
		if (czyIstniejeSciezka(zrodlo, cel)){
			System.err.printf("GrafzPrzeplywem.znajdzWezel(): sciezka juz istnieje\n");
			return null;
		}	// if
		
		Punkt ret = zrodlo;
		double minOdleglosc = Punkt.odleglosc(zrodlo, cel);
		
		// BFS
		Vector<Punkt> kolejka = new Vector<Punkt>();
		kolejka.add(zrodlo);
		
		boolean[] czyOdwiedzony = new boolean[getIloscWierzcholkow()];
		for (int i = 0 ;i< getIloscWierzcholkow(); i++)
			czyOdwiedzony[i] = false;
		
		while (kolejka.size() >=1){
			// Jezeli w moga byc cykle to trzeba jeszcze dodac
			//  Nie mam pojecia czemu ale to wlasnie tu ma tendencje do wywalania sie
			// boolean czyWierzcholekOdwiedzony[] = {false, .. false}
			// 
			//if (czyOdwiedzony[sasiad p] == false)){
			//		czyOdwiedzony[p] = true;
			//		kolejka.add(p)
			// }
			Punkt p = kolejka.remove(0);
			for (Krawedz e: getSasiedzi(indeks(p))){
				if (e.getB().getTyp() == Punkt.TYP_INNY && czyOdwiedzony[indeks(e.getB())] == false){
					kolejka.add(e.getB());
					czyOdwiedzony[indeks(e.getB())] = true;
					
				}	// if
				
			}	// for
			
			double odleglosc = Punkt.odleglosc(p, cel);
			if (odleglosc < minOdleglosc){
				minOdleglosc = odleglosc;
				ret = p;
			}	// if
		}	// while
		
		return ret;
	}	// znajdzNajblizszyWezel
	
	
	
	/** uzywac z zachowaniem szczegolnej ostroznosci
	 * ma metoda NIE modyfikuje macierzy transportu
	 */
	private void zwiekszPrzeplywNaSciezce(Vector<Punkt> v, int val) throws ModywikacjaGrafuException{
		if (v == null){
			return;
		}	// fi
		Punkt p1 = v.get(0);
		int i1 = indeks(p1);
		if (i1 == -1){
			System.err.printf("GzP.zwiekszPrzeplywNaSciezce(): punkt nie istnieje w grafie %s\n", p1);
			return;
		}	// if
		
		for (int i=1; i<v.size(); i++){
			Punkt p2 = v.get(i);
			int i2 = indeks(p2);
			
			if (i2 == -1){
				System.err.printf("GzP.zwiekszPrzeplywNaSciezce(): punkt nie istnieje w grafie %s\n", p2);
				throw new ModywikacjaGrafuException(String.format("Punkt %s z tej sciezkie nie istnieje w grafie\n%s\n", 
						p2, v));
			}	// if
			if (czyKrawedzIstnieje(i1, i2) == false){
				System.err.printf("GzP.zwiekszPrzeplywNaSciezce(): krawedz nie istnieje w grafie %s %s\n",p1, p2);
				throw new ModywikacjaGrafuException(String.format("krawedz %s %s z tej sciezkie nie istnieje w grafie\n%s\n", 
						p1, p2, v));
			}	// if
			getKrawedz(i1, i2).zwiekszPrzeplyw(val);
			
			if (Math.abs(getKrawedz(i1, i2).getPrzeplyw()) < progBledu){
				usunKrawedz(i1, i2);
			}	// fi
			i1 = i2;
		}	// for
		
	}	// zwiekszPrzeplywNaSciezce()
}	// class
