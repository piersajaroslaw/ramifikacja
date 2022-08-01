package v01;

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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import v03.MapaDostawFactory;


/** Edytor Mapy Dostaw
 * 
 * 2007-06-26
 * 		utworzenie poczatkowo w klasie MapaDostaw
 * 2007-12-13
 * 		przeniesienie do oddzielnego pliku
 * 2007-02-14
 * 		dodano opcje ustawienia intensywnosci odbiorcy
 * 		poprawki dotyczace klas implementujacych interfejsy dla tabel
 * 		pomniejsze korekty
 * 2008-06-01
 * 		Poprawiono napisy na unicode
 * 		Dodano normalizowanie sumatycznej intensywnosci zrodel i odbiorcow
 * 		Blokada usuwania ostatniego zrodla / odbiorcy
 * 		Blokada ustawienia zerowej intensywnosci zrodel
 * 
 * @author Jarosaw Piersa
 */
public class EdytorMapyDostaw extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private JToggleButton buttZr, buttOdb;
	private JButton buttNormalizuj, buttLosuj, buttWagi;
	
	private JButton buttTrojkat, buttF2a;// TODO dopisac ten button!
	
	private final Frame parent;
	private MapaDostaw mapa = null;
	private Random r = new Random(System.currentTimeMillis());
	private JLabel labPozycjaKursoraX, labPozycjaKursoraY = null;
	
	
	// obserwator wymusza odzwierzenie okna po zamknieciu dialogu z wagami
	private static Obserwator obs = new Obserwator();
	

	
	public EdytorMapyDostaw(Frame parent, MapaDostaw mapa){
		super();
		this.parent = parent;
		this.mapa = mapa;
		setLayout(new BorderLayout());
		
		
		// panE
		JPanel panE = new JPanel();
		JPanel panEprzyciski = new JPanel(new GridLayout(9,1));
		buttZr = new JToggleButton(new ImageIcon("./v01/ikony/src.gif"));
		buttZr.setSelected(true);
		buttZr.addActionListener(this);
		buttZr.setToolTipText("Dodaj lub usu\u0144 \u017ar\u00f3d\u0142o");
		panEprzyciski.add(buttZr);

		buttOdb = new JToggleButton(new ImageIcon("./v01/ikony/dest.gif"));
		buttOdb.addActionListener(this);
		buttOdb.setToolTipText("Dodaj lub usu\u0144 odbiorc\u0119");
		panEprzyciski.add(buttOdb);
		
		// normalizacja juz chyba nie bedzie potrzebna
//		buttNormalizuj = new JButton(new ImageIcon("./v01/ikony/norm.gif"));
//		buttNormalizuj.addActionListener(this);
//		buttNormalizuj.setToolTipText("Normalizuj Transport");
//		panEprzyciski.add(buttNormalizuj);
		
		buttLosuj = new JButton(new ImageIcon("./v01/ikony/los.gif"));
		buttLosuj.addActionListener(this);
		buttLosuj.setToolTipText("Losuj Transport");
		panEprzyciski.add(buttLosuj);
		
		buttWagi = new JButton(new ImageIcon("./v01/ikony/wagi.gif"));
		buttWagi.addActionListener(this);
		buttWagi.setToolTipText("Ustaw Transport");
		panEprzyciski.add(buttWagi);
		
		buttTrojkat = new JButton("Trk"/*new ImageIcon("./v01/ikony/wagi.gif") */);
		buttTrojkat.addActionListener(this);
		buttTrojkat.setToolTipText("Ustawienie trojkatne");
		panEprzyciski.add(buttTrojkat);

		
		buttF2a = new JButton("f2a"/*new ImageIcon("./v01/ikony/wagi.gif") */);
		buttF2a.addActionListener(this);
		buttF2a.setToolTipText("Połącz pierwsze źródło z każdym odbiorcą");
		panEprzyciski.add(buttF2a);

		
		labPozycjaKursoraX = new JLabel("x= 0");
		panEprzyciski.add(labPozycjaKursoraX);
		labPozycjaKursoraY = new JLabel("y= 0");
		panEprzyciski.add(labPozycjaKursoraY);
		
		panE.add(panEprzyciski);
		
		
		addMouseListener(this);
		addMouseMotionListener(this);
		add(panE, BorderLayout.EAST);
		setPreferredSize(new Dimension((int)Punkt.MAX_X + 60, (int)Punkt.MAX_Y));
		
		EdytorMapyDostaw.obs.addObserver(new Observer() {
		        // reakcja na zarejestrowan obserwacj
		        public void update(Observable o, Object arg) {
		        	repaint();
		        }	// update
	    	}	// Observer
		);	// addObserver
	}	// konstruktor
	
	public void paintComponent(Graphics arg0){
		Graphics g = arg0.create();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,(int) Punkt.MAX_X, (int) Punkt.MAX_Y);
		
		for (int i=0; i<mapa.getIlZrodel(); i++){
			Punkt z = mapa.getZrodlo(i);
			for (int j=0; j< mapa.getIlOdbiorcow(); j++){
				// 0 bialy
				// 10+ czarny
				int val = 255 - (int)(mapa.getTransport(i,j)* mapa.getIntensywnoscZrodla(i) * 25.5);
				if (val>245)
					continue;
				if (val <0)
					val = 0;
				
				g.setColor(new Color(val, val, val));
				Punkt o = mapa.getOdbiorca(j);
				g.drawLine((int)z.getX(), (int)z.getY(), (int)o.getX(), (int)o.getY());
			}	// for j
		}	// for i
		
		g.setColor(Punkt.KOLOR_PUNKTU[Punkt.TYP_ZRODLO]);
		
		for (int i=0; i< mapa.getIlZrodel(); i++){
			Punkt p = mapa.getZrodlo(i); 
			g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
		}	// for p
		
		g.setColor(Punkt.KOLOR_PUNKTU[Punkt.TYP_ODBIORCA]);
		
		for (int i=0; i< mapa.getIlOdbiorcow(); i++){
			Punkt p = mapa.getOdbiorca(i); 
			g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
		}	// for p
	}	// paint

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttLosuj){
			mapa.losujMacierzTransportu();
			repaint();
			return;
		}	// if
		
		if (arg0.getSource() == buttNormalizuj){
			repaint();
			mapa.normalizujMacierzTransportu();
			return;
		}	// if
		
		if (arg0.getSource() == buttZr){
			buttOdb.setSelected(false);
			buttZr.setSelected(true);
			return;
		}	// if
		
		if (arg0.getSource() == buttOdb){
			buttOdb.setSelected(true);
			buttZr.setSelected(false);
			return;
		}	// if
		
		if (arg0.getSource() == buttWagi){
			new EdytorPolaczen(parent);
		}	// if
		
		if (arg0.getSource().equals(buttTrojkat)){
			MapaDostawFactory.ustawMape(mapa);
			repaint();
			return;
		}	// if

		
		if (arg0.getSource().equals(buttF2a)){
			mapa.dodajTransportPierwszyDoWszystkich();
			repaint();
			return;
		}	// if
		
	}	// actionPerformed()

	public void mouseClicked(MouseEvent arg0) {
		boolean czyZrodlo = buttZr.isSelected();
		Punkt p = new Punkt(arg0.getX(), arg0.getY(), czyZrodlo ? Punkt.TYP_ZRODLO : Punkt.TYP_ODBIORCA);
		Punkt.setPrecyzyjnePorownywaniePunktow(false);
		
		// lmb - dodawanie
		if (arg0.getButton() ==  MouseEvent.BUTTON1){
			if (czyZrodlo)
				mapa.dodajZrodlo(p);
			else
				mapa.dodajOdbiorce(p);
		}	// if
		
		// rmb - usuwanie
		if (arg0.getButton() ==  MouseEvent.BUTTON3){
			if (czyZrodlo){
				if (mapa.getIlZrodel() == 1){
					JOptionPane.showMessageDialog(this, "Musi by\u0107 przynajmniej jedno \u017ar\u00f3d\u0142o.","B\u0142\u0105d",
							JOptionPane.ERROR_MESSAGE);
					return;
				}	// if
				mapa.usunZrodlo(p);
			} else {
				if (mapa.getIlOdbiorcow() == 1){
					JOptionPane.showMessageDialog(this, "Musi by\u0107 przynajmniej jeden odbiorca.","B\u0142\u0105d",
							JOptionPane.ERROR_MESSAGE);
					return;
				}	// if
				mapa.usunOdbiorce(p);
			}	// if .. else
		}	// if
					
		Punkt.setPrecyzyjnePorownywaniePunktow(true);
		repaint();
	}	// mouse clicked

	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	
	
	public void mouseDragged(MouseEvent arg0) {
		return;
	}

	public void mouseMoved(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		
		x = x>=Punkt.MAX_X ? (int) Punkt.MAX_X -1 : x;
		
		labPozycjaKursoraX.setText("x= "+x);
		labPozycjaKursoraY.setText("y= "+y);
		
	}	// mouseMoved
	
	
	/** okienko do recznego ustawienia transportu
	 * UWAGA nie dodawac / usuwac zrodel i odbiorcow w czasie edycji polaczen!!!
	 * @author Jarosaw Piersa
	 */
	private class EdytorPolaczen extends JDialog implements ActionListener {
		private static final long serialVersionUID = 1L;
		private JButton buttOK, buttAnuluj, buttZast, buttNormalizujZrodla, buttNormalizujOdbiorcow;
		private JLabel labSumaZrodel, labSumaOdbiorcow;
		
		// tabela glowna
		private Vector<String> kolumny;
		private Vector<Vector<Object>> dane;
		private JTable tabela;
		
		// tabela intensywnosci zrodel
		private Vector<String> kolumnyZr;
		private Vector<Vector<Object>> daneZr;
		private JTable tabelaZr;
		
		// tabela intensywnosci odbiorcow
		private Vector<String> kolumnyOdb;
		private Vector<Vector<Object>> daneOdb;
		private JTable tabelaOdb;
		
		// parent jest glownym oknem ktore JEST BLOKOWANE przez ten dialog
		public EdytorPolaczen(Frame parent){
			super(parent, true);
			setTitle("Edytor Przeplywu");

			
			//panel S
			JPanel panS = new JPanel();
			
			buttOK = new JButton("OK");
			buttOK.addActionListener(this);
			panS.add(buttOK);
			
			buttAnuluj = new JButton("Anuluj");
			buttAnuluj.addActionListener(this);
			panS.add(buttAnuluj);
			
			buttZast = new JButton("Zastosuj");
			buttZast.addActionListener(this);
			panS.add(buttZast);
			
			// tabela
			kolumny = new Vector<String>();
			
			kolumny.add("\u0179r\u00f3do / Odbiorca");
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){
				Punkt p = mapa.getOdbiorca(i);
				kolumny.add(p.toString());
			}
			
			dane = new Vector<Vector<Object>>();
			
			for (int i=0; i< mapa.getIlZrodel(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add(mapa.getZrodlo(i).toString());
				for( int j=0; j < mapa.getIlOdbiorcow(); j++)
					v.add((Integer) mapa.getTransport(i, j));
				dane.add(v);
			}	// for i
			
			// tabelaZr
			kolumnyZr = new Vector<String>();
			kolumnyZr.add("\u0179r\u00f3d\u0142o");
			kolumnyZr.add("Intensywno\u015b\u0107");
			
			
			daneZr = new Vector<Vector<Object>>();
			for (int i=0; i< mapa.getIlZrodel(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add(mapa.getZrodlo(i).toString());
				v.add((Integer) mapa.getIntensywnoscZrodla(i));
				daneZr.add(v);
			}	// for
			
			// tabel odb
			kolumnyOdb = new Vector<String>();
			kolumnyOdb.add("Odbiorca");
			kolumnyOdb.add("Intensywno\u015b\u0107");
			
			
			daneOdb = new Vector<Vector<Object>>();
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){
				Vector<Object> v = new Vector<Object>();
				v.add(mapa.getOdbiorca(i).toString());
				v.add((Integer) mapa.getIntensywnoscOdbiorcy(i));
				daneOdb.add(v);
			}	// for
			
			
			// pan W
			tabelaZr = new JTable(daneZr, kolumnyZr);
			JScrollPane scrollPane1 = new JScrollPane(tabelaZr);
			scrollPane1.setPreferredSize(new Dimension(200, 200));
			
			tabelaZr.setModel(new TableModelZr());
			tabelaZr.getColumn(tabelaZr.getColumnName(1)).setCellEditor(new TableModelZr());
			tabelaZr.getColumn(tabelaZr.getColumnName(1)).setCellRenderer(new TableModelZr());
			
			
			// panC
			tabela = new JTable(dane, kolumny);
			JScrollPane scrollPane2 = new JScrollPane(tabela);
			scrollPane2.setPreferredSize(new Dimension(400, 200));
			
			tabela.setModel(new TableModelClass());
			for (int i=1; i<mapa.getIlOdbiorcow()+1; i++){
				tabela.getColumn(tabela.getColumnName(i)).setCellEditor(new TableModelClass());
				tabela.getColumn(tabela.getColumnName(i)).setCellRenderer(new TableModelClass());
			}	// for
			
			// panE
			
			// pan E
			tabelaOdb = new JTable(daneOdb, kolumnyOdb);
			JScrollPane scrollPaneE = new JScrollPane(tabelaOdb);
			scrollPaneE.setPreferredSize(new Dimension(200, 200));
			
			tabelaOdb.setModel(new TableModelOdb());
			tabelaOdb.getColumn(tabelaOdb.getColumnName(1)).setCellEditor(new TableModelOdb());
			tabelaOdb.getColumn(tabelaOdb.getColumnName(1)).setCellRenderer(new TableModelOdb());

			// panN
			JPanel panN = new JPanel();
			
			buttNormalizujZrodla = new JButton("");
			buttNormalizujZrodla.addActionListener(this);
			panN.add(buttNormalizujZrodla);
			
			
			labSumaZrodel = new JLabel();
			labSumaZrodel.setText(String.format("suma \u017ar\u00f3de\u0142= %3d", sumaIntensywnosciZrodel()));
			panN.add(labSumaZrodel);
			
			labSumaOdbiorcow = new JLabel();
			labSumaOdbiorcow.setText(String.format("suma odbioru = %3d", mapa.getSumaIntensywnosciOdbiorcow()));
			panN.add(labSumaOdbiorcow);
			
			buttNormalizujOdbiorcow = new JButton("");
			buttNormalizujOdbiorcow.addActionListener(this);
			panN.add(buttNormalizujOdbiorcow);
			
			// Okno
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panN, BorderLayout.NORTH);
			getContentPane().add(scrollPane1, BorderLayout.WEST);
			getContentPane().add(panS, BorderLayout.SOUTH);
			getContentPane().add(scrollPane2, BorderLayout.CENTER);
			getContentPane().add(scrollPaneE, BorderLayout.EAST);
			
			
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pack();
			setVisible(true);
		}	// konstruktor

		private int sumaIntensywnosciOdbiorcow(){
			int ret = 0;
			for (Vector<Object> v : daneOdb){
				ret += (Integer)(v.get(1));
			}	// for
			return ret;
		}	// sumaIntensywnosciRodbiorcy
		
		private int sumaIntensywnosciZrodel(){
			int ret = 0;
			for (Vector<Object> v : daneZr){
				ret += (Integer) v.get(1);
			}	// for
			return ret;
		}	// sumaIntensywnosciRodbiorcy
		
		
		/* ActionListener
		 */
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource()== buttAnuluj){
				dispose();
				return;
			}	// if
			if (arg0.getSource() == buttOK){
				zapiszPolaczenia();
				tabela.repaint();
				this.repaint();
				
				EdytorMapyDostaw.obs.setChanged();
				EdytorMapyDostaw.obs.notifyObservers("polaczenia ok");
				dispose();
			}	// if
			if (arg0.getSource() == buttZast){
				zapiszPolaczenia();
				tabela.repaint();
				this.repaint();
				
				EdytorMapyDostaw.obs.setChanged();
				EdytorMapyDostaw.obs.notifyObservers("polaczenia zastosuj");
			}	// if
			
			if (arg0.getSource().equals(buttNormalizujZrodla)){
				normalizujIntensywnoscZrodel();
				repaint();
				return;
			}	// if
			
			if (arg0.getSource().equals(buttNormalizujOdbiorcow)){
				normalizujIntensywnoscOdbiorcow();
				repaint();
				return;
			}	// if
			

			
		}	// actionPerformed

		/** normalizuje intensywnosc zrodel tak by pasowaly do sumarycznej
		 *  intensywnosci odbiorcow
		 *  
		 *  
		 *  TODO przepisac ten algorytm
		 */
		private void normalizujIntensywnoscZrodel(){
			if (1+1==2){
				return;
			}
			
			@SuppressWarnings("unused")
			int intensywnoscOdbiorcow = sumaIntensywnosciOdbiorcow();
			int intensywnoscZrodel = sumaIntensywnosciZrodel();
			if (intensywnoscOdbiorcow == 0){
				JOptionPane.showMessageDialog(this, "Sumaryczne intensywno\u015bci nie mog\u0105 by\u0107 zerowe",
						"B\u0142\u0105d", JOptionPane.ERROR_MESSAGE);
				return;
			}	// fi
			
			// testujemy czy sa zera w tablicy
			for (int i=0; i<mapa.getIlZrodel() ; i++){
				if ( (Integer)tabelaZr.getValueAt(i, 1) == 0){
					tabelaZr.setValueAt(0.1, i, 1);
				}	// if
				//daneZr.get(i).setElementAt(1, 1);
			}	// for
			intensywnoscZrodel = sumaIntensywnosciZrodel();
	
			
			double skalar = intensywnoscOdbiorcow /  intensywnoscZrodel ; 
			
			for (int i=0; i<daneZr.size(); i++){ 
				int val = (Integer) tabelaZr.getValueAt(i, 1);
				//daneZr.get(i).setElementAt(val*skalar, 1);
				tabelaZr.setValueAt((Integer) val*skalar, i, 1);
			}	// for
			
			repaint();
		}	// normalizujIntensywnoscZrodel()
		
		/** normalizuje intensywnosc odbiorcow tak by pasowaly do sumarycznej
		 *  intensywnosci zrodel
		 */
		private void normalizujIntensywnoscOdbiorcow(){
			if (1+1==2){
				return;
			}
			
			@SuppressWarnings("unused")
			
			int intensywnoscOdbiorcow = sumaIntensywnosciOdbiorcow();
			int intensywnoscZrodel = sumaIntensywnosciZrodel();
			if (intensywnoscZrodel == 0){
				JOptionPane.showMessageDialog(this, "Sumaryczne intensywno\u015bci nie mog\u0105 by\u0107 zerowe",
						"B\u0142\u0105d", JOptionPane.ERROR_MESSAGE);
				return;
			}	// fi
			
			// testujemy czy sa zera w tablicy
			if (intensywnoscOdbiorcow == 0 )
			for (int i=0; i<mapa.getIlOdbiorcow() ; i++){
					tabelaOdb.setValueAt(1.0, i, 1);
			}	// for
			intensywnoscOdbiorcow = sumaIntensywnosciOdbiorcow();
	
			
			double skalar =  intensywnoscZrodel / intensywnoscOdbiorcow; 
			
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){ 
				double val = (Integer) tabelaOdb.getValueAt(i, 1);
				//daneZr.get(i).setElementAt(val*skalar, 1);
				tabelaOdb.setValueAt(new Integer((int) Math.round(val*skalar)), i, 1);
			}	// for
			repaint();
			
		}	// normalizujIntensywnoscOdbiorcow()
		
		/** Normalizuje polaczenia zrodel i odbiorcow by sumowaly sie do 1 
		 * dla kazdego zrodla
		 * (suma po odbiorcach)
		 */
		private void normalizujPolaczenia(){
			int s = 0;
			for (int i=0; i< mapa.getIlZrodel(); i++){
				s = 0;
				for (int j=1; j<= mapa.getIlOdbiorcow(); j++)
					s += (Integer) dane.get(i).get(j);
				if (s!=0)
					for (int j=1; j<=mapa.getIlOdbiorcow(); j++)
						/*
						 *  TODO do przemyslenia
						 */
						dane.get(i).set(j,  (Integer) dane.get(i).get(j)  );
				else
					dane.get(i).set(r.nextInt(mapa.getIlOdbiorcow()), new Integer(1));
			}	// for i
		}	// normalizujMacierzTransportu
		
		/** przepisuje konfiguracje polaczen z edytora (dialog) do macierzy transporu 
		 * przepisuje intensywnosc transportu z edytora do vektora w klasie 
		 * wykonuje normalizacje
		 */
		private void zapiszPolaczenia(){
			normalizujPolaczenia();
			for (int i=0; i< mapa.getIlZrodel(); i++)
				for (int j=0; j<mapa.getIlOdbiorcow(); j++)
					mapa.setTransport(i, j, (Integer)dane.get(i).get(j+1));
			
			for (int i=0; i< mapa.getIlZrodel(); i++){
				mapa.setIntensywnoscZrodla(i, ((Integer)( daneZr.get(i).get(1))));
				
			}	// for i		
			
			for (int i=0; i< mapa.getIlOdbiorcow(); i++){
				mapa.setIntensywnoscOdbiorcy(i, ((Integer)( daneOdb.get(i).get(1))));
				
			}	// for i
		}	// zapiszPolaczenia()

		
		/** TableModel do tablicy z danymi
		 */
		private class TableModelClass  extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, TableModel {
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
				if (c==0)
					return false;
				return true;
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
			
			private static final long serialVersionUID = 1L;
			JSlider slider = new JSlider();

    		public Component getTableCellEditorComponent(JTable table, 
    				Object value, boolean isSelected, int row, int column){
	        	slider.setValue((int)((Integer)value).intValue());
        		return slider;
    		}	//GetTableCellEditor

			public Object getCellEditorValue(){
	        		return new Integer(slider.getValue());
	    	}	//GetCellEditorValue
			
			JProgressBar bar = new JProgressBar(0, 100);
			
			{
			bar.setStringPainted(true);
			}	
			
			public Component getTableCellRendererComponent(JTable table, Object value,
	                            boolean isSelected, boolean hasFocus, int row, int column)					{
				int val = (Integer)value;
				bar.setValue(val);
				bar.setString(String.format("%3d", val));
				return bar;
	    	}	// getTableCellRendererComponent
		}	// TableModelClass
		
		/** TableModel do renderowania tablicy z intensywnoscia
		 */
		private class TableModelZr extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, TableModel {
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
				if (c==0)
					return false;
				return true;
			}
			public Object getValueAt(int r, int c) {
				return daneZr.get(r).get(c);
			}
			public void setValueAt(Object arg0, int r, int c) {
				daneZr.get(r).setElementAt(arg0, c);
				labSumaZrodel.setText(String.format("suma \u017ar\u00f3de\u0142= %3d", sumaIntensywnosciZrodel()));
			}
			public void addTableModelListener(TableModelListener arg0) {
			}
			public void removeTableModelListener(TableModelListener arg0) {
			}

			private static final long serialVersionUID = 1L;
			JSlider slider = new JSlider(1, 100);
			JProgressBar bar = new JProgressBar(0, 100);
			
			{
			bar.setStringPainted(true);
			}	

    		public Component getTableCellEditorComponent(JTable table, 
    				Object value, boolean isSelected, int row, int column){
    			
	        	slider.setValue((int)((Integer)value).intValue());
        		return slider;
    		}	//GetTableCellEditor

			public Object getCellEditorValue(){
	        		return new Integer(slider.getValue() );
	    	}	//GetCellEditorValue
			
			public Component getTableCellRendererComponent(JTable table, Object value,
	                            boolean isSelected, boolean hasFocus, int row, int column)					{
				int val = ((Integer)value).intValue();
				bar.setValue(val);
				bar.setString(String.format("%3d",val ));
				return bar;
	    	}	// getTableCellRendererComponent
		}	//   class CellRenderer
		
		private class TableModelOdb extends AbstractCellEditor implements TableModel, TableCellRenderer, TableCellEditor {
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
				if (c==0)
					return false;
				return true;
			}
			public Object getValueAt(int r, int c) {
				return daneOdb.get(r).get(c);
			}
			public void setValueAt(Object arg0, int r, int c) {
				daneOdb.get(r).setElementAt(arg0, c);
				labSumaOdbiorcow.setText(String.format("suma odbioru = %d", sumaIntensywnosciOdbiorcow()));
			}	// setValueAt
			public void addTableModelListener(TableModelListener arg0) {
			}
			public void removeTableModelListener(TableModelListener arg0) {
			}
			
			private static final long serialVersionUID = 1L;
			JSlider slider = new JSlider();
			JProgressBar bar = new JProgressBar(0, 100);
			
			{
			bar.setStringPainted(true);
			}	

    		public Component getTableCellEditorComponent(JTable table, 
    				Object value, boolean isSelected, int row, int column){
	        	slider.setValue((int)(((Integer)value).intValue()));
        		return slider;
    		}	//GetTableCellEditor

			public Object getCellEditorValue(){
	        		return new Integer(slider.getValue());
	    	}	//GetCellEditorValue
			
			public Component getTableCellRendererComponent(JTable table, Object value,
	                            boolean isSelected, boolean hasFocus, int row, int column)					{
				int val = ((Integer)value).intValue();
				bar.setValue(val);
				bar.setString(String.format("%3d",val ));
				return bar;
	    	}	// getTableCellRendererComponent
			
		}	// TableModelClass
		
		
	}	// class EdytorPolaczen
	
	
	private static class Obserwator extends Observable {
	    // The setChanged() protected method must overridden to make it public
	    public synchronized void setChanged() {
	        super.setChanged();
	    }	// setcanged
	}	// class obserwator


	
	
}	// class EdytorMapy