package v01;


import java.awt.Component;
import java.util.Random;
import java.util.Vector;

import v02.ModywikacjaGrafuException;



/** graf o wierzcholkach na R^2
 * G=(V,E)
 * 2007-06-08
 * 		(...)
 * 		utworzenie, zmudne klepanie metod i testowanie konsystencji 
 * 2007-09-02
 * 		laczenie rozdzielanie wierzcholkow
 * 2007-09-15
 * 		rozdzielanie X, Y, Lambda ksztatne (Wreszcie jakos dziala)
 * 		obliczanie kosztu grafu
 * 		obliczanie mapy odleglosci
 * 2007-09-18
 * 		wyszukiwanie najblizszych i przesuwanie wierzcholkow, DSF
 * 		wykrywanie i usuwanie prostych anomalii
 * 		opcja wyswietlania indeksow wezlow
 * 2007-09-19
 * 		przesuwanie wezlow o N(0, sigma^2) zgodnie z dynamika sieci Boltzmana
 * 		losowe usuwanie wezlow prostych   {x-y-z  => x---z}
 * 		losowe wstawianie wezla na srodku krawedzi
 * 2007-10-03
 * 		laczenie wierzcholkow przyjmowane jest z prawdopodobienstwem zaleznym 
 * 			od zmiany kosztu
 * 2007-10-08
 * 		przeniesiono modyfikacje grafu (laczenie rozlaczanie) do panelu w podgladzie grafu
 * 2007-11-11
 * 		dodano regulacje parametru alfa oraz wydruk pierwotnego kosztu
 * 2007-12-03
 * 		przeniesiono panel wyswietlajacy graf do innej klasy
 * 2008-01-06
 * 		usuwanie anomalii 1-3
 * 2008-02-06
 * 		usuwanie anomalii 2-2
 * 2008-02-24
 * 		przeniesiono tu pozostale losowe modyfikacje np ruchy kohonena itp
 * 2008-02-25
 * 		dodano akceptowanie lub odrzycanie rozlaczania zaleznie od zmiany kosztu
 * 		dodano modyfikowanie rozgalezien, krawedzi wychodzacych
 * 2008-05-19
 * 		kilka poprawek
 * 2008-06-01
 * 		Kilka WAZNYCH poprawek m.in. unimozliwienie tworzenia sie cykli w wyniku przesuwania 
 * 		rozgalezien
 *
 *  
 * w ogolnosci SKIEROWANY
 * graf operuje na indeksach!!
 * 
 * 
 * 
 * @author Jarosaw Piersa
 *
 * UWAGA do wersji 2.0
 * pomyslec nad zmiana struktury (pamietamy rowniez wejscia do wezla,
 *  dla kazdej krawedzi pamietamy skad, ile i dokad przez nia przeplywa)
 *
 */
public class Graf {
	protected int n;
	protected static double alpha = .25;
	protected double koszt = 1;
	protected static double sigmaKw = 16;
	private static double sigma = Math.sqrt(sigmaKw);
	protected Vector<Punkt> wierzcholki;
	protected Vector<Vector<Krawedz>> sasiedzi;
	protected MapaOdleglosci mapaOdl = null;
	protected Random r = null;
	protected double kosztPoczatkowy;
	
	private double maxDystans = 60;
	private double minDystans = 30;
	
	
	// TODO pomyslec nad metoda testujaca konsystencje grafu <chyba nie konieczne - widac>
	
	/** konstruktor klasy, tworzy graf o n wierzcholkach bez krawedzi
	 * wierzcholki sa losowymi punktami na R^2
	 * @param n ilosc wierzcholkow w grafie
	 */
	public Graf(int n){
		this.n = n;
		r = new Random(System.currentTimeMillis());
		
		wierzcholki = new Vector<Punkt>();
		for (int i=0; i<n; i++){
			wierzcholki.add(new Punkt());
		}	// for
		
		sasiedzi= new Vector<Vector<Krawedz>>();
		for (int i=0; i<n; i++){
			sasiedzi.add(new Vector<Krawedz>());
		}	// for
		
		obliczKoszt();
		kosztPoczatkowy = koszt;
		
	}	// konstruktor
	
	
	public Graf(MapaDostaw m){
		this(0);
		double odstep = 25;
		int del =m.getIlOdbiorcow();
		del = del ==0 ? del : del+1;
		
		// kopiujemy zrodla i odbiorcow
		for (int i=0; i<m.getIlZrodel(); i++){
			Punkt z = m.getZrodlo(i);
			dodajWierzcholek(new Punkt(z.getX(), z.getY(), z.getTyp(), m.getIntensywnoscZrodla(i)));
		}	// for
		for (int k=0; k< m.getIlOdbiorcow(); k++){
			Punkt o = m.getOdbiorca(k);
			double suma = 0;
			int j = m.indeksOdbiorcy(o);
			for (int i=0; i< m.getIlZrodel(); i++)
				suma += m.getTransport(i, j)*m.getIntensywnoscZrodla(i);
			dodajWierzcholek(new Punkt(o.getX(), o.getY(), o.getTyp(), suma));
			
		}	// for
		
		// dla kazdego polaczenia tworzymy lancuszek w grafie
		for (int i=0; i<m.getIlZrodel(); i++){
			for (int j=0; j<m.getIlOdbiorcow(); j++){
				if (m.getTransport(i, j)==0)
					continue;
				
				/*
				 *  JP tu bedzie prooblem do rozwiazania a mianowicie jak teraz poprzydzielac transporty...
				 */
				int przeplyw =  m.getTransport(i, j); /** m.getIntensywnoscZrodla(i)*/;
				
				double d = Punkt.odleglosc( m.getZrodlo(i) , m.getOdbiorca(j));
				int n = (int) (d / odstep);
				
				int pierwszyIndeks = indeks(m.getZrodlo(i));
				int ostatniIndeks = indeks(m.getOdbiorca(j));
				int poprzedniIndeks = pierwszyIndeks;
				
				double x1 = m.getZrodlo(i).getX();
				double x2 = m.getOdbiorca(j).getX();
				double y1 = m.getZrodlo(i).getY();
				double y2 = m.getOdbiorca(j).getY();
				
				for (int k=1; k<n; k++){
					Punkt nowy = new Punkt( x1 + (k*1.0/n) * (x2 - x1) , y1 + (k*1.0/n) * (y2-y1) , 
							Punkt.TYP_INNY, przeplyw );
					if (czyWierzcholekIstnieje(nowy) == false){
						dodajWierzcholek(nowy);
						int nowyIndeks = indeks(nowy);
						dodajKrawedz( poprzedniIndeks, nowyIndeks, przeplyw );
						poprzedniIndeks = nowyIndeks;
					}	// if
				}	// for k
				
				dodajKrawedz(poprzedniIndeks,ostatniIndeks, przeplyw);
				
				
			}	// for j
		}	// for i
		
		resetujKosztPoczatkowy();
	}	// Graf
	
	
	/** tekstowa reprezentacja grafu
	 */
	public String toString(){
		String res = "";
		for (int i=0; i<n; i++){
			res += String.format("%3d:   ", i);
			for (Krawedz k: sasiedzi.get(i)){
				res += String.format("%3d  ", indeks(k.getB()));
			}	// for integer
			res += "\n";
		}	// for vector
		return res;
	}	// toString();

	
	/* **********************************
	 * 
	 *  DOSTEP DO POL KLASY
	 *  
	 *   
	 * **********************************/
	
	
	
	
	public static double getSigmaKw() {
		return sigmaKw;
	}


	public static void setSigmaKw(double sigmaKw) {
		if (sigmaKw >=0){
			Graf.sigmaKw = sigmaKw;
			Graf.sigma = Math.sqrt(sigmaKw);
			return;
		}	// if
		System.err.format("Graf.setSigmaKW() argument sigmkaKw = %.2f < 0\n", sigmaKw);
	}	// if


	/**
	 * @return potega przelpywu przy liczeniu kosztu
	 */
	public double getAlpha() {
		return alpha;
	}	// getAlpha

	/** 
	 * @param alpha potega przy przeplywie przy liczeniu kosztu
	 * alpha in [0,1]
	 */
	public static void setAlpha(double _alpha) {
		if (_alpha >=0 && _alpha <=1)
			alpha = _alpha;
		else
			System.err.println("Graf.setAlpha(): niepoprawny parametr");
	}	// setAlpha

	/**
	 * sum( e %in E,  dl(e)*przeplyw(e)^%alpha )
	 * @return zwraca koszt (energie) grafu
	 */
	public double getKoszt() {
		return koszt;
	}	// getKoszt
	
	/**
	 * sum( e %in E,  dl(e)*przeplyw(e)^%alpha )
	 * @return zwraca koszt (energie) grafu
	 */
	public double getKosztPoczatkowy() {
		return kosztPoczatkowy;
	}	// getKoszt
	
	public MapaOdleglosci getMapaOdleglosci() {
		if (mapaOdl == null)
			obliczMapeOdleglosci();
		return mapaOdl;
	}	// getMapaOdl


	public void setMapaOdl(MapaOdleglosci mapaOdl) {
		this.mapaOdl = mapaOdl;
	}


	/** ustawia bierzacy koszt Grafu na poczatkowy
	 */
	public void resetujKosztPoczatkowy(){
		obliczKoszt();
		kosztPoczatkowy = koszt;
	}	// resetujKosztPoczatkowy()
	
	/* ***************************
	 * 
	 * K O S Z T
	 * 
	 * ********************************/
	
	/** oblicza koszt grafu
	 * 
	 * sum( e %in E,  dl(e)*przeplyw(e)^%alpha )
	 * 
	 */
	public void obliczKoszt(){
		double kosztTmp = 0;
		for (Vector<Krawedz> v : sasiedzi){
			for (Krawedz k : v){
				kosztTmp += obliczKosztKrawedzi(k);
			}	// for Krawedz
		}	// for Vector
		koszt = kosztTmp;
	}	// obliczKoszt

	protected double obliczKosztKrawedzi(Krawedz k){
		return Math.pow(k.getPrzeplyw(), alpha) * k.getDlugosc();
	}	// obliczKosztKrawedzi()
	
	protected double obliczKosztWierzcholka(Punkt p){
		double ret = 0;
		int i= indeks(p);
		if (i==-1){
			System.err.printf("Graf.obliczKosztW-ka(): Punkt %s nie nalezy do grafu\n", p);
			return -1;
		}	// if
		
		for (Krawedz k : getSasiedzi(i))
			ret += obliczKosztKrawedzi(k);
		for (Krawedz k : getSasiedziWchodzacy(i))
			ret += obliczKosztKrawedzi(k);
		
		return ret;
	}	// obliczKosztWierzcholka(Punkt)
	
	/**oblicza mape odleglosci w grafie
	 */
	public void obliczMapeOdleglosci(){
		MapaOdleglosci mapa = new MapaOdleglosci();
		for (int i=0; i<getIloscWierzcholkow(); i++ ){
			for (Krawedz k: getSasiedzi(i)){
				mapa.zaznaczOdleglosciWokolPunktu(k.getA());
				mapa.zaznaczOdleglosciWokolPunktu(k.getB());
			}	// for krawedz
		}	// for i
		if (mapaOdl!= null)
			mapaOdl.ujednolicenie(mapa);
		else
			mapaOdl = mapa;
	}	// obliczMape

	
	/* ****************************
	 * 
	 * DOSTEP DO WIERZCHOLKOW 
	 * 
	 * *******************************/
	
	
	
	/** zwraca ilosc wierzcholkow w grafie
	 */
	public int getIloscWierzcholkow() {
		return n;
	}

	/** zwraca indeks wierzcholka p O ILE ISTNIEJE ON W GRAFIE!!!
	 * zwraca -1 wpw
	 * @param p szukany wierzcholek
	 * @return indeks p w grafie lub -1
	 */
	public int indeks(Punkt p){
		for (int i=0; i<n; i++){
			if (p.equals( wierzcholki.get(i)))
				return i;
		}	// for
		return -1;
	}	// indeks

	/** usuwa wierzcholek p z grafu
	 * 
	 * @param p wierzcholek do usuniecia
	 * patrz porownywanie wierzcholkow
	 * klasa Punkt.java
	 */
	public void usunWierzcholek(Punkt p){
	
		int i = indeks(p);
		if (i==-1) return;
		wierzcholki.removeElementAt(i);
		sasiedzi.removeElementAt(i);
		
		for (Vector<Krawedz> v: sasiedzi){
			// JP: Iterujac po kolekcji NIE MOZNA Z NIEJ USUWAC!!!!!!!!!!!!!!
			// v.removeElement(i);  ConcurrentModificationException
			Krawedz doUsuniecia = null;
			for (Krawedz k : v){
				if (k.getB().equals(p))
					doUsuniecia = k;
			}	// for
	
			v.remove(doUsuniecia);
		}	// for
		n--;
		obliczKoszt();
	}	// usunWierzcholek


	/** dodaje wierzcholek p do grafu, o ile wczesniej taki nie istnial
	 * @param p wierzcholek do dodania
	 */
	public boolean dodajWierzcholek(Punkt p) {
		if (czyWierzcholekIstnieje(p)== true){
			System.err.printf("Graf.dodajWierzcholek(): wierzcholek juz istnieje %s\n", p);
			return false;
		}	// if
			
		n++;
		wierzcholki.add(p.clone());
		sasiedzi.add(new Vector<Krawedz>());
		return true;
	}	// dodajWierzcholek


	/** Czy wierzcholek p istnieje w grafie
	 * @param p wierzcholek do sprawdzenia
	 */
	public boolean czyWierzcholekIstnieje(Punkt p){
		for (Punkt q: wierzcholki){
			if (p.equals(q))
				return true;
		}	// for
		return false;
	}	// czyWierzcholekIstnieje()

	/** zwraca losowy wierzcholek z grafu w tym takrze zrodla i odbiorcy
	 */
	protected Punkt getWierzcholekLosowy(){
		return (getWierzcholek(r.nextInt(getIloscWierzcholkow())));
	}	// punkt

	
	/** zwraca wierzcholek o indeksie i
	 * @param i indeks
	 */
	public Punkt getWierzcholek(int i){
		if (i==-1){
			// w tym wypadku nie ma wiadomosci o bledzie
			return null;
		}	// if
		
		if (i<0 || i>=n){
			System.err.printf("Graf.getWierzcholek(): bledny indeks %d\n", i);
			return null;
		}	// if
		return wierzcholki.get(i);
	}	// getWierzcholek
	
	/** usuwa z grafu wiszace punkty
	 */
	public void usunWiszaceWierzcholki(){
		int i=0;
		while (i<getIloscWierzcholkow()){
			Punkt p = getWierzcholek(i);
			if (p.getTyp()!=Punkt.TYP_ZRODLO && p.getTyp()!=Punkt.TYP_ODBIORCA){
				if (getSasiedzi(i).size() == 0){
					if (getSasiedziWchodzacy(i).size() == 0){
						usunWierzcholek(p);
						continue;
					}	// if
				}	// if
			}	// if
			i++;
		}	// while
	}	//usunWiszacePunkty()
	
	/** zwraca wszystkie wierzcholki
	 */
	public Vector<Punkt> getWierzcholki(){
		return wierzcholki;
	}	// getWierzcholek
	
	
 
	/* *********************************
	 * 
	 *  DOSTEP DO KRAWEDZI
	 * 
	 * *********************************/
	
	
	
	/** zwraca sasiadow wierzcholka o indeksie i
	 * @param i indeks wierzcholka
	 * @return wektor sasiadow wierzcholka i
	 */
	public Vector<Krawedz> getSasiedzi(int i){
		if (i<0 || i>=n){
			System.err.printf("Graf.getSasiedzi(): bledny indeks %d\n", i);
			return null;
		}	// if
		return sasiedzi.get(i);
	}	// getSasiedzi
	
	
	/** zwraca sasiadow wchodzacych do wierzcholka o indeksie i
	 * @param i indeks wierzcholka
	 * @return wektor sasiadow wierzcholka i
	 */
	public Vector<Krawedz> getSasiedziWchodzacy(int i){
		if (i<0 || i>=n){
			System.err.println("Graf.getSasiedzi(): bledny indeks "+i);
			return null;
		}	// if
		Vector<Krawedz> v = new Vector<Krawedz>();
		for (int j=0; j<n; j++){
			if (czyKrawedzIstnieje(j, i))
				v.add(getKrawedz(j, i));
		}	// for
		return v;
	}	// getSasiedzi
	
	
	
	/** czy istnieje krawedz miedzy wierzcholkami i oraz j
	 * sprawdzanie istnienia krawedzi nie jest symetryczne!!!
	 * @return true wtw gry istniej krawedz z i do j w G
	 */
	public boolean czyKrawedzIstnieje(int i, int j){
		if ( i<0 || j<0 ||  i>=n || j>=n || i==j){
			//System.err.printf("Graf.czyKrawedzIstnieje(): bledny indeks %d %d\n", i, j);
			return false;
		}	// if
		
		for (Krawedz k : sasiedzi.get(i)){
			if ( indeks(k.getB()) == j)
				return true;
		}	// for 
		return false;
	}	// czyKrawedzIstnieje()
	
	/** dodaje krawedz (i, j) do G 
	 * dodawanie krawedzi nie jest symetryczne!!
	 * @param (i,j)
	 */
	public void dodajKrawedz(int i, int j){
		if (i==j || i<0 || j<0 ||  i>=n || j>=n){
			System.err.printf("Graf.dodajKrawedz(): bledny indeks %d %d\n", i, j);
			return;
		}	// if
		if (czyKrawedzIstnieje(i, j))
			return;
		
		Krawedz k = new Krawedz(getWierzcholek(i), getWierzcholek(j));
		sasiedzi.get(i).add(k);
		obliczKoszt();
	}	// dodajKrawedz()
	
	/** dodaje krawedz (i, j) do G 
	 * dodawanie krawedzi nie jest symetryczne!!
	 * @param (i,j)
	 * @param p przeplyw przez krawedz
	 */
	public void dodajKrawedz(int i, int j, int p){
		if (i==j || i<0 || j<0 ||  i>=n || j>=n){
			System.err.printf("Graf.dodajKrawedz(): bledny indeks %d %d", i, j);
			try {throw new ModywikacjaGrafuException(String.format("Graf.dodajKrawedz(): bledny indeks %d %d\n", i, j));
			} catch(ModywikacjaGrafuException e){
				e.printStackTrace();
			}	// catch
			
			return;
		}	// if
		if (czyKrawedzIstnieje(i, j))
			return;
		
		Krawedz k = new Krawedz(getWierzcholek(i), getWierzcholek(j), p);
		sasiedzi.get(i).add(k);
	}	// dodajKrawedz()
	
	/** usuwa z grafu krawedz miedzy wierzcholkami i oraz j 
	 * @param (i, j) indeksy wierzcholkow krawedzi
	 * usuwanie krawedzi nie jest symetryczne!!! 
	 */
	public void usunKrawedz(int i, int j){
		if (i==j || i<0 || j<0 ||  i>=n || j>=n){
			System.err.printf("Graf.usunKrawedz(): bledny indeks %d %d\n", i, j);
			return;
		}	// if
		
		
		int del = 0;
		for (int k=0; k<sasiedzi.get(i).size(); k++ ){
			if (indeks(sasiedzi.get(i).get(k).getB())==j)
				del = k;
		}	// for
		sasiedzi.get(i).removeElementAt(del);
		obliczKoszt();
	}	// usunKrawedz()
	
	/** zwraca krawedz miedzy wierzcholkami o indeksach i,j o ile istnieje
	 * @param i indeks wierzcholka wychodzcego
	 * @param j indeks wierzcholka wchodzacego
	 * @return krawedz (i,j) lub null jezeli taka nie istnieje
	 */
	public Krawedz getKrawedz(int i, int j){
		if (i==j || i<0 || j<0 ||  i>=n || j>=n){
			System.err.printf("Graf.getKrawedz(): bledny indeks %d %d\n", i, j);
			return null;
		}	// if
		
		for (Krawedz k : sasiedzi.get(i)){
			if ( indeks(k.getB()) == j)
				return k;
		}	// for 
		return null;
	}	// getKrawedz
	
	/** szukanie sciezki miedzy dwoma wierzcholkami 
	 * NIESYMETRYCZNE!!!
	 * @param p1 punkt startowy
	 * @param p2 punkt koncowy
	 * @return true wtw gdy istnieje sciezka z p1 do p2
	 */
	public boolean czyIstniejeSciezka(Punkt p1, Punkt p2){
		if (!czyWierzcholekIstnieje(p1) || !czyWierzcholekIstnieje(p2) )
			return false;
		if (p1.equals(p2))
			return true;
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		
		boolean tablica[] = new boolean[n];
		for (int i=0; i<n; i++)
			tablica[i] = false;
		Vector<Punkt> stos = new Vector<Punkt>();
		stos.add(p1);
		tablica[i1]= true;
		
		// DSF
		while (stos.size()!=0){
			Punkt p = stos.elementAt(stos.size()-1);
			stos.remove(stos.size()-1);
			for (Krawedz k: getSasiedzi(indeks(p))){
				Punkt s = k.getB();
				if (tablica[indeks(s)]== false){
					tablica[indeks(s)]= true;
					stos.add(s);
				}	// if
			}	// for sasiedzi
		}	// while
		
		
		return tablica[i2];
	}	// czyIstniejeSciezka()

	/* ***************************************************
	 * 
	 *  MODYFIKACJE GRAFU - LACZENIE
	 * 
	 * ****************************************************/

	/** laczy dwa wierzcholki w jeden zachowujacy wspolnych sasiadow
	 * @param p1 wierzcholek do polaczenia
	 * @param p2 wierzcholek do polaczenia
	 */
	public void polaczWierzcholki(Punkt p1, Punkt p2){
		polaczWierzcholki(p1, p2, 0);
	}	// polaczWierzcholki
	
	
	/** laczy dwa wierzcholki w jeden zachowujacy wspolnych sasiadow
	 * z pstwem zaleznym od beta i zmiany kosztu
	 * @param p1 wierzcholek do polaczenia
	 * @param p2 wierzcholek do polaczenia
	 * @param beta Temperatura Odwrotna = 1/T (patrz dynamika Boltzmana)
	 * 		Beta>=0 i wraz z postepem algorytmu powinno rosnac
	 * 		Beta == 0 powoduje ze zawsze zostanie przyjete jak zwiekszy koszt
	 */
	// TODO poprawic laczenie do X!!
	public void polaczWierzcholki(Punkt p1, Punkt p2, double beta){
		
		if (czyWierzcholekIstnieje(p1)== false || p1.getTyp()!=Punkt.TYP_INNY)
			return;
		if (czyWierzcholekIstnieje(p2)== false || p2.getTyp()!=Punkt.TYP_INNY)
			return;
		if (p1.equals(p2))
			return;
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		if (czyKrawedzIstnieje(i1, i2) || czyKrawedzIstnieje(i2, i1)) 
			return;
		// TODO |A|+|B| >=3
		// powinno byc |A \ union B| >=3
		// ??
		if (getSasiedzi(i1).size() + getSasiedzi(i2).size() >=3)
			return;
		if (getSasiedziWchodzacy(i1).size() + getSasiedziWchodzacy(i2).size() >=3)
			return;
		
		polaczWierzcholkiNiesasiadujace(p1, p2, beta);
		obliczKoszt();
	}	// polaczWiezcholki()
	
	/** czenie dwuch wierzcholkow w jeden 
	 * zalozenie ze p1 i p2 nie sa sasiednie
	 */
	private void polaczWierzcholkiNiesasiadujace(Punkt p1, Punkt p2, double beta){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		double koszt1=0, koszt2 = 0;
		
		for (Krawedz k : getSasiedzi(i1)){
			koszt1 += obliczKosztKrawedzi(k);
		}	// for krawedz
		for (Krawedz k : getSasiedziWchodzacy(i1)){
			koszt1 += obliczKosztKrawedzi(k);
		}	// for krawedz
		for (Krawedz k : getSasiedzi(i2)){
			koszt1 += obliczKosztKrawedzi(k);
		}	// for krawedz
		for (Krawedz k : getSasiedziWchodzacy(i2)){
			koszt1 += obliczKosztKrawedzi(k);
		}	// for krawedz
		
		
		Punkt p3 = new Punkt((p1.getX() + p2.getX()) /2, (p1.getY() + p2.getY()) /2,
				Punkt.TYP_INNY
				/*, p1.getPrzeplyw()+p2.getPrzeplyw()*/
				);
		
		boolean ok = czyWierzcholekIstnieje(p3);
		if (ok == true){
			return;
		}	// if
		
		dodajWierzcholek(p3);
		int i3 = indeks(p3);
		
		// liczenie przeplywu wychodzacego
		int przeplyw[] = new int[n];
		
		for (Krawedz k1: getSasiedzi(i1)){
			przeplyw[indeks(k1.getB())]+=k1.getPrzeplyw();
		}	// for
		for (Krawedz k2: getSasiedzi(i2)){
			przeplyw[indeks(k2.getB())]+=k2.getPrzeplyw();
		}	// for
		
		// dodawanie krawedzi wychodzacych
		for (int i=0; i<n; i++){
			if (przeplyw[i]!=0){
				dodajKrawedz(i3, i, przeplyw[i]);
			}	// fi
		}	// for
		
		// liczenie przeplywu wchodzacego - przyplywu
		int przyplyw[] = new int[n];
		
		// iteracja po wierzcholkach
		for (int i=0; i<n; i++){
			if (czyKrawedzIstnieje(i, i1) == true){
				przyplyw[i]+= getKrawedz(i, i1).getPrzeplyw();
			}	// fi
			if (czyKrawedzIstnieje(i, i2) == true){
				przyplyw[i]+= getKrawedz(i, i2).getPrzeplyw();
			}	// fi
			
		}	// for
		
		// dodawanie krawedzi wchodzacych
		for (int i=0; i<n-1; i++){
			if (przyplyw[i]>0){
				dodajKrawedz(i, i3, przyplyw[i]);
			}	// fi
		}	// for
		
		for (Krawedz k : getSasiedzi(i3)){
			koszt2 += obliczKosztKrawedzi(k);
		}	// for krawedz
		for (Krawedz k : getSasiedziWchodzacy(i3)){
			koszt2 += obliczKosztKrawedzi(k);
		}	// for krawedz
		
		// jezeli zlaczenie równoległe || do X to akceptowane jest zawsze
		if (getSasiedzi(i1).size()==1 && getSasiedzi(i2).size()==1 &&
				getSasiedziWchodzacy(i1).size()==1 && getSasiedziWchodzacy(i2).size()==1){
			usunWierzcholek(p1);
			usunWierzcholek(p2);
			return;
		}	// if
		
		
		// jezeli koszt zmalal akceptujemy
		if (koszt2 <  koszt1){
			usunWierzcholek(p1);
			usunWierzcholek(p2);
			return;
		}	// if
		
		// wpw z pstwem exp(beta) akceptujemy
		if (r.nextDouble() < Math.exp(-beta*(koszt2-koszt1))){
			usunWierzcholek(p1);
			usunWierzcholek(p2);
			return;
		}	// if
		usunWierzcholek(p3);
		
		
	}	//polaczWierzcholkiNiesasiadujace()
	
	/* **************************
	 * 
	 * R O Z D Z I E L A N I E
	 *
	 * *************************/
	
	/** rozdziela wierzcholek
	 * musi miec dokladnie dwuch sasiadow nastepnych i co najwyzej dwuch poprzednich 
	 * @param p1 punkt do rozdzielenia
	 */
	public void rozdzielWierzcholek(Punkt p1){
		rozdzielWierzcholek(p1, 0);
	}	// rozdzielWierzcholek()
	
	
	
	/** rozdziela wierzcholek
	 * musi miec dokladnie dwuch sasiadow nastepnych i co najwyzej dwuch poprzednich 
	 * @param p1 punkt do rozdzielenia
	 * @param beta - temperatura odwrotna (b>0 i powinno rosnac) - patrz dynamika maszyn Boltzmanna
	 * 		  beta==0  => zmiana zawsze zaakceptowana
	 */
	/*    
	 *   s3      s4                        s3             s4  
	 *      \   /              ===\          \            / 
	 *        p1               ===/           p3       p4
	 *      /   \							  | \	  / | 
	 *      (...)                                (...)
	 * 
	 */
	public void rozdzielWierzcholek(Punkt p1, double beta){
		int i1 = indeks(p1);
		if (p1.getTyp()!= Punkt.TYP_INNY ){
			return;
		}	// if
		
		int degIn = getSasiedziWchodzacy(i1).size();
		int degOut = getSasiedzi(i1).size();
		
		if (degIn ==1 && degOut==2){
			rozdzielWierzcholek1Wejscie2Wyjscia(p1, beta);
		}	// if
		
		if (degIn ==2 && degOut==1){
			rozdzielWierzcholek2Wejscia1Wyjscie(p1, beta);
		}	// if
		
		if (degIn ==2 && degOut == 2){
			//rozdzielWierzcholek2Wejscia2Wyjscia(p1);
		}	// if
		
		
		
	}	// rozdzielW-ek()
	
	
	/** rozdziela wierzcholek p1 o ksztalcie
	 *  
	 *     s1                             s1
	 *     |                             /  \
	 *     p1                           p3  p4
	 *    /  \                         /      \
	 * pop1  pop2                    pop1    pop2
	 *
	 * @param p1 - wierzcholek do rozdzielenia
	 * @param beta - temperatura odwrotna (b>0 i powinno rosnac) - patrz dynamika maszyn Boltzmanna
	 * 		  beta==0  => zmiana zawsze zaakceptowana
	 *
	 */
	private void rozdzielWierzcholek2Wejscia1Wyjscie(Punkt p1, double beta){
		int i1 = indeks(p1);
		Punkt s1 = getSasiedzi(i1).get(0).getB();
		int is1 = indeks(s1);
		Punkt pop1 = getSasiedziWchodzacy(i1).get(0).getA();
		Punkt pop2 = getSasiedziWchodzacy(i1).get(1).getA();
		int ipop1 = indeks(pop1);
		int ipop2 = indeks(pop2);
		
		
		// Nowe wierzcholki
		
		Punkt p3 = new Punkt( (s1.getX() +  pop1.getX() ) /2 ,
				(s1.getY() +  pop1.getY()) /2,
				Punkt.TYP_INNY,
				getKrawedz(ipop1, i1).getPrzeplyw() );
		
		Punkt p4 = new Punkt( (s1.getX() +  pop2.getX() ) /2 ,
				(s1.getY() +  pop2.getY()) /2,
				Punkt.TYP_INNY,
				getKrawedz(ipop2, i1).getPrzeplyw() );
		
		
		if ( czyWierzcholekIstnieje(p3) == true){
			return;
		}	// if
		if ( czyWierzcholekIstnieje(p4) == true){
			return;
		}	// if
		
		dodajWierzcholek(p3);
		dodajWierzcholek(p4);
		int i3 = indeks(p3);
		int i4 = indeks(p4);
		
		
		// Wierzcholki wychodzace
		dodajKrawedz(i3, is1, getKrawedz(ipop1, i1).getPrzeplyw());
		dodajKrawedz(i4, is1, getKrawedz(ipop2, i1).getPrzeplyw());
		
		// wierzcholki wchodzace
		
		dodajKrawedz(ipop1, i3, getKrawedz(ipop1, i1).getPrzeplyw() );
		dodajKrawedz(ipop2, i4, getKrawedz(ipop2, i1).getPrzeplyw() );

		// liczenie zmiany kosztu
		double koszt1 = obliczKosztWierzcholka(p1);
		
		double koszt2 = obliczKosztWierzcholka(p3);
		koszt2 += obliczKosztWierzcholka(p4);	
		
		if (koszt2 < koszt1){
			usunWierzcholek(p1);
			return;
		}	// if
		
		if (r.nextDouble() < Math.exp(-beta*(koszt2-koszt1))){
			usunWierzcholek(p1);
			return;
		}	// if
		
		// cofanie zmian
		usunWierzcholek(p3);
		usunWierzcholek(p4);
		
	}	// rozdzielWierzcholek2Wejscia1Wyjscie()
	
	/** Rozdziela wierzcholek p1 o ksztalcie
	 * 
	 *    s3  s4
	 *      \/
	 *      p1
	 *      |
	 *     pop1
	 *     
	 * @param p1 wierzcholek do rozdzielenia
	 * @param beta - temperatura odwrotna (b>0 i powinno rosnac) - patrz dynamika maszyn Boltzmanna
	 * 		  beta==0  => zmiana zawsze zaakceptowana
	 */
	private void rozdzielWierzcholek1Wejscie2Wyjscia(Punkt p1, double beta){
		int i1 = indeks(p1);
		Punkt s3 = getSasiedzi(i1).get(0).getB();
		Punkt s4 = getSasiedzi(i1).get(1).getB();
		Punkt pop1 = getSasiedziWchodzacy(i1).get(0).getA();
		int is3 = indeks(s3);
		int is4 = indeks(s4);
		int ipop1 = indeks(pop1);
		
		
		// Nowe wierzcholki
		
		Punkt p3 = new Punkt( (s3.getX() + pop1.getX() ) /2 ,
				( s3.getY() + pop1.getY()) /2,
				Punkt.TYP_INNY
				/*,	s3.getPrzeplyw() */);
		
		Punkt p4 = new Punkt( (s4.getX() + pop1.getX() ) /2 ,
				( s4.getY() + pop1.getY()) /2,
				Punkt.TYP_INNY
				/*,	s4.getPrzeplyw() */);
		
		if ( czyWierzcholekIstnieje(p3) == true){
			return;
		}	// if
		if ( czyWierzcholekIstnieje(p4) == true){
			return;
		}	// if
		
		dodajWierzcholek(p3);
		dodajWierzcholek(p4);
		int i3 = indeks(p3);
		int i4 = indeks(p4);
				
		// Wierzcholki wychodzace
		dodajKrawedz(i3, is3, getKrawedz(i1, is3).getPrzeplyw());
		dodajKrawedz(i4, is4, getKrawedz(i1, is4).getPrzeplyw());
		
		// wierzcholki wchodzace
		
		dodajKrawedz(ipop1, i3, getKrawedz(i1, is3).getPrzeplyw() );
		dodajKrawedz(ipop1, i4, getKrawedz(i1, is4).getPrzeplyw() );

		// liczenie zmiany kosztu
		double koszt1 = obliczKosztWierzcholka(p1);
		double koszt2 = obliczKosztWierzcholka(p3);
		koszt2 += obliczKosztWierzcholka(p4);	
		
		if (koszt2 < koszt1){
			usunWierzcholek(p1);
			return;
		}	// if
		
		if (r.nextDouble() < Math.exp(-beta*(koszt2-koszt1))){
			usunWierzcholek(p1);
			return;
		}	// if
		
		// cofanie zmian
		usunWierzcholek(p3);
		usunWierzcholek(p4);
	}	// rozdzielWierzcholekFrontowo1Wejscie()
	
	
	/** Rozdziela wierzcholek p1 o ksztalcie
	 * 
	 *    s3  s4
	 *     \  /
	 *      p1
	 *     /  \
	 *  pop1  pop2
	 *     
	 * @param p1 wierzcholek do rozdzielenia
	 * TODO pomyslec jeszcze nad tym
	 */
	protected void rozdzielWierzcholek2Wejscia2Wyjscia(Punkt p1){
		int i1 = indeks(p1);
		Punkt s3 = getSasiedzi(i1).get(0).getB();
		Punkt s4 = getSasiedzi(i1).get(1).getB();
		Punkt pop1 = getSasiedziWchodzacy(i1).get(0).getA();
		Punkt pop2 = getSasiedziWchodzacy(i1).get(1).getA();
		int is3 = indeks(s3);
		int is4 = indeks(s4);
		int ipop1 = indeks(pop1);
		int ipop2 = indeks(pop2);
		
		
		Punkt p3 = new Punkt( (p1.getX() + s3.getX() + pop1.getX()*.5 + pop2.getX()*.5 ) /3 ,
				(p1.getY() + s3.getY() + pop1.getY()*.5 + pop2.getY()*.5) /3,
				Punkt.TYP_INNY/*,
				s3.getPrzeplyw() */);
		
		Punkt p4 = new Punkt( (.5*p1.getX() + s4.getX() + pop1.getX()*.75 + pop2.getX()*.75 ) /3 ,
				(.5*p1.getY() + s4.getY() + pop1.getY()*.75 + pop2.getY()*.75) /3,
				Punkt.TYP_INNY/*,
				s4.getPrzeplyw() */);
		
		
		if ( czyWierzcholekIstnieje(p3) == true){
			return;
		}	// if
		if ( czyWierzcholekIstnieje(p4) == true){
			return;
		}	// if
		
		dodajWierzcholek(p3);
		dodajWierzcholek(p4);
		int i3 = indeks(p3);
		int i4 = indeks(p4);
		
		if (i3==-1 || i4==-1){
			System.err.println("Graf.Rozdziel() : nie mozna rozdzielic!");
			if (i3!=-1) usunWierzcholek(p3);
			if (i4!=-1) usunWierzcholek(p4);
			return;
		}	// if
		
		// Wierzcholki wychodzace
		dodajKrawedz(i3, is3, getKrawedz(i1, is3).getPrzeplyw());
		dodajKrawedz(i4, is4, getKrawedz(i1, is4).getPrzeplyw());
		
		/* Wierzcholki wchodzace
		 * 
		 *  (s3)           (s4)
		 *    \             /
		 *   e1\           /e2
		 *      \         /
		 *     (p3)     (p3)
		 *      |  \    /  |
		 *      |   \  /   |
		 *      |    \/    |
		 *    v1| v2 /\ u1 |u2
		 *      |   /  \   |
		 *      |  /    \  |
		 *    (pop1)    (pop2)    
		 *
		 * u1 + u2 = u = przeplyw z (pop1) do (s1) stary wierzcholek
		 * v1 + v2 = v = przeplyw z (pop2) do (s1) stary wierzcholek
		 * e1 + e2 = u + v
		 * 
		 *
		 *  	v1		v2		u1		u2
		 * ==================================
		 * 1	v		0		e1-v	e2
		 * 2	0		v		e1		e2-v
		 * 3	e1-u	e2		u		0
		 * 4   	e1		e2 - u	0		u
		 * 
		 * 
		 * cost = dl1 * v1  + dl2 * v2 + dl3 * u1 + dl4 * u4
		 */     
		double dl1 = Punkt.odleglosc(pop1, p3);
		double dl2 = Punkt.odleglosc(pop1, p4);
		double dl3 = Punkt.odleglosc(pop2, p3);
		double dl4 = Punkt.odleglosc(pop2, p4);
		 
		int e1 = getKrawedz(i1, is3).getPrzeplyw();
		int e2 = getKrawedz(i1, is4).getPrzeplyw();
		
		int v = getKrawedz(ipop1, i1).getPrzeplyw();
		int u = getKrawedz(ipop2, i1).getPrzeplyw();
		
		double c1=0, c2=0, c3=0, c4 = 0;
		
		if (e1 - v>=0)
			c1 = dl1 * Math.pow(v, alpha) + 0 + 
				dl3 * Math.pow(e1 - v, alpha) + dl4 * Math.pow(e2, alpha);
		else 
			c1 = Double.MAX_VALUE;
		
		if (e2 - v>=0)
			c2 = 0 +  dl2 * Math.pow(v, alpha) + 
				dl3 * Math.pow(e1, alpha) + dl4 * Math.pow(e2 - v, alpha);
		else 
			c2 = Double.MAX_VALUE;
		
		if (e1 - u>=0)
			c2 = dl1*Math.pow(e1-u, alpha) +  dl2 * Math.pow(e2, alpha) + 
				dl3 * Math.pow(u, alpha) + 0;
		else 
			c3 = Double.MAX_VALUE;
		
		if (e2 - u>=0)
			c2 = dl1*Math.pow(e1-u, alpha) +  dl2 * Math.pow(e2, alpha) + 
				dl3 * Math.pow(u, alpha) + 0;
		else 
			c3 = Double.MAX_VALUE;
		int min = 1;
		double minc = c1;
		if (minc > c2){
			minc = c2;
			min = 2;
		}	// if
		if (minc > c3){
			minc = c3;
			min = 3;
		}	// if
		if (minc > c4){
			minc = c4;
			min = 4;
		}	// if		
		
		switch (min){
			case 1:
				dodajKrawedz(ipop1, i3, v);
				if (e1 - v > 0)
					dodajKrawedz(ipop2, i3, e1-v);
				dodajKrawedz(ipop2, i4, e2);
				break;
			case 2:
				dodajKrawedz(ipop1, i4, v);
				dodajKrawedz(ipop2, i3, e1);
				if (e2-v > 0)
					dodajKrawedz(ipop2, i4, e2-v);
				break;
			case 3:
				if (e1 - u > 0)
					dodajKrawedz(ipop1, i3, e1 - u);
				dodajKrawedz(ipop1, i4, e2);
				dodajKrawedz(ipop2, i3, u);
				break;
			case 4:
			default :
				dodajKrawedz(ipop1, i3, e1);
				if (e2 - u > 0)
					dodajKrawedz(ipop1, i4, e2-u);
				dodajKrawedz(ipop2, i4, u);
				
		}	// switch
		
		// super ale jeszcze trzeba sprawdzić drożność dróg
		usunWierzcholek(p1);
		System.out.println("Graf.RozdzielW-ek-2-2");
		obliczKoszt();
	}	// rozdzielWierzcholekFrontowo2Wejscia()
	
	
	/*
	 * U S U W A N I E   A N O M A L I I
	 */
	
	
	
	/** wykrywa sciezki dwukrokowe w grafie,
	 * zwraca indeks srodkowego wezla
	 * @param p1 poczatek drogi
	 * @param p2 koniec drogi
	 * @return indeks wezla posredniego lub -1 o ile taki nie istnieje
	 */
	private int czyIstniejeSciezkaWDwuchKrokach(Punkt p1, Punkt p2){
		if (!czyWierzcholekIstnieje(p1) || !czyWierzcholekIstnieje(p2) )
			return -1;
		if (p1.equals(p2))
			return -1;
		int i1 = indeks(p1);
		
		for (Krawedz k1: getSasiedzi(i1)){
			int i3 = indeks(k1.getB());
			for (Krawedz k2: getSasiedzi(i3)){
				if (p2.equals( k2.getB() ))
					return i3;
			}	// for k2
		}	// for k1
		return -1;
	}	// czyIstniejeSciezkaWdwuchKrokach()

	/** wykrywa sciezki trojkrokowe w grafie,
	 * zwraca indeks srodkowego wezla
	 * @param p1 poczatek drogi
	 * @param p2 koniec drogi
	 * @return indeksy wezlow posrednich lub [-1, -1] o ile takie nie istnieja
	 */
	private int[] czyIstniejeSciezkaWTrzechKrokach(Punkt p1, Punkt p2){
		int[] ret = {-1, -1};
		if (!czyWierzcholekIstnieje(p1) || !czyWierzcholekIstnieje(p2) )
			return ret;
		if (p1.equals(p2))
			return ret;
		int i1 = indeks(p1);
		
		for (Krawedz k1: getSasiedzi(i1)){
			int i3 = indeks(k1.getB());
			for (Krawedz k2: getSasiedzi(i3)){
				int i4 = indeks(k2.getB());
				for (Krawedz k3: getSasiedzi(i4)){
					if (p2.equals( k3.getB() )){
						ret[0] = i3;
						ret[1] = i4;
						return ret;
					}	// if
				}	// for k3
				
			}	// for k2
		}	// for k1
		
		return ret;
	}	// czyIstniejeSciezkaWTrzechKrokach()
	
	/* ******************************************************
	 * 
	 *  USUWANIE ANOMALII
	 * 
	 * ***************************************************/
	
	
	
	/** usuwanie z grafu anomalii tj sytuacji ktore same trudno sie rozwiazuja
	 */
	public void usunAnomalie(){
		usunAnomalie_1_2();
		usunAnomalie_1_3();
		usunAnomalie_2_2();
	}	// usunAnomalie
	
	/**         i2                     i2
	 *         |  \                     |
	 *         \   i3           =>     i3 
	 *          \ /                     |
	 *           i1                    i1
	 *
	 */
	private void usunAnomalie_1_2(){
		// dla kazdego wierzcholka
		int val = 0;
		int i1, i2, i3;
		
		for (Punkt p1: wierzcholki){
			i1 = indeks(p1);
			i2 = -1;
			//dla kazdek krawedzi
			for (Krawedz k : getSasiedzi(i1)) {
				Punkt p2 = k.getB();
				// szukamy anomalii
				i3 = czyIstniejeSciezkaWDwuchKrokach(p1, p2);
				// przekierowywujemy przeplyw 
				if (i3!=-1){
					i2 = indeks(p2);
					val = k.getPrzeplyw();
					getKrawedz(i1, i3).zwiekszPrzeplyw(val);
					getWierzcholek(i3).dodajPrzeplyw(val);
					getKrawedz(i3, i2).zwiekszPrzeplyw(val);
					break;
				}	// if
			}	// for k
			
			// usuwamy anomalie (w for (K:collection) nie mozna!!!)
			if (i2!=-1){
				usunKrawedz(i1, i2);
			}	// if
		}	// for p1
	}	// usunAnomalie_1_2
	
	/**         i2                     i2
	 *          / \	                    |
	 *         /   i4                  i4
	 *         |   |                    |
	 *         \   i3           =>     i3 
	 *          \ /                     |
	 *           i1                    i1
	 *
	 */
	private void usunAnomalie_1_3(){
		// dla kazdego wierzcholka
		int val = 0;
		int i1, i2, i3l;
		
		for (Punkt p1: wierzcholki){
			i1 = indeks(p1);
			i2 = -1;
			//dla kazdek krawedzi
			for (Krawedz k : getSasiedzi(i1)) {
				Punkt p2 = k.getB();
				// szukamy anomalii
				int[] ret = czyIstniejeSciezkaWTrzechKrokach(p1, p2);
				int i3 = ret[0];
				int i4 = ret[1];
				// przekierowywujemy przeplyw 
				if (i3!=-1){
					i2 = indeks(p2);
					val = k.getPrzeplyw();
					getKrawedz(i1, i3).zwiekszPrzeplyw(val);
					getWierzcholek(i3).dodajPrzeplyw(val);
					
					getKrawedz(i3, i4).zwiekszPrzeplyw(val);
					getWierzcholek(i4).dodajPrzeplyw(val);
					
					getKrawedz(i4, i2).zwiekszPrzeplyw(val);
					break;
				}	// if
			}	// for k
			
			// usuwamy anomalie (w for (K:collection) nie mozna!!!)
			if (i2!=-1){
				usunKrawedz(i1, i2);
			}	// if
		}	// for p1
	}	// usunAnomalie
	
	/** usuwa anomalie:
	 * usuwanie dziala tylko gdy stopien (wychodzacy) wierzcholkow nie przekracza 2              
	 *               p4
	 *   p4           |
	 *  / \          p3
	 * p2  p3   =>    |
	 *  \ /          p2
	 *   p1           |
	 *               p1
	 */
	private void usunAnomalie_2_2(){
		int i1 = -1, i2 = -1, i3 = -1, i4 = -1;
		Punkt p2 = null, p3 = null, p4 = null;
		
		// szukanie anomalii
		for (Punkt p1: wierzcholki){
			i1 = indeks(p1);
			if ( getSasiedzi(i1).size() <= 1)
				continue;
			
			p2 = getSasiedzi(i1).get(0).getB();
			p3 = getSasiedzi(i1).get(1).getB();
			p4 = znajdzWspolnegoPotomka(p2, p3);
			
			
			if (p4 != null){
				// usuwanie anomalii
				i2 = indeks(p2);
				i3 = indeks(p3);
				i4 = indeks(p4);
				
				if (czyKrawedzIstnieje(i2, i3)){
					//System.err.printf("Graf.usunAnomalie2-2(): krawedz %2d-%2d istnieje\n", i2, i3);
					continue;
				}	// if
				
				int przeplyw_1_3 = getKrawedz(i1, i3).getPrzeplyw();
				int  przeplyw_2_4 = getKrawedz(i2, i4).getPrzeplyw();
				
				getKrawedz(i1, i2).zwiekszPrzeplyw( przeplyw_1_3 );
				getKrawedz(i3, i4).zwiekszPrzeplyw( przeplyw_2_4 );
				dodajKrawedz(i2, i3,  przeplyw_1_3 + przeplyw_2_4);
				usunKrawedz(i1, i3);
				usunKrawedz(i2, i4);
			}	// if 
		}	// for p1
	}	// usunAnomalie
	
	
	/** zwraca pierwszego wspolnego potomka lub null jezeli takowy nie istnieje
	 * przed uzyciem sprawdzac czy istnieje wspolny potomek
	 * 
	 * @param p1 punkt1
	 * @param p2 punkt2
	 * @return wspolny poromek w grafie lub null
	 */
	 public Punkt znajdzWspolnegoPotomka(Punkt p1, Punkt p2){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		if (i1 == i2 || i1 == -1 || i2 == -1)
			return null;
		
		// robimy listy sasiadow dla p1 i p2
		int sasiedzi1[] = new int[ getSasiedzi(i1).size()];
		for (int i=0; i<sasiedzi1.length; i++){
			sasiedzi1[i] = indeks(getSasiedzi(i1).get(i).getB());
		}	// for
		
		int sasiedzi2[] = new int[ getSasiedzi(i2).size()];
		for (int i=0; i<sasiedzi2.length; i++){
			sasiedzi2[i] = indeks(getSasiedzi(i2).get(i).getB());
		}	// for
		
		// wiem ze petla kwadratowa, ale jezeli stopien jest oszacowany przez 2 to to dziala w czasie 
		// mniej lub bardziej stalym (O(4))
		
		for (int i=0; i< sasiedzi1.length; i++){
			for (int j=0; j< sasiedzi2.length; j++){
				if (sasiedzi1[i] == sasiedzi2[j])
					return wierzcholki.get(sasiedzi1[i]);
			}	// for j
		}	// for i
		
		return null;
	}	// znajdz potomka
	
	/** czy p1 i p2 maja wspolnego potomka 
	 * @param p1 
	 * @param p2 != p1
	 * @return
	 */
	public boolean czyIstniejeWspolnyPotomek(Punkt p1, Punkt p2){
		return znajdzWspolnegoPotomka(p1, p2) != null;
	}	// czy istnieje wspolny potomek
	
	
	/** wyszukuje najblizszy pod wzgledem odleglosci wierzcholek do p
	 * @param p punkt pomiarowy
	 * @return wierzcholek w G najblizsz do p
	 */
	public Punkt znajdzNajblizszyWierzcholek(Punkt p){
		int najI = 0;
		double najO = Punkt.odleglosc(getWierzcholek(0), p);
		for (int i=0; i<n; i++){
			double odl = Punkt.odleglosc(getWierzcholek(i), p);
			if (odl < najO){
				najO = odl;
				najI = i;
			}	// fi
		}	// for i
		return getWierzcholek(najI);
	}	// znajdzNajblizszyWierzcholek()

	/** wyszukuje najblizszy punkt do punktu p, ktory nie jest polaczony sciezka do, 
	 * ani z p, i lezy blizej niz maxodl
	 *  zwraca NULL jezelli taki punkt nie istnieje
	 * @param - p punkt startowy
	 * @param maxOdl - maksymalna odleglosc od p
	 * @return najblizszy do p punkt
	 */
	private Punkt znajdzWierzcholekDoPolaczenia(Punkt p, double maxOdl){
		Punkt ret = null;
		double najOdl = Double.MAX_VALUE; 
		// ta scierzka jest dobra ale w pewnym przykladzie uniemozliwia uzystanie optymalnego rozwiazania (cykl!!)
		// update: nieaktualne wraz z modyfikowaniem przeplywu skad - dokad
		for (Punkt s : wierzcholki){
			double d = Punkt.odleglosc(p,s);
			if (d< maxOdl && d < najOdl){
				if (!czyIstniejeSciezka(p,s) && !czyIstniejeSciezka(s,p)){
					najOdl = d;
					ret = s;
				}	// if
			}	// if
		}	// for
		
		return ret;
	}	// znajdzWierzcholekDoPolaczenia()
	
	/** Przesuwa punkt p wraz z sasiadami w strone colowyP
	 * p %in V ; G= (V, E)
	 * @param p - punkt przesuwany
	 * @param celowyP - punkt docelowy do niego p jest przesuwany
	 * @param t - il krokow
	 * @param T - maksymalny krok
	 */
	private void przesunWierzcholkiKohonen(Punkt p, Punkt celowyP, int t, int T){
		double a = 1 - ((double)t-1.0)/T;
		int indeks = indeks(p);
		
		// przesuwamy wierzcholek
		if (p.getTyp() == Punkt.TYP_INNY){
			double x = (1-a)* p.getX() + a* celowyP.getX();
			
			double y = (1-a)* p.getY() + a* celowyP.getY();
			
			if (czyWierzcholekIstnieje(new Punkt(x, y) ) == true){
				return;
				// nie mozna przesunac
			}	// if
			p.setX( x );
			p.setY( y );
		}	// fi
		
		// i jego sasiadow wychodzacych
		for (Krawedz k : getSasiedzi(indeks)){
			Punkt p1 = k.getB();
			if (p1.getTyp()!= Punkt.TYP_INNY){
				continue;
			}	// if
			double x = (1-a/4)* p1.getX() + a/4* celowyP.getX();
			double y = (1-a/4)* p1.getY() + a/4* celowyP.getY();
			
			if (czyWierzcholekIstnieje(new Punkt(x, y) ) == true){
				continue;
				// nie mozna przesunac
			}	// if
			p1.setX(  x );
			p1.setY(  y );
		}	// for sasiedzi out
		
		// i chodzacych
		for (Krawedz k : getSasiedziWchodzacy(indeks)){
			Punkt p1 = k.getA();
			if (p1.getTyp()!= Punkt.TYP_INNY){
				continue;
			}	// if
			double x = (1-a/4)* p1.getX() + a/4* celowyP.getX();
			
			double y = (1-a/4)* p1.getY() + a/4* celowyP.getY();
			
			if (czyWierzcholekIstnieje(new Punkt(x, y) ) == true){
				continue;
				// nie mozna przesunac
			}	// if
			
			p1.setX( x );
			p1.setY( y );
		}	// for sasiedzi in
		
	}	// przesunWierzcholki

	
	/* ******************************************
	 * 
	 *  PRZESUWANIE ROZGALEZIEN
	 * 
	 * ******************************************/
	
	/**   przesuwa jednego z dwuch potomkow wychodzacych z p1 wzdłuz lub w tył sciezki
	 * w tyl mozliwe tylko gdy p1 ma jednego  sasiada wchodzacego
	 * p2 nie moze byc odbiorca
	 * przyjmowane z prawdopodobienstwem zaleznym od zmiany kosztu i temperatuury beta 
	 * (patrz dynamika Boltzmana)
	 * jezeli zadna modyfikacja nie jest mozliwa dodawany jest posredni punkt na krawedzi nastepnej
	 *
	 * @param p1 - punkt z ktorego wychodzi dwoch potomkow (przesuwany jest losowy z nich)
	 * @param beta - temperatura odwrotna - partz dynamika boltzmana
	 *             - beta >=0  i rosnie, beta <= 0  zmiana ZAWSZE zaakceptowana 
	 * 
	 *    p2   p3     p2 -lub- p3
	 *     \  /        \     /
	 *      p1          p1  / lub
	 *      |           |  /
	 *      p4           p4
	 */
	private void modyfikacjaPrzesunRozgalezienieWychodzaceZWierzcholka(Punkt p1, double beta){
		int i1 = indeks(p1);
		if (getSasiedzi(i1).size() <=1)
			return;
		
		// tu i2 i3 sa numerami sasiada 
		int i3 = r.nextInt(getSasiedzi(i1).size());
		int i2 = -1;
		
		do { 
			i2 = r.nextInt(getSasiedzi(i1).size());
		} while (i2==i3);
		
		
		// od tad i2 i3 sa indeksami wierzcholka w grafie
		Punkt p2 = getSasiedzi(i1).get(i2).getB();
		i2 = indeks(p2);
		Punkt p3 = getSasiedzi(i1).get(i3).getB();
		i3 = indeks(p3);
		Punkt p4 = null;
		
		boolean czyMoznaPrzesunaCWstecz = false;
		// musi byc dokladnie jeden poprzednik
		if (getSasiedziWchodzacy(i1).size()==1){
			
			p4 = getSasiedziWchodzacy(i1).get(0).getA();
			int i4 = indeks(p4);
			
			if ( czyKrawedzIstnieje(i4, i3) == false && czyKrawedzIstnieje(i3, i4)== false){
				czyMoznaPrzesunaCWstecz = true;
			}	// if
			
			if (getSasiedzi(i4).size() >1){
				czyMoznaPrzesunaCWstecz = false;
			}	// if
		}	// if
		
		
		boolean czyMoznaPrzesunaCWprzod= true;
		// mozna jezeli 
		// p2 jest tranzywowy i
		// ma jednego potomka i
		// nie ma krawerzi miedzi p2-p3 ani p3-p2
		if (getSasiedzi(i2).size()>=2 || p2.getTyp() != Punkt.TYP_INNY 
				|| czyKrawedzIstnieje(i2, i3) || czyKrawedzIstnieje(i3, i2)){
			czyMoznaPrzesunaCWprzod = false;
		}	//if
		
		
		
		
		// jezeli mozna oba to wybieramy jeden z p-em = .5
		if (czyMoznaPrzesunaCWprzod && czyMoznaPrzesunaCWstecz)
			czyMoznaPrzesunaCWprzod &= r.nextBoolean();
		
		// wykona sie tylko jeden z nich!!!
		if (czyMoznaPrzesunaCWprzod){
			przesunRozgalezienieWychodzaceWprzod(p1, p2, p3, beta);
			return;
		}	// if
		
		if (czyMoznaPrzesunaCWstecz){
			przesunRozgalezienieWychodzaceWstecz(p1, p4, p3, beta);
			return;
		}	// if
		
	}	// PrzesunRozgalezienieWychodzaceZWierzcholka
	
