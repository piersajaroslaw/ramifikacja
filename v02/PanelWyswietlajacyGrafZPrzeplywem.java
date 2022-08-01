package v02;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ConcurrentModificationException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import v01.Krawedz;
import v01.MapaDostaw;
import v01.Punkt;
import v03.GraphToSVGRenderer;

/** klasa podgladu grafu + reczne laczenie
 * 
 * 2008-01-03
 * 		utworzenie, a raczej skopiowanie z poprzedniego pliku
 * 2008-01-04
 * 		podglad przeplywu, ladne formatowanie
 * 2008-02-06
 * 		poprawiono wyglad przyciskow do zlaczania rozlaczania
 * 		dodano mozliwosc wyswietlenia przeplywu przez krawedz
 * 2008-02-14
 * 		dodano suwak i wyzwietlanie skalarow kosztow w grafie
 * 2008-02-20
 * 		dodano tablice do wyswietlania deklarowanego i faktycznego wplywu do odbiorcy
 * 		Usunieto polskie ogonki z napisow w programie
 * 2008-06-01
 * 		przeniesiono skalary i alpha do PanelNadzorujacy.java
 * 
 * TODO dopisac wyswietlanie kierunku strzalki
 * TODO pomyslen nad ogonkamiw unicode
 * 
 * @author Jarosaw Piersa
 */
public class PanelWyswietlajacyGrafZPrzeplywem extends JPanel implements ActionListener, ChangeListener, MouseMotionListener{
	private GrafWrazZPrzeplywem graf=null;
	
	private static final long serialVersionUID = 1L;
	private JButton buttMapa, buttSciezka, buttSVG;
	private JCheckBox chckMapa = null, chckNumery = null, chckPrzeplyw = null, chckPrzeplywNaKrawedzi = null;
	private JCheckBox chckPolacz = null, chckRozlacz = null;
	
	
	private boolean flagaNumer = false;
	private JSpinner spin1, spin2;
	private JLabel labPozycja = null;
	
	public PanelWyswietlajacyGrafZPrzeplywem(GrafWrazZPrzeplywem g){
		super();
		this.graf = g;
		setLayout(new BorderLayout());
		
		JPanel panE1 = new JPanel(new GridLayout(7,1,0,0));
		
		panE1.add(new JLabel(String.format("KPocz = %.2f", g.getKosztPoczatkowy())));
		
//		panE1.add(new JPanel());
		
		JPanel pan = new JPanel();
		chckMapa = new JCheckBox(new ImageIcon("./v01/ikony/odl2.gif"), false);
		chckMapa.setSelectedIcon(new ImageIcon("./v01/ikony/odl1.gif"));
		chckMapa.addChangeListener(this);
		pan.add(chckMapa);
		chckNumery = new JCheckBox(new ImageIcon("./v01/ikony/nr0.gif"), false);
		chckNumery.setSelectedIcon(new ImageIcon("./v01/ikony/nr1.gif"));
		chckNumery.addChangeListener(this);
		pan.add(chckNumery);
		panE1.add(pan);
		
		
		
		chckPrzeplyw = new JCheckBox(new ImageIcon("./v01/ikony/ikonaPrzeplyw.gif"),false);
		chckPrzeplyw.addActionListener(this);
		panE1.add(chckPrzeplyw);
		
		chckPrzeplywNaKrawedzi = new JCheckBox(new ImageIcon("./v01/ikony/ikonakrawedzBezPrzeplywu.gif") ,false);
		chckPrzeplywNaKrawedzi.setSelectedIcon(new ImageIcon("./v01/ikony/ikonakrawedzZprzeplywem.gif"));
		chckPrzeplywNaKrawedzi.addActionListener(this);
		
		pan = new JPanel();
		pan.add(chckPrzeplyw);
		pan.add(chckPrzeplywNaKrawedzi);
		panE1.add(pan);
	
		
		
		chckPolacz = new JCheckBox(new ImageIcon("./v01/ikony/ikona-zlaczenie.gif"),false);
		chckPolacz.addActionListener(this);
		panE1.add(chckPolacz);
		
		chckRozlacz= new JCheckBox(new ImageIcon("./v01/ikony/ikona-rozlaczenie.gif"),false);
		chckRozlacz.addActionListener(this);
		panE1.add(chckRozlacz);

		pan = new JPanel();
		pan.add(chckPolacz);
		pan.add(chckRozlacz);
		panE1.add(pan);
		
		spin1 = new JSpinner();
		spin1.setPreferredSize(new Dimension(35, 20));
		spin2 = new JSpinner();
		spin2.setPreferredSize(new Dimension(35, 20));
		
		pan = new JPanel();
		pan.add(spin1);
		pan.add(spin2);
		panE1.add(pan);
		

		
		labPozycja = new JLabel(String.format("x,y = %3d, %3d", 0, 0));
		panE1.add(labPozycja);
		
		buttSVG = new JButton("svg");
		buttSVG.addActionListener(this);
		panE1.add(buttSVG);
		
		
		JPanel panE = new JPanel();
		panE.add(panE1);
		
//		JPanel panS = new JPanel();
//		
//		buttSciezka = new JButton("Sciezka");
//		buttSciezka.addActionListener(this);
//		panS.add(buttSciezka);
//		
		add(panE, BorderLayout.EAST);
//		add(panS, BorderLayout.SOUTH);
		setPreferredSize(new Dimension((int)Punkt.MAX_X+150, (int)Punkt.MAX_Y ));
		addMouseMotionListener(this);
		repaint();
	}	// konstruktor
	
