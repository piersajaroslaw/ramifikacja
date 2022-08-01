package v03;

import java.util.Random;

import v01.MapaDostaw;
import v01.Punkt;

public class MapaDostawFactory {
	public static final int MAPA_DOSTAW_TROJKAT = 1;
	public static final int MAPA_DOSTAW_OKRAG = 2;
	public static final int MAPA_DOSTAW_PROSTOKAT = 3;
	public static final int MAPA_DOSTAW_KOLO = 4;
	public static final int MAPA_DOSTAW_TRAPEZ = 5;

	public static int MAPA_DOSTAW_MAX_TYP = 5;
	
	private static final double sqrt3 = Math.sqrt(3);

	
	private static Punkt atrapaZ = null;
	private static Punkt atrapaO = null;
	
	
	
	
	
	public static void ustawMape(MapaDostaw m){
		Random r = new Random();
		ustawMape(m, r.nextInt(MAPA_DOSTAW_MAX_TYP) +1);
	}
	
	public static void ustawMape(MapaDostaw m, int typ){
		switch (typ){
		case MAPA_DOSTAW_TROJKAT: 
			mapaTrojkat(m); 
			break;
		case MAPA_DOSTAW_KOLO:
			mapaKolo(m);
			break;
		case MAPA_DOSTAW_PROSTOKAT:
			mapaProstokat(m);
			break;
		case MAPA_DOSTAW_OKRAG:
			mapaOkrag(m);
			break;
		case MAPA_DOSTAW_TRAPEZ:
			mapaTrapez(m);
			break;
		default:
			Random r = new Random();
			ustawMape(m, r.nextInt(MAPA_DOSTAW_MAX_TYP) +1);
		}	// switch
	}	// ustawMape
	
	
	public static void mapaTrojkat(MapaDostaw m){
		double mx = Punkt.MAX_X;
		double my = Punkt.MAX_Y;
		
		
		dodajAtrapy(m);
		usunWierzcholki(m);
		
		
		double offset = 100;
		double a = mx- offset;
		double x = offset/2 + a/2;
		double y = offset/2 + a* sqrt3 /2;
		Punkt s = new Punkt(x, y, Punkt.TYP_ZRODLO, 2);
		m.dodajZrodlo(s);
		
		x = offset/2;
		y = offset/2;
		Punkt d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
		m.dodajOdbiorce(d1);
		
		x = offset/2 + a;
		y = offset/2;
		
		Punkt d2 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
		m.dodajOdbiorce(d2);
		
		usunAtrapy(m);

		
		m.setIntensywnoscZrodla(0, 2);
		m.setIntensywnoscOdbiorcy(0, 1);
		m.setIntensywnoscOdbiorcy(1, 1);
		m.setTransport(0, 0, 1);
		m.setTransport(0, 1, 1);
		
	}
	
	
	
	public static void mapaProstokat(MapaDostaw m){
		double mx = Punkt.MAX_X;
		double my = Punkt.MAX_Y;
		int i = 0;
		
		dodajAtrapy(m);
		usunWierzcholki(m);
		
		/*
		 * moze odbiorcy na poziomych i producenci na pionowych?
		 * 
		 * offset x --- x ---- x --- x --- x
		 *        |       distx            |
		 *        *                        *
		 *        |                        |
		 *        *                        *
		 *        | dist y                 |
		 *        *                        *
		 *        |                        |
		 * offset x --- x ---- x --- x --- x
		 *        
		 */
		
		double offset  = 50;
		int num = 20; 
		double distx = (mx -2 * offset) / num;
		double disty = (my -2 * offset) / num;
		
		double x = offset;
		double y = offset;
		Punkt s = new Punkt(x, y, Punkt.TYP_ZRODLO, 4 * num -4);
		m.dodajZrodlo(s);
		
		x = offset;
		y = my - offset;
		s = new Punkt(x, y, Punkt.TYP_ZRODLO, num);
		m.dodajZrodlo(s);
		
		x = mx - offset;
		y = my - offset;
		s = new Punkt(x, y, Punkt.TYP_ZRODLO, num);
		m.dodajZrodlo(s);
		
		x = mx - offset;
		y = offset;
		s = new Punkt(x, y, Punkt.TYP_ZRODLO, num);
		m.dodajZrodlo(s);
		
		
		for (i=1; i< num; i++){
			x = offset + i * distx;
			y = offset;
			Punkt d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
			
			x = offset + i * distx;
			y = my - offset;
			d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
			
			x = offset ;
			y = offset + i * disty;
			d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
			
			x = mx - offset;
			y = offset + i * disty;
			d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
		}
		usunAtrapy(m);
		
		
		for (i=0; i< 4; i++){
			m.setIntensywnoscZrodla(i,  4* num -4);
			
			for (int j=0; j< 4*num-4; j++){
				m.setIntensywnoscOdbiorcy(j, 4);
				m.setTransport(i, j, 1);
			}
		}
		
	}	// set to prostokat
	

	public static void mapaOkrag(MapaDostaw m){
		double mx = Punkt.MAX_X;
		double my = Punkt.MAX_Y;
		
		dodajAtrapy(m);
		usunWierzcholki(m);
		
		int i=0;
		
		
		double R = (Math.min(mx,my)-100) / 2;
		int num = 100; 
		
		double x = mx / 2;
		double y = my / 2;
		Punkt s = new Punkt(x, y, Punkt.TYP_ZRODLO, num);
		m.dodajZrodlo(s);
		
		
		for (i=0; i< num; i++){
			x = mx/2 + R * Math.cos( 2 * Math.PI * i / num);
			y = my/2 + R * Math.sin( 2 * Math.PI * i / num);
			Punkt d1 = new Punkt(x, y, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
		}
		usunAtrapy(m);

		m.setIntensywnoscZrodla(0,  num);
		
		for (i=0; i< num; i++){
			m.setIntensywnoscOdbiorcy(i, 1);
			m.setTransport(0, i, 1);
		}	// for
		
	}	// set to kolo
	
	

	public static void mapaKolo(MapaDostaw m){
		double mx = Punkt.MAX_X;
		double my = Punkt.MAX_Y;
		
		Random r = new Random();
		int i = 0;
		
		dodajAtrapy(m);
		usunWierzcholki(m);
		
		
		double R = (Math.min(mx,my)-100) / 2;
		int num = 300; 
		double x = mx / 2;
		double y = my / 2 + R;
		Punkt s = new Punkt(x, y, Punkt.TYP_ZRODLO, num);
		m.dodajZrodlo(s);
		
		for (i=0; i< num; i++){
			do {
				x = r.nextDouble() * 2 * R - R;
				y = r.nextDouble() * 2 * R - R;
			} while ( x * x + y * y > R * R );
			Punkt d1 = new Punkt(x + mx / 2, y +my / 2, Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
		}
		usunAtrapy(m);
		
		m.setIntensywnoscZrodla(0,  num);
		
		for (i=0; i< num; i++){
			m.setIntensywnoscOdbiorcy(i, 1);
			m.setTransport(0, i, 1);
		}
		
	}	// set to kolo
	
	public static void mapaTrapez(MapaDostaw m){
		double mx = Punkt.MAX_X;
		int i = 0;
		
		dodajAtrapy(m);
		usunWierzcholki(m);

		
		double offset  = 50;
		int num = 100; 
		double distx = (mx -2 * offset) / num;
				
		for (i=0; i<=num; i++){
			double x = offset + i * distx;
			Punkt d1 = new Punkt(x, mx - offset, Punkt.TYP_ZRODLO, 1);
			m.dodajZrodlo(d1);
			
			d1 = new Punkt(x, offset , Punkt.TYP_ODBIORCA, 1);
			m.dodajOdbiorce(d1);
		}
		usunAtrapy(m);
		
		
		for (i=0; i<= num; i++){
			m.setIntensywnoscZrodla(i,  1);
			m.setIntensywnoscOdbiorcy(i, 1);
			m.setTransport(i, i, 1);
			for (int j=i+1; j<=num; j++){
				m.setTransport(i, j, 0);
				m.setTransport(j, i, 0);

			}	// for j
		}	// for i
	}	// ustawTrapez

	
	
	private static void dodajAtrapy(MapaDostaw m){
		if (atrapaZ == null){
			atrapaZ = new Punkt(1, 1, Punkt.TYP_ODBIORCA, 1);
		}
		if (atrapaO == null){
			atrapaO = new Punkt(Punkt.MAX_X-1, Punkt.MAX_Y-1, Punkt.TYP_ZRODLO, 1);	
		}
		m.dodajZrodlo(atrapaZ);
		m.dodajOdbiorce(atrapaO);
	}
	
	private static void usunWierzcholki(MapaDostaw m){
		int i=0;
		while (m.getIlZrodel() >= 2){
			Punkt p = m.getZrodlo(i);
			if (!p.equals(atrapaZ)){
				m.usunZrodlo(p);
			} else {
				i++;
			}
		} 	// while
		
		i=0;
		while (m.getIlOdbiorcow() >= 2){
			Punkt p = m.getOdbiorca(i);
			if (!p.equals(atrapaO)){
				m.usunOdbiorce(p);
			} else {
				i++;
			}
		}	// while
	}	// usunWierzcholki
	
	private static void usunAtrapy(MapaDostaw m){
		m.usunOdbiorce(atrapaO);
		m.usunZrodlo(atrapaZ);
	}	// usunAtrapy()
	
}	// class