	/**Przesuwa p1 p2 i p3 jak nizej
	 * Zachowac szczegolna ostroznosc!, nie sa sprawdzane warunki wykonalnosci przesuwania!
	 * 
	 *    p2 -X- p3     p2 ---> p3
	 *     \    /        \     
	 *       p1          p1 
	 *      
	 *  to MOZE STWORZYC CYKL!!!!
	 *  
	 *  PETLA 1-3    CYKL 3-el
	 *   
	 *    p2          p2 <--p4
	 *    |  \        | \    ^
	 *  ^ |    p4     |  \    \
	 *  | |     |     |   \    \
	 *    |    p3     |    --> p3
	 *     \   /      |     
	 *       p1       p1 
	 *  
	 *  
	 */
	private void przesunRozgalezienieWychodzaceWprzod(Punkt p1, Punkt p2, Punkt p3, double beta){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		int i3 = indeks(p3);
		
		if (czyKrawedzIstnieje(i2, i3) || czyKrawedzIstnieje(i3, i2)){
			return;
		}	// fi
		
		int przeplyw = getKrawedz(i1, i3).getPrzeplyw();
		
		double koszt1 = obliczKosztKrawedzi(getKrawedz(i1, i3)); 
		koszt1 += obliczKosztKrawedzi(getKrawedz(i1, i2));
		
		dodajKrawedz(i2, i3, przeplyw);
		getKrawedz(i1, i2).zwiekszPrzeplyw( przeplyw);
		
		double koszt2 = obliczKosztKrawedzi(getKrawedz(i2, i3)); 
		koszt2 += obliczKosztKrawedzi(getKrawedz(i1, i2));
		
		// sprawdzani czy jest cykl
		if (czyIstniejeCykl(p2)){
			// odrzcamy bez zastanowienia
			usunKrawedz(i2, i3);
			getKrawedz(i1, i2).zwiekszPrzeplyw( -przeplyw);
			return;
		}	// if 
		
		
		// akceptowanie / odrzucania
		// jesli koszt zmalal
		if (koszt2 < koszt1){
			usunKrawedz(i1, i3);
			return;
		}	//if
		
		// jesli wzrosl to akceptujemu z prawdopodobiensatwem
		if (r.nextDouble() < Math.exp(- beta *(koszt2 - koszt1))){
			usunKrawedz(i1, i3);
			return;
		}	// if
		
		// wpw odrzucamy
		usunKrawedz(i2, i3);
		getKrawedz(i1, i2).zwiekszPrzeplyw( -przeplyw);

		
	}	// przesunRozgalezienieWychodzaceWprzod
	
	
	/**Przesuwa p1 p2 i p3 jak nizej
	 * Zachowac szczegolna ostroznosc!, nie sa sprawdzane warunki wykonalnosci przesuwania!
	 * 
	 *      p3          p3
	 *    /             / 
	 *  p1         p1  /
	 *  |            \/
	 *   p2          p2
	 */
	private void przesunRozgalezienieWychodzaceWstecz(Punkt p1, Punkt p2, Punkt p3, double beta){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		int i3 = indeks(p3);
		
		int przeplyw = getKrawedz(i1, i3).getPrzeplyw();
		if (getKrawedz(i2, i1).getPrzeplyw() < przeplyw ){
			System.err.format("graf.przesunRozgalezienieWychodzaceWstecz() przeplyw poprzedniej krawedzi jest za maly\n");
			return;
		}	// if
		
		
		double koszt1 = obliczKosztKrawedzi(getKrawedz(i1, i3)); 
		koszt1 += obliczKosztKrawedzi(getKrawedz(i2, i1));
		
		dodajKrawedz(i2, i3, przeplyw);
		getKrawedz(i2, i1).zwiekszPrzeplyw( -przeplyw);
		
		double koszt2 = obliczKosztKrawedzi(getKrawedz(i2, i3)); 
		koszt2 += obliczKosztKrawedzi(getKrawedz(i2, i1));
		
		// akceptowanie / odrzucania 
		if (koszt2 < koszt1){
			usunKrawedz(i1, i3);
			return;
		}	//if
		
		if (r.nextDouble() < Math.exp(- beta *(koszt2 - koszt1))){
			usunKrawedz(i1, i3);
			return;
		}	// if
		
		usunKrawedz(i2, i3);
		getKrawedz(i2, i1).zwiekszPrzeplyw( +przeplyw);

		
	}	// przesunRozgalezienieWychodzaceWstecz
	
	
	/** przesuwa jednego z dwuch rodzicow wchodzacych do p1 wzdłuz lub w tył sciezki
	 * w przod mozliwe tylko gdy p1 ma jednego sasiada wychodzacego
	 * przyjmowane z prawdopodobienstwem zaleznym od zmiany kosztu i temperatuury beta 
	 * (patrz dynamika Boltzmana)
	 * jezeli zadna modyfikacja nie jest mozliwa dodawany jest posredni punkt na krawedzi poprzedniej
	 *
	 * @param p1 - punkt, do ktorego wychodzi dwoch potomkow (przesuwany jest losowy z nich)
	 * @param beta - temperatura odwrotna - partz dynamika boltzmana
	 *             - beta >=0  i rosnie, beta <= 0  zmiana ZAWSZE zaakceptowana 
	 * 
	 *      p4             p4
	 *      |               |  \    
	 *      p1             p1   ?
	 *     /  \            /     \
	 *    p2   p3        p2  -?-- p3
	 */
	private void modyfikacjaPrzesunRozgalezienieWchodzaceDoWierzcholka(Punkt p1, double beta){
		int i1 = indeks(p1);
		
		Vector<Krawedz> sasiedziWchdzacyi1 = getSasiedziWchodzacy(i1);
		
		if (sasiedziWchdzacyi1.size() <=1)
			return;
		
		// tu i2 i3 sa numerami sasiada 
		int i3 = r.nextInt(sasiedziWchdzacyi1.size());
		int i2 = -1;
		
		do { 
			i2 = r.nextInt(sasiedziWchdzacyi1.size());
		} while (i2==i3);
		
		
		// od tad i2 i3 sa indeksami wierzcholka w grafie
		Punkt p2 = sasiedziWchdzacyi1.get(i2).getA();
		i2 = indeks(p2);
		Punkt p3 = sasiedziWchdzacyi1.get(i3).getA();
		i3 = indeks(p3);
		Punkt p4 = null;
		
		boolean czyMoznaPrzesunacWprzod = false;
		// i1 ma przynajmniej jednego sasiada - sasiad istnieje
		if (getSasiedzi(i1).size()==1){
			
			p4 = getSasiedzi(i1).get(0).getB();
			int i4 = indeks(p4);
			
			if ( czyKrawedzIstnieje(i3, i4) == false && czyKrawedzIstnieje(i4, i3)== false){
				czyMoznaPrzesunacWprzod = true;
			}	// if
			
			// dodatkowo i4 ma miec jednego nastepnika
			if (getSasiedzi(i4).size()>1){
				czyMoznaPrzesunacWprzod = false;
			}	// if
		}	// if
		
		
		boolean czyMoznaPrzesunacWstecz= true;
		if (getSasiedziWchodzacy(i2).size()>=2 || p2.getTyp() != Punkt.TYP_INNY 
				|| czyKrawedzIstnieje(i2, i3) || czyKrawedzIstnieje(i3, i2))
			czyMoznaPrzesunacWstecz = false;
		
		
		
		
		// jezeli mozna oba to wybieramy jeden z p-em = .5
		if (czyMoznaPrzesunacWprzod && czyMoznaPrzesunacWstecz)
			czyMoznaPrzesunacWprzod &= r.nextBoolean();
		
		
		if (czyMoznaPrzesunacWprzod){
			przesunRozgalezienieWchodzaceWprzod(p1, p4, p3, beta);
			return;
		}	// if
		
		if (czyMoznaPrzesunacWstecz){
			przesunRozgalezienieWchodzaceWstecz(p1, p2, p3, beta);
			return;
		}	// if
	}	// PrzesunRozgalezienieWchodzaceDoWierzcholka
	
	
	/**Przesuwa p1 p2 i p3 jak nizej
	 * Zachowac szczegolna ostroznosc!, nie sa sprawdzane warunki wykonalnosci przesuwania!
	 * 
	 *      p1             p1   
	 *     /  \            /    
	 *    p2   p3        p2 <-- p3
	 *      
	 *    TO MOZE STWORZYC CYKL !!!
	 *    
	 *    p1          p1
	 *    |  \        |       
	 *    |    p3     |    +---p3 
 	 *    |    |      |    |    |
	 *    |    p4      \   |   p4
	 *    |  /          \  \/  / 
	 *    p2               p2
	 *           
	 */
	private void przesunRozgalezienieWchodzaceWstecz(Punkt p1, Punkt p2, Punkt p3, double beta){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		int i3 = indeks(p3);
		
		if (czyKrawedzIstnieje(i2,i3) || czyKrawedzIstnieje(i3, i2)){
			return;
		}	// fi
		
		int przeplyw = getKrawedz(i3, i1).getPrzeplyw();
		
		double koszt1 = obliczKosztKrawedzi(getKrawedz(i3, i1)); 
		koszt1 += obliczKosztKrawedzi(getKrawedz(i2, i1));
		
		dodajKrawedz(i3, i2, przeplyw);
		getKrawedz(i2, i1).zwiekszPrzeplyw( +przeplyw);
		
		double koszt2 = obliczKosztKrawedzi(getKrawedz(i3, i2)); 
		koszt2 += obliczKosztKrawedzi(getKrawedz(i2, i1));
		
		
		// sprawdzanie czy powstal cykl
		if (czyIstniejeCykl(p2)){
			// odrzuc!
			usunKrawedz(i3, i2);
			getKrawedz(i2, i1).zwiekszPrzeplyw(-przeplyw);
			return;
		}	// if
		
		// akceptowanie / odrzucania 
		if (koszt2 < koszt1){
			usunKrawedz(i3, i1);
			return;
		}	//if
		
		if (r.nextDouble() < Math.exp(- beta *(koszt2 - koszt1))){
			usunKrawedz(i3, i1);
			return;
		}	// if
		
		usunKrawedz(i3, i2);
		getKrawedz(i2, i1).zwiekszPrzeplyw( -przeplyw);
		
	}	// przesunRozgalezienieWchodzaceWtyl
	
	
	
	
	/** Przesuwa p1 p2 i p3 jak nizej
	 * Zachowac szczegolna ostroznosc!, nie sa sprawdzane warunki wykonalnosci przesuwania!
	 * 
	 *      p2             p2
	 *      |               | \    
	 *      p1             p1  \
	 *        \                 \
	 *         p3                p3
	 *    
	 */
	private void przesunRozgalezienieWchodzaceWprzod(Punkt p1, Punkt p2, Punkt p3, double beta){
		int i1 = indeks(p1);
		int i2 = indeks(p2);
		int i3 = indeks(p3);
		
		int przeplyw = getKrawedz(i3, i1).getPrzeplyw();
		
		double koszt1 = obliczKosztKrawedzi(getKrawedz(i3, i1)); 
		koszt1 += obliczKosztKrawedzi(getKrawedz(i1, i2));
		
		dodajKrawedz(i3, i2, przeplyw);
		getKrawedz(i1, i2).zwiekszPrzeplyw( -przeplyw);
		
		double koszt2 = obliczKosztKrawedzi(getKrawedz(i3, i2)); 
		koszt2 += obliczKosztKrawedzi(getKrawedz(i1, i2));
		
		// akceptowanie / odrzucania 
		if (koszt2 < koszt1){
			usunKrawedz(i3, i1);
			return;
		}	//if
		
		if (r.nextDouble() < Math.exp(- beta *(koszt2 - koszt1))){
			usunKrawedz(i3, i1);
			return;
		}	// if
		
		usunKrawedz(i3, i2);
		getKrawedz(i1, i2).zwiekszPrzeplyw( +przeplyw);

	}	// przesunRozgalezienieWchodzaceWprzod
	
	/** szuka cyklu z punktu p
	 * tj z p da sie dojsc do p
	 * @param p wierzcholek
	 * @return
	 */
	private boolean czyIstniejeCykl(Punkt p){
		if (czyWierzcholekIstnieje(p) == false ){
			return false;
		}	// if
		Vector<Punkt> kolejka = new Vector<Punkt>();
		boolean[] czyOdwiedzone = new boolean[getIloscWierzcholkow()];
		
		for (int i=0; i< getIloscWierzcholkow(); i++ ){
			czyOdwiedzone[i] = false;
		}	// for

		int ip = indeks(p);
		czyOdwiedzone[ip] = true;
		kolejka.add(p);

		while (kolejka.size() >0){
			Punkt q = kolejka.remove(0);
						
			for (Krawedz k : getSasiedzi(indeks(q))){
				Punkt s = k.getB();
				
				if (s.equals(p)){
//					System.out.format("cykl!!! p= %s\n", p);
					return true;
				}	// if
				
				int is = indeks(s);
				
				if (czyOdwiedzone[is] == false){
					czyOdwiedzone[is] = true;
					kolejka.add(s);
				}	// if
				
			}	// for sasiedzi q
		}	// while	
//		System.out.format("ok\n", p);
		return false;
	}	// czyIstniejeCykl()
	
	/* **************************************************
	 * 
	 * LOSOWE MODYFIKACJE
	 * 
	 * **************************************************/
	
	private static  double prawdopodobienstwoUsunieciaWierzcholka(double x, double min, double max){
		if (x<min){
			return 1;
		}	// if
		if (x>max){
			return 0;
		}	// if
		if (min == max){
			return 0;
		}	// if
			
		return (x-min) / (max - min);
	}	//prawdopodobienstwoUsunieciaWierzcholka()


	/** w losowy sposob przesowa rozgalezienia w grafie, przyjmuje zmiany z p-em 
	 * zaleznym od temperatury beta i zmiany kosztu
	 * 
	 * @param beta - temperatura odwrotna - patrz dynamika boltzmanna
	 * 		beta >0 i rosnie; beta<=0 => zmiany sa zawsze akceptowane
	 */
	public void losowaModyfikacjaPrzesuniecieRozgalezienia(double beta){
		Punkt p = null;
		int i = -1;
		int j=1;
		if (r.nextBoolean()){
			do {
				p = getWierzcholekLosowy();
				i = indeks(p);
				j++;
				if (j>5){
					return;
				}	// if
				// te j<10 to takie zabezpieczenie zeby petla nie dzialala w nieskonczonosc
				// jak nie ma w grafie odpowiedniego wierzcholka
			} while (getSasiedzi(i).size()<=1  || p.getTyp()!= Punkt.TYP_INNY);
			
			modyfikacjaPrzesunRozgalezienieWychodzaceZWierzcholka(p, beta);
				
		} else {
			do {
				p = getWierzcholekLosowy();
				i = indeks(p);
				j++;
				if (j>5){
					return;
				}	//if
			} while (getSasiedziWchodzacy(i).size()<=1  || p.getTyp()!= Punkt.TYP_INNY);
			
			modyfikacjaPrzesunRozgalezienieWchodzaceDoWierzcholka(p, beta);
			
		}	// if .. else
//		System.out.printf("k \n ");
	}	// losowaModyfikacjaPrzesuniecieRozgalezienia
	
	/** przesuwa wierzcholek o N(0, sigmaKW)^2 z prawdopodobienstwem zaleznym od 
	 * zmiany energii (kosztu grafu)
	 * 
	 * @param beta Temperatura Odwrotna = 1/T (patrz dynamika Boltzmana)
	 * 		Beta>0 i wraz z postepem algorytmu powinno rosnac
	 */
	public void losowaModyfikacjaPrzesuniecieLosowe(double beta){
		int i1 = r.nextInt(n);
		Punkt p1 = getWierzcholek(i1);
		
		if (p1.getTyp() != Punkt.TYP_INNY){
			return;
		}	// if
		
		double koszt1 = obliczKosztWierzcholka(p1);
//		for (Krawedz k : getSasiedzi(i1)){
//			koszt1 += obliczKosztKrawedzi(k);
//		}	// for krawedz
//		for (Krawedz k : getSasiedziWchodzacy(i1)){
//			koszt1 += obliczKosztKrawedzi(k);
//		}	// for krawedz
		
		double dx = r.nextGaussian()*sigma, dy = r.nextGaussian()*sigma;
		
		if (czyWierzcholekIstnieje(new Punkt(p1.getX()+dx, p1.getY()+dy))== true){
			return;
		}	// if
			
		p1.przesun(dx,dy);
		
		double koszt2 = obliczKosztWierzcholka(p1);
//		for (Krawedz k : getSasiedzi(i1)){
//			koszt2 += obliczKosztKrawedzi(k);
//		}	// for krawedz
//		for (Krawedz k : getSasiedziWchodzacy(i1)){
//			koszt2 += obliczKosztKrawedzi(k);
//		}	// for krawedz
		
		
		
		
		// jezeli koszt zmalal akceptujemy
		if (koszt2< koszt1){
			return;
		}	// fi
		
		// wpw akceptujemy z pstwem exp(-Beta(k2-k1))
		double prog = Math.exp(-beta*(koszt2-koszt1) );
		if (r.nextDouble() < prog){
			return;
		}	// if
		
		// WPW odrzucamy zmiane (czyt cofamy)
		p1.przesun(-dx,-dy);
		
	}	//	lisiwaModyfikacjaPrzesuniecie()
	

	/*    (nast)                 (nast)
	 *       |                      | 
	 *     (p1)          =>         |
	 * 	     |                      |
	 *     (pop)                  (pop)
	 */
	/** usuwa losowy wierzcholek o 1 wejsciu i jednym wyjsciu
	 * p-stwo usuniecia zalezne od odleglosci do sasiadow 
	 * bo od zmiany energii nie ma sensu (zawsze zmaleje!!!)
	 */
	public void losowaModyfikacjaUsuniecieWierzcholka(){
		int i1 = r.nextInt(n);
		Punkt p1 = getWierzcholek(i1);
		
		if (getSasiedzi(i1).size()!= 1 || getSasiedziWchodzacy(i1).size() != 1){
			return;
		}	// if
		
		Punkt pop = getSasiedziWchodzacy(i1).get(0).getA();
		int ipop = indeks(pop);
		Punkt nast = getSasiedzi(i1).get(0).getB();
		int inast = indeks(nast);
		if (czyKrawedzIstnieje(ipop, inast)== true){
			return;
		}	// if

		double dlugosc = getSasiedziWchodzacy(i1).get(0).getDlugosc(); 
		dlugosc += getSasiedzi(i1).get(0).getDlugosc();
		
		double prog = prawdopodobienstwoUsunieciaWierzcholka(dlugosc, 
				minDystans, maxDystans);
		if (r.nextDouble() < prog){
			int przeplyw = getSasiedzi(i1).get(0).getPrzeplyw();
			dodajKrawedz(ipop, inast, przeplyw);
			usunWierzcholek(p1);
		}	// if
		
		
		
	}	// losowaModyfikacjaUsuniecieWierzcholka()
	
	
	/** Dodaje wierzcholek na srodku krawedzi z p-stwem zaleznym od dlugosci krawedzi
	 * im dluzsza krawedz tym wieksze prawdopodobienstwo
	 */
	public void losowaModyfikacjaDodanieWierzcholka(){
		int i1 = r.nextInt(n);
		Punkt p1 = getWierzcholek(i1);
		
		if (getSasiedzi(i1).size()==0){
			return;
		}	// if
		
		Krawedz k = getSasiedzi(i1).get(r.nextInt(getSasiedzi(i1).size()));
		
		double dlugosc = k.getDlugosc();
		/* 0 = mindystans * a + b
		 * 1 = MaxDystans * a + b
		 * 1 - 0 = a * (M-m)
		 * a = 1/(M-m)
		 * b = -ma = -m/(M-m) 
		 */
		double prog = 1 - prawdopodobienstwoUsunieciaWierzcholka(dlugosc, 
				minDystans, maxDystans);
		
		if (r.nextDouble() > prog){
			return;
		}	// if
		
		Punkt p2 = k.getB();
		int i2 = indeks(p2);
		int przeplyw = k.getPrzeplyw();
		double x = p1.getX()/2 + p2.getX()/2; 
		// x+= r.nextGaussian()*sigmaKw;
		double y = p1.getY()/2 + p2.getY()/2;
		//y += r.nextGaussian()*sigmaKw;
		
		Punkt p3 = new Punkt(x, y, Punkt.TYP_INNY, przeplyw);
		if (czyWierzcholekIstnieje(p3)== true){
			return;
		}	// if
		dodajWierzcholek(p3);
		
		int i3 = indeks(p3);
		dodajKrawedz(i1, i3, przeplyw);
		dodajKrawedz(i3, i2, przeplyw);
		usunKrawedz(i1, i2);
	}	//losowaModyfikacjaUsuniecieWierzcholka()
	
	/** laczy z pewnym p-em  (zaleznym od temperatury) dwa wierzcholki 
	 * oddalone o co najwyzej (promien)
	 * 
	 * @param temperatura - temperatura odwrotna tj t>= i powinno rosnac
	 * @param promien - promien w jakim powinny byc laczone wierzcholki 
	 */
	public void losowaModyfikacjaPolaczWierzcholki(double temperatura, double promien){
		Punkt cel = getMapaOdleglosci().losujPunkt();
		Punkt p1  = znajdzNajblizszyWierzcholek(cel);
		Punkt p2 = znajdzWierzcholekDoPolaczenia(p1, promien);
		if (p2 == null)
			return;
		
		polaczWierzcholki(p1, p2, temperatura);
	}	// modyfikacjaPolaczWierzcholki()
	
	/** wybiera losowy wierzcholek jesli moze rozdziela go na dwa i z pewnym prawdopodobienstwem 
	 * akcptuje zmiany
	 * 
	 * @param beta - temperatura odwrotna (b>0 i powinno rosnac) - patrz dynamika maszyn Boltzmanna
	 * 		  beta==0  => zmiana zawsze zaakceptowana
	 */
	public void losowaModyfikacjaRozlaczWierzcholki(double beta){
		Punkt cel = getMapaOdleglosci().losujPunkt();
		Punkt p   = znajdzNajblizszyWierzcholek(cel);
		if (r.nextDouble() < .1){
			rozdzielWierzcholek(p, beta);
		}	// if
	}	//modyfikacjaRozlaczWierzcholki()
	
	/** losowe przesyniecia wierzcholkow z dynamika kohonena
	 * @param t - aktualny ktok iteracji (sila przyciagania)
	 * @param maxT - maksymalny krok iteracji - sila przyciagania
	 */
	public void losowaModyfikacjaPrzesuniecieKohonena(int t, int maxT){
		Punkt cel = getMapaOdleglosci().losujPunkt();
		Punkt p   = znajdzNajblizszyWierzcholek(cel);
		// ptzesuwanie wierzcholkow kohonena nie zalezy od temperatury ale 
		// bezposrednio od ilosci iteracji
		przesunWierzcholkiKohonen(p, cel, t<maxT ? t :maxT-1, maxT);
	}	// modyfikacjaPrzesunWierzcholek()
	
	/* *********************************************
	 * 
	 * W Y S W I E T L A N I E
	 * 
	 *  wyswietlanie wywalone do PanelGraficzny.java :P
	 * ********************************************/ 
	
	/**
	 * @return zwraca komponent reprezentujacy graf
	 */
	public Component getPodgladGrafu(){
		return new PanelWyswietlajacyGraf( this );
	}	// getPodglad()
	
}	// class