	public void paint(Graphics arg0) {
		super.paint(arg0);
		Graphics g = arg0.create();
		
		g.setColor(Color.white);
		g.fillRect(0,0,(int) Punkt.MAX_X, (int) Punkt.MAX_Y);
		
		// rysowanie mapy odleglosci
		if (chckMapa.isSelected() && graf.getMapaOdleglosci() != null){
			for (int i=0; i<(int) Punkt.MAX_X; i++)
				for (int j=0; j<(int) Punkt.MAX_Y; j++){
					float val = graf.getMapaOdleglosci().getMapa()[i][j];
					if (val>1) val = 1;
					if (val >0){
						int col = 255 - (int) (100*val );
						g.setColor(new Color(col, col, col));
						g.drawRect(i, j, 1,1);
					}	// if
				}	// for i,j
		}	// fi
		
		// rysowanie krawedzi
		boolean czyRysowacPrzeplywNaKrawedziach = chckPrzeplywNaKrawedzi.isSelected();
		
		try{
			
			for (int i=0; i<graf.getIloscWierzcholkow(); i++){
				Punkt q = graf.getWierzcholek(i);
				for (Krawedz k: graf.getSasiedzi(i)){
					/*int val = (int)(100 * (2.55-k.getPrzeplyw()));
					val = val<=0 ? 0 : val;
					g.setColor(new Color(val, val, 255));
					//g.setColor(Color.blue);
					 */
					g.setColor(kolorPrzeplywu(k.getPrzeplyw()));
					Punkt p = graf.getWierzcholek( graf.indeks(k.getB()) );
					g.drawLine((int)q.getX(), (int)q.getY(), (int)p.getX(), (int)p.getY());
					
					if (czyRysowacPrzeplywNaKrawedziach){
						int x =  (int) (q.getX() + p.getX()) / 2;
						int y =  (int) (q.getY() + p.getY()) / 2;
						g.setColor(Color.black);
						g.drawString(String.format("%2d", k.getPrzeplyw()), x, y);
					}	//
				}	// for j
			}	// for i
		
		
			for (Punkt p : graf.getWierzcholki()){
				g.setColor(Punkt.KOLOR_PUNKTU[p.getTyp()]);
				g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
				g.setColor(Color.black);
				if (flagaNumer==true)
					g.drawString(""+graf.indeks(p), (int)p.getX()-3, (int)p.getY()+3);
			}	// for p
		} catch (ConcurrentModificationException e) {
			// System.err.printf("%s\n", e);
		}  catch (Exception e) {
			// System.err.printf("%s\n", e);
		}	// catch
		
		//g.drawString(String.format("koszt = %.2f", graf.getKoszt() ),10, 10);
		g.drawString(String.format("%s", graf.obliczKosztStr() ),10, 10);
	}	// paint

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttMapa){
			graf.obliczMapeOdleglosci();
			repaint();
		} else if (arg0.getSource() == chckPolacz){
			int i1 = (Integer) spin1.getValue();
			int i2 = (Integer) spin2.getValue();
			graf.polaczWierzcholki( graf.getWierzcholek(i1), graf.getWierzcholek(i2));
			repaint();
			return;
		} else if (arg0.getSource() == chckRozlacz){
			int i1 = (Integer) spin1.getValue();
			graf.rozdzielWierzcholek( graf.getWierzcholek(i1));
			repaint();
			return;
		} else if (arg0.getSource() == buttSciezka){
			int i1 = (Integer) spin1.getValue();
			int i2 = (Integer) spin2.getValue();
			System.out.println(graf.getSciezka(graf.getWierzcholek(i1), graf.getWierzcholek(i2)));
			System.out.println(graf.czyIstniejeDokladnieJednaSciezka(graf.getWierzcholek(i1), graf.getWierzcholek(i2)));
			repaint();
			return;
		} else if (arg0.getSource() == chckPrzeplyw){
			//chckPrzeplyw.setSelected(false);
			new PodgladPolaczen(null, graf.getMapaDostaw());
			return;
			//repaint();
		} else if (arg0.getSource() == chckPrzeplywNaKrawedzi){
			repaint();
			return;
		} else if (arg0.getSource() == buttSVG){
			GraphToSVGRenderer r = new GraphToSVGRenderer(this.graf);
			r.renderGraphSnapshot();
			return;
		}	// if
		
	}	// actionPerformed()

	
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource() == chckMapa){
			if (graf.getMapaOdleglosci() == null)
				graf.obliczMapeOdleglosci();
			repaint();
		}	// fi
		if (arg0.getSource() == chckNumery){
			flagaNumer = chckNumery.isSelected();
			repaint();
		}	// fi
		
	}	// stateChanged()

	/** oznacza przeplyw kolorem zaleznym od intensywnosci
	 * @param przeplyw >=0
	 * @return kolor okreslajacy intensywnosc przeplywu
	 * 
	 * 
	 * TODO dostosowac do intow
	 */
	public static Color kolorPrzeplywu(int przeplyw){
		if (przeplyw <0)
			return Color.white;
		
		int val =(int) ((przeplyw - Math.floor(przeplyw))* 255);
		//System.out.println(val);
		
		// white -> blue
		if (przeplyw < 1)
			return	new Color(255- val, 255-val, 255);
		// blue -> teal
		if (przeplyw < 2)
			return	new Color(0, val, 255);
		// teal -> green
		if (przeplyw < 3)
			return	new Color(0, 255, 255-val);
		// green -> yellow
		if (przeplyw < 4)
			return	new Color(val, 255, 0);
		// yellow -> red
		if (przeplyw < 5)
			return	new Color(255, 255 - val, 0);
		// red -> violet
		if (przeplyw < 7)
			return	new Color(255, 0, val);
		return Color.decode("#ff00ff");
	}	// kolorPrzeplywu()

	public void mouseDragged(MouseEvent arg0) {
	}	// mouseDragged()

	public void mouseMoved(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		
		if (x >= Punkt.MAX_X){
			x= (int) Punkt.MAX_X-1;
		}	// if
		
		labPozycja.setText(String.format("x,y = %3d, %3d", x, y));
		
	}	// MouseMoved();
	
	/** Skopiowany dialog do pokazywania aktualnego przeplywu
	 * @author piersaj
	 */
	private class PodgladPolaczen extends JDialog implements ActionListener{
		private static final long serialVersionUID = 1L;
		private JButton buttOK;
		// tabela glowna
		private Vector<String> kolumny;
		private Vector<Vector<Object>> dane;
		private JTable tabela;
		
		// tabela intensywnosci zrodel
		private Vector<String> kolumnyZr;
		private Vector<Vector<Object>> daneZr;
		private JTable tabelaZr;
		
		
		// tabela intensywnosci Odbioprcow
		private Vector<String> kolumnyOdb;
		private Vector<Vector<Object>> daneOdb;
		private JTable tabelaOdb;
//		private MapaDostaw mapa;
		
		// parent jest glownym oknem ktore JEST BLOKOWANE przez ten dialog
		public PodgladPolaczen(Frame parent, MapaDostaw mapa){
			super(parent, true);
			setTitle("Podglad przeplywu");

			
			//panel S
			JPanel panS = new JPanel();
//			this.mapa = mapa;
			
			buttOK = new JButton("Zamknij");
			buttOK.addActionListener(this);
			panS.add(buttOK);
			
			// tabela
			kolumny = new Vector<String>();
			
			kolumny.add("Źródo / Odbiorca");
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){
				Punkt p = mapa.getOdbiorca(i);
				kolumny.add(p.toString());
			}
			
			dane = new Vector<Vector<Object>>();
			
			for (int i=0; i< mapa.getIlZrodel(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add(mapa.getZrodlo(i).toString());
				for( int j=0; j < mapa.getIlOdbiorcow(); j++)
					v.add( mapa.getTransport(i, j));
				dane.add(v);
			}	// for i
			
			// tabelaZr
			kolumnyZr = new Vector<String>();
			kolumnyZr.add("Źródło");
			kolumnyZr.add("Intensywność");
			
			
			daneZr = new Vector<Vector<Object>>();
			for (int i=0; i< mapa.getIlZrodel(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add(mapa.getZrodlo(i).toString());
				v.add( mapa.getIntensywnoscZrodla(i));
				daneZr.add(v);
			}	// for
			
			
			// tabelaOdbiorcy
			kolumnyOdb = new Vector<String>();
			kolumnyOdb.add("Odbiorca");
			kolumnyOdb.add("Wplyw deklarowany");
			kolumnyOdb.add("Wplyw faktyczny");
			
			daneOdb= new Vector<Vector<Object>>();
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add( mapa.getOdbiorca(i).toString());
				v.add( mapa.getIntensywnoscOdbiorcy(i));
				
				int val = 0;
				for (int j=0; j<mapa.getIlZrodel(); j++){
					val += mapa.getTransport(j, i) * mapa.getIntensywnoscZrodla(j);
				}	// for j
				
				v.add(val);
				
				daneOdb.add(v);
			}	// for i
			
			
			// pan W
			tabelaZr = new JTable(daneZr, kolumnyZr);
			JScrollPane scrollPaneW = new JScrollPane(tabelaZr);
			scrollPaneW.setPreferredSize(new Dimension(200, 200));
			tabelaZr.setModel(new TableModelZrodla());
			
			
			// panC
			tabela = new JTable(dane, kolumny);
			JScrollPane scrollPane2 = new JScrollPane(tabela);
			scrollPane2.setPreferredSize(new Dimension(400, 200));
			
			tabela.setModel(new TableModelTransport());
			for (int i=1; i<mapa.getIlOdbiorcow()+1; i++){
				tabela.getColumn(tabela.getColumnName(i)).setCellRenderer(new TableModelTransport());
			}	// for
			
			
			// panE
			tabelaOdb = new JTable(daneOdb, kolumnyOdb);
			JScrollPane scrollPaneE = new JScrollPane(tabelaOdb);
			scrollPaneE.setPreferredSize(new Dimension(300, 200));
			tabelaOdb.setModel(new TableModelOdbiorcy());
			
			// Okno
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(scrollPaneW, BorderLayout.WEST);
			getContentPane().add(scrollPaneE, BorderLayout.EAST);
			getContentPane().add(panS, BorderLayout.SOUTH);
			getContentPane().add(scrollPane2, BorderLayout.CENTER);
			
			
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pack();
			setVisible(true);
		}	// konstruktor

		public void actionPerformed(ActionEvent arg0) {
			dispose();
		}	// actionPerformed
		
		/* Modele do wyswietlania trzech tablic
		 * 
		 */
		
		private class TableModelTransport implements TableModel, TableCellRenderer {
			public int getRowCount() {
				return dane.size();
			}
			public int getColumnCount() {
				return kolumny.size();
			}
			public String getColumnName(int arg0) {
				return kolumny.get(arg0);
			}
			public Class<?> getColumnClass(int arg0) {
				if (arg0==0){
					return String.class;
				}	// if
				return Integer.class;
			}	
			public boolean isCellEditable(int r, int c) {
				return false;
			}
			public Object getValueAt(int r, int c) {
				return dane.get(r).get(c);
			}
			public void setValueAt(Object arg0, int r, int c) {
				dane.get(r).setElementAt(arg0, c);
			}
			public void addTableModelListener(TableModelListener arg0) {
			}
			public void removeTableModelListener(TableModelListener arg0) {
			}
			// cellRenderer
			JProgressBar bar = new JProgressBar(0, 100);
			
			{
			bar.setStringPainted(true);
			}	
			public Component getTableCellRendererComponent(JTable table, Object value,
	                            boolean isSelected, boolean hasFocus, int row, int column)					{
				int val = ((Integer)value).intValue();
				bar.setValue(val);
				bar.setString(String.format("%3d", val));
				return bar;
	    	}	// getTableCellRendererComponent
		}	// table modelClass
		
		private class TableModelZrodla implements TableModel{
			public int getRowCount() {
				return daneZr.size();
			}
			public int getColumnCount() {
				return kolumnyZr.size();
			}
			public String getColumnName(int arg0) {
				return kolumnyZr.get(arg0);
			}
			public Class<?> getColumnClass(int arg0) {
				if (arg0==0){
					return String.class;
				}	// if
				return Integer.class;
			}	
			public boolean isCellEditable(int r, int c) {
				return false;
			}
			public Object getValueAt(int r, int c) {
				return daneZr.get(r).get(c);
			}
			public void setValueAt(Object arg0, int r, int c) {
				daneZr.get(r).setElementAt(arg0, c);
			}
			public void addTableModelListener(TableModelListener arg0) {
			}
			public void removeTableModelListener(TableModelListener arg0) {
			}
		}	// table modelClass
		
		private class TableModelOdbiorcy implements TableModel{
			public int getRowCount() {
				return daneOdb.size();
			}
			public int getColumnCount() {
				return kolumnyOdb.size();
			}
			public String getColumnName(int arg0) {
				return kolumnyOdb.get(arg0);
			}
			public Class<?> getColumnClass(int arg0) {
				if (arg0==0){
					return String.class;
				}	// if
				return Integer.class;
			}	
			public boolean isCellEditable(int r, int c) {
				return false;
			}
			public Object getValueAt(int r, int c) {
				return daneOdb.get(r).get(c);
			}
			public void setValueAt(Object arg0, int r, int c) {
				daneOdb.get(r).setElementAt(arg0, c);
			}
			public void addTableModelListener(TableModelListener arg0) {
			}
			public void removeTableModelListener(TableModelListener arg0) {
			}
		}	// table modelClass
		
	}	// class
	
}	// panelGraficzny