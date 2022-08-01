package v01;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;



/** mapa dostaw tj informacje
 * z jakiego zrodla
 * do jakiego odbiorcy 
 * ile jest transportowane
 * 
 * 2007-06-08
 * 		podstawowy kod, podglad
 * 2007-06-08
 * 		usuwanie zrodel i odbiorcow, edytor 
 * 2007-08-26
 * 		edytor (dialog) polaczen miedzy wezlami 
 * 2007-09-01
 * 		generowanie grafu na podstawie mapy
 * 2007-10-08
 * 		wymuszenie odswiezenia okna po zamknieciu dialogu zmian transportu
 * 2007-11-11
 * 		dodanie modyfikacji intensywnosci zrodla,
 * 		budowanie grafu z okreslana intensywnoscia zrodel
 * 2007-12-03
 * 		przeniesiono panel edytora do oddzielnego pliku
 * 2008-01-04
 * 		klonowanie
 * 2008-02-14
 * 		dodano intensywnosc dbiorcow (tj ile optymalnie odbiorca powinien przyjac)
 * 
 * @author Jarosaw Piersa
 */
public class MapaDostaw {
	private Random rand;
	private int IL_ZRODEL;
	private int IL_ODBIORCOW;
	
	private Vector<Punkt> zrodla = null;
	private Vector<Punkt> odbiorcy = null;
	private Vector<Integer> intensywnoscZrodla = null;
	private Vector<Integer> intensywnoscOdbiorcy= null;

	private int macierzTransportu[][] = null;
	
	public MapaDostaw(int ilzr, int ilod){
		IL_ODBIORCOW = ilod;
		IL_ZRODEL = ilzr;
		rand = new Random(System.currentTimeMillis());
		
		zrodla = new Vector<Punkt>();
		for (int i=0; i< IL_ZRODEL; i++)
			zrodla.add(new Punkt(Punkt.TYP_ZRODLO));
		intensywnoscZrodla = new Vector<Integer>();
		for (int i=0; i< IL_ZRODEL; i++)
			intensywnoscZrodla.add(1);
		
		
		odbiorcy = new Vector<Punkt>();
		for (int i=0; i< IL_ODBIORCOW; i++)
			odbiorcy.add(new Punkt(Punkt.TYP_ODBIORCA));
		intensywnoscOdbiorcy = new Vector<Integer>();
		for (int i=0; i< IL_ODBIORCOW; i++)
			intensywnoscOdbiorcy.add(1);
		
		macierzTransportu = new int[IL_ZRODEL][IL_ODBIORCOW];
		losujMacierzTransportu();
		
	}	// konstruktor
	

	public MapaDostaw clone(){
		MapaDostaw ret = new MapaDostaw(0,0);
		ret.IL_ZRODEL = IL_ZRODEL;
		ret.IL_ODBIORCOW = IL_ODBIORCOW; 
		
		ret.zrodla = new Vector<Punkt>();
		ret.intensywnoscZrodla = new Vector<Integer>();
		for (int i=0; i<IL_ZRODEL; i++){
			ret.zrodla.add(getZrodlo(i).clone());
			ret.intensywnoscZrodla.add(getIntensywnoscZrodla(i));
		}	// for
			
		
		ret.odbiorcy = new Vector<Punkt>();
		ret.intensywnoscOdbiorcy = new Vector<Integer>();
		for (int i=0; i<IL_ODBIORCOW; i++){
			ret.odbiorcy.add(getOdbiorca(i).clone());
			ret.intensywnoscOdbiorcy.add(getIntensywnoscOdbiorcy(i));
		}	// for
		
		ret.macierzTransportu = new int[IL_ZRODEL][IL_ODBIORCOW];
		for (int i=0; i<IL_ZRODEL; i++){
			for (int j=0; j<IL_ODBIORCOW; j++){
				ret.macierzTransportu[i][j] = macierzTransportu[i][j];
			}	// for j
		}	// for i
		return ret;
	}	// clone
	
	
	/** normalizowanie macierzy transportu
	 */
	public void losujMacierzTransportu(){
		for (int i=0; i< IL_ZRODEL; i++){
			for (int j=0; j< IL_ODBIORCOW; j++){
				macierzTransportu[i][j] = rand.nextBoolean()? rand.nextInt(5):0;
			}	// for j
		}	// for i
		normalizujMacierzTransportu();
	}	// losujMacierzTransportu
	
	/** normalizowanie macierzy transportu
	 * ma byc to macierz stochastyczna tj
	 * forall i=1..il_zrodel sum(j=i.. il_odborcow, A[i][j] ) == 1
	 *
	 */
	public void normalizujMacierzTransportu(){
		for (int i=0; i< IL_ZRODEL; i++){
			double s = 0;
			for (double d: macierzTransportu[i])
				s+=d;
			if (s!=0)
				for (int j=0; j<IL_ODBIORCOW; j++)
					macierzTransportu[i][j] /= s;
			else
				macierzTransportu[i][rand.nextInt(IL_ODBIORCOW)] = 1;
		}	// for i
	}	// normalizujMacoerzTransportu
	
	/** Zwraca intensywnosc zrodla i
	 * @param inteks zrodla
	 * @return intensywnosc zrodla, 0 jezeli zrodlo nie istnieje
	 */
	public int getIntensywnoscZrodla(int i){
		if (i<0 || i >=IL_ZRODEL){
			System.err.printf("MapaDostaw.getIntensywnoscZrodla(): bledny indeks %d\n", i);
			return 0;
		}	// if
		return intensywnoscZrodla.get(i); 
	}	// getIntensywnoscZrodla()
	
	/** Zwraca intensywnosc zrodla i
	 * @param inteks zrodla
	 * @param val >=0 nowa wartosc
	 */
	public void setIntensywnoscZrodla(int i, int val){
		if (i<0 || i >=IL_ZRODEL){
			System.err.printf("MapaDostaw.setIntensywnoscZrodla(): bledny indeks %d\n", i);
			return;
		}	// if
		if (val < 0 ){
			System.err.printf("MapaDostaw.setIntensywnoscZrodla(): wartosc <0  \n");
			return;
		}	// if
		intensywnoscZrodla.set(i, val); 
	}	// getIntensywnoscZrodla()
	
	/** Zwraca intensywnosc odbiorcy i
	 * @param inteks odbiorcy
	 * @return intensywnosc odbiorcy, 0 jezeli zrodlo nie istnieje
	 */
	public int getIntensywnoscOdbiorcy(int i){
		if (i<0 || i >=IL_ODBIORCOW){
			System.err.printf("MapaDostaw.getIntensywnoscOdbiorcy(): bledny indeks %d\n", i);
			return 0;
		}	// if
		return intensywnoscOdbiorcy.get(i); 
	}	// getIntensywnoscZrodla()
	
	/** Zwraca intensywnosc odbiorcy i
	 * @param inteks odbiorcy
	 * @param val >= 0 nowa wartosc 
	 */
	public void setIntensywnoscOdbiorcy(int i, int val){
		if (i<0 || i >=IL_ODBIORCOW){
			System.err.printf("MapaDostaw.setIntensywnoscOdbiorcy(): bledny indeks %d\n", i);
			return;
		}	// if
		if (val < 0 ){
			System.err.printf("MapaDostaw.setIntensywnoscOdbiorcy(): wartosc <0  \n");
			return;
		}	// if
		intensywnoscOdbiorcy.set(i, val); 
	}	// getIntensywnoscZrodla()
	
	/** zwraca wartosc transportu
	 * @param zrodlo - indeks zrodla
	 * @param odbiorca - indeks odbiorcy
	 * @return - wartosc transportu ze zrodla do odbiorcy z przedzialu (0,1)
	 */
	public int getTransport(int zrodlo, int odbiorca){
		if (zrodlo<0 || zrodlo >=IL_ZRODEL || odbiorca<0 || odbiorca >=IL_ODBIORCOW){
			System.err.printf("MapaDostaw.getTransport(): bledny indeks %d %d\n", zrodlo, odbiorca);
			return 0;
		}	// if
		return macierzTransportu[zrodlo][odbiorca];
	}	// getTransport()
	
	/** ustawia wartosc transporu z [zrodlo] do [odbiorca]
	 * @param zrodlo - indeks zrodla
	 * @param odbiorca - indeks odbiorcy
	 * @param val - wartosc transportu val >=0!!!
	 * po modyfikacjach nalezy przenormalizowac macierz!!!
	 */
	public void setTransport(int zrodlo, int odbiorca, int val){
		if (zrodlo<0 || zrodlo >=IL_ZRODEL || odbiorca<0 || odbiorca >=IL_ODBIORCOW || val<0){
			System.err.printf("MapaDostaw.setTransport(): bledny indeks lub wartosc %d %d %f\n", zrodlo, odbiorca, val);
			return;
		}	// if
		macierzTransportu[zrodlo][odbiorca] = val;
	}	// getTransport()


	public int getIlOdbiorcow() {
		return IL_ODBIORCOW;
	}

	public int getIlZrodel() {
		return IL_ZRODEL;
	}

	/**
	 * @return zwraca i-tego odbiorce
	 */
	public Punkt getOdbiorca(int i) {
		if (i>=0 && i<IL_ODBIORCOW)
			return odbiorcy.get(i);
		return null;
	}	// getZrodlo


	/**
	 * @return zwraca i-te zrodlo
	 */
	public Punkt getZrodlo(int i) {
		if (i>=0 && i<IL_ZRODEL)
			return zrodla.get(i);
		return null;
	}	// getOdbiorca
	
	/** Dodaje nowedo odbiorce do listy
	 * @param p nowy odbiorca
	 */
	public void dodajOdbiorce(Punkt p){
		p.setTyp(Punkt.TYP_ODBIORCA);
		odbiorcy.add(p);
		IL_ODBIORCOW++;
		
		int nowaMacierz[][]= new int[IL_ZRODEL][IL_ODBIORCOW];
		for (int i=0; i< IL_ZRODEL; i++){
			for (int j=0; j< IL_ODBIORCOW-1; j++){
				nowaMacierz[i][j] = macierzTransportu[i][j];
			}	// for j
		}	// for i
		
		intensywnoscOdbiorcy.add(1);
		
		macierzTransportu = null;
		macierzTransportu = nowaMacierz;
		normalizujMacierzTransportu();
	}	// dodajOdbiorce
	
	/** Dodaje nowe zrodlo do listy
	 * @param p nowe zrodlo
	 */
	public void dodajZrodlo(Punkt p){
		p.setTyp(Punkt.TYP_ZRODLO);
		zrodla.add(p);
		IL_ZRODEL++;
		
		int nowaMacierz[][]= new int[IL_ZRODEL][IL_ODBIORCOW];
		for (int i=0; i< IL_ZRODEL-1; i++){
			for (int j=0; j< IL_ODBIORCOW; j++){
				nowaMacierz[i][j] = macierzTransportu[i][j];
			}	// for j
		}	// for i
		
		macierzTransportu = null;
		macierzTransportu = nowaMacierz;
		normalizujMacierzTransportu();
		
		intensywnoscZrodla.add(1);
	}	// dodajZrodlo
	
	public int indeksZrodla(Punkt p){
		for (int i=0; i<IL_ZRODEL; i++){
			if (p.equals( zrodla.get(i)))
				return i;
		}	// for
		return -1;
	}	// indeksZrodla

	/** zwraca indeks odbiorcy p o ile taki istnieje
	 * zwraca -1 wpw
	 * @param p poszukiwany odbiorca
	 * @return indeks odbiorcy lub -1 jezeli nie istnieje
	 */
	public int indeksOdbiorcy(Punkt p){
		for (int i=0; i<IL_ODBIORCOW; i++){
			if (p.equals( odbiorcy.get(i)))
				return i;
		}	// for
		return -1;
	}	// indeksOdbiorcy
	
	/** usuwanie Odbiorcy z listy
	 * UWAGA!! po usunieciu odbiorcy automatycznie normalizowana jest macierz transportu
	 * @param p
	 */
	public void usunOdbiorce(Punkt p){
		int k = indeksOdbiorcy(p);
		if (k==-1)
			return;
		odbiorcy.removeElementAt(k);
		intensywnoscOdbiorcy.removeElementAt(k);
		IL_ODBIORCOW--;
		
		int nowaMacierz[][]= new int[IL_ZRODEL][IL_ODBIORCOW];
		for (int i=0; i < IL_ZRODEL; i++){
			for (int j=0; j< k; j++){
				nowaMacierz[i][j] = macierzTransportu[i][j];
			}	// for j
			for (int j=k; j < IL_ODBIORCOW; j++){
				nowaMacierz[i][j] = macierzTransportu[i][j+1];
			}	// for j
		}	// for i
		macierzTransportu = nowaMacierz;
		normalizujMacierzTransportu();
	}	// usunOdbiorce()
	
	/** usuwanie zrodla z listy zrodel
	 * @param p zrodlo do usuniecia
	 */
	public void usunZrodlo(Punkt p){
		int k = indeksZrodla(p);
		if (k==-1)
			return;
		zrodla.removeElementAt(k);
		intensywnoscZrodla.removeElementAt(k);
		IL_ZRODEL--;
		
		int nowaMacierz[][]= new int[IL_ZRODEL][IL_ODBIORCOW];
		for (int i=0; i<k; i++){
			for (int j = 0; j < IL_ODBIORCOW; j++){
				nowaMacierz[i][j] = macierzTransportu[i][j];
			}	// for j
		}	// for i
		
		for (int i=k; i < IL_ZRODEL; i++){
			for (int j = 0; j < IL_ODBIORCOW; j++){
				nowaMacierz[i][j] = macierzTransportu[i+1][j];
			}	// for j
		}	// for i
		macierzTransportu = nowaMacierz;
		//normalizujMacierzTransportu();
		
	}	// usunZrodlo();
	
	/** 
	 * @return sumaryczna intensywnosc wyplywajaca ze zrodel
	 */
	public int getSumaIntensywnosciZrodel(){
		int ret = 0;
		for (Integer d : intensywnoscZrodla)
			ret += d;
		return ret;
	}	// suma intensywnosciZrodel
	
	/** 
	 * @return sumaryczna intensywnosc wplywajaca do odbiorcow
	 */
	public int getSumaIntensywnosciOdbiorcow(){
		int ret = 0;
		for (Integer d : intensywnoscOdbiorcy)
			ret += d;
		return ret;
	}	// suma intensywnosciOdbiorcow
	
	/** zwraca podglad mapy
	 * @return podglad mapy
	 */
	public Component getPodgladMapy(){
		return new PodgladMapy();
	}	// getPodgladMapy
	
	/** zwraca podglad z mozliwoscia edycji, dodania usuniecia punktu
	 * @param parent glowne okno programu, zostanie zablokowane w trakcie edycji
	 * @return edytor mapy
	 */
	public JPanel getEdytorMapy(Frame parent){
		return new EdytorMapyDostaw(parent, this);
	}	// getPodgladMapy
	
	/** Generuje graf polaczen na podstawie konfiguracji wejsciowej problemu
	 * @deprecated use  Graf(MapaDostaw)
	 * @return graf o architekturze zblizonej dosieci poczen
	 */
	public Graf wygenerujGrafPolaczen(){
		Graf g = new Graf(0);
		double odstep = 25;
		
		// kopiujemy zrodla i odbiorcow
		for (int i=0; i<IL_ZRODEL; i++){
			Punkt z = zrodla.get(i);
			g.dodajWierzcholek(new Punkt(z.getX(), z.getY(), z.getTyp(), intensywnoscZrodla.get(i)));
		}	// for
		for (Punkt o : odbiorcy){
			double suma = 0;
			int j = indeksOdbiorcy(o);
			for (int i=0; i< IL_ZRODEL; i++)
				suma += macierzTransportu[i][j]*intensywnoscZrodla.get(i);
			g.dodajWierzcholek(new Punkt(o.getX(), o.getY(), o.getTyp(), suma));
			
		}	// for
		
		int przeplyw, n; 
		double d;
		double x1, x2, y1, y2;
		
		// dla kazdego polaczenia tworzymy lancuszek w grafie
		for (int i=0; i<IL_ZRODEL; i++){
			for (int j=0; j<IL_ODBIORCOW; j++){
				if (macierzTransportu[i][j]==0)
					continue;
				przeplyw =  macierzTransportu[i][j] * intensywnoscZrodla.get(i);
				d = Punkt.odleglosc( getZrodlo(i) , getOdbiorca(j));
				n = (int) (d / odstep);
				
				int pierwszyIndeks = g.indeks(getZrodlo(i));
				int ostatniIndeks = g.indeks(getOdbiorca(j));
				int poprzedniIndeks = pierwszyIndeks;
				
				for (int k=1; k<n; k++){
					x1 = getZrodlo(i).getX();
					x2 = getOdbiorca(j).getX();
					y1 = getZrodlo(i).getY();
					y2 = getOdbiorca(j).getY();
					
					Punkt nowy = new Punkt( x1 + (k*1.0/n) * (x2 - x1) , y1 + (k*1.0/n) * (y2-y1) , 
							Punkt.TYP_INNY, przeplyw );
					if (g.czyWierzcholekIstnieje(nowy) == false){
						g.dodajWierzcholek(nowy);
						int nowyIndeks = g.indeks(nowy);
						g.dodajKrawedz( poprzedniIndeks, nowyIndeks, przeplyw );
						poprzedniIndeks = nowyIndeks;
					}	// if
				}	// for k
				
				g.dodajKrawedz(poprzedniIndeks,ostatniIndeks, przeplyw);
				
				
			}	// for j
		}	// for i
		
		g.resetujKosztPoczatkowy();
		return g;
	}	// wygenerujGrafPolaczen()
	
	/** Dpodaje połączenia każdy do każdego dla pierwszego węzła w mapie o ile jeszcze nie iestnieją
	 * 
	 */
	public void dodajTransportPierwszyDoWszystkich(){
		int t=0;
		for (int i=0; i<getIlOdbiorcow(); i++){
			t = getTransport(0, i);
			setTransport(0, i, t==0 ? 1 : t);
			setIntensywnoscOdbiorcy(i, getIntensywnoscOdbiorcy(i) + (t==0 ? 1 : t) );
		}	// for
		
		setIntensywnoscZrodla(0, getIlOdbiorcow());
		
	}	// dodajPierwszyDoWszystkich()
	
	
	/** klasa do podlgadu mapy
	 * @author piersaj
	 */
	private class PodgladMapy extends Component {
		private static final long serialVersionUID = 1L;
		public PodgladMapy(){
			super();
			setPreferredSize(new Dimension((int)Punkt.MAX_X, (int)Punkt.MAX_Y));
		}	// konstruktor
		
		/** Podglad do mapy
		 */
		public void paint(Graphics arg0){
			super.paint(arg0);
			
			Graphics g = arg0.create();
			g.setColor(Color.WHITE);
			g.fillRect(0,0, getWidth()-1, getHeight()-1);
			
			
			for (int i=0; i<IL_ZRODEL; i++){
				Punkt z = getZrodlo(i);
				for (int j=0; j< IL_ODBIORCOW; j++){
					// 0 bialy
					// 10+ czarny
					int val = 255 - (int)(macierzTransportu[i][j]* intensywnoscZrodla.get(i) * 25.5);
					if (val>255)
						continue;
					
					g.setColor(new Color(val, val, val));
					Punkt o = getOdbiorca(j);
					g.drawLine((int)z.getX(), (int)z.getY(), (int)o.getX(), (int)o.getY());
				}	// for j
			}	// for i
			
			g.setColor(Punkt.KOLOR_PUNKTU[Punkt.TYP_ZRODLO]);
			for (Punkt p :zrodla){
				g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
			}	// for p
			
			g.setColor(Punkt.KOLOR_PUNKTU[Punkt.TYP_ODBIORCA]);
			for (Punkt p :odbiorcy){
				g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
			}	// for p
			
		}	// paint
	}	// class PodgladMapy
	
	
}	// class mapaDostaw
