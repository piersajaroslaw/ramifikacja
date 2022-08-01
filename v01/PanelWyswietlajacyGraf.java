package v01;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ConcurrentModificationException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** klasa podgladu grafu + reczne laczenie
 * 
 * 2007-??-??
 * 		podstawowy kod byl pierwotnie w Graf.java
 * 2007-12-03
 * 		przeniesiono do oddzielnego pliku
 * 
 * @author Jarosaw Piersa
 */
public class PanelWyswietlajacyGraf extends JPanel implements ActionListener, ChangeListener, MouseMotionListener{
	private Graf graf=null;
	
	private static final long serialVersionUID = 1L;
	private JButton buttMapa, buttPolacz, buttRozdziel = null;
	private JCheckBox chckMapa = null;
	private JCheckBox chckNumery = null;
	
	private boolean flagaNumer = false;
	private JSpinner spin1, spin2;
	private JLabel labAlpha;
	private JSlider slidAlpha = null;
	private JLabel labPozycja = null;
	
	public PanelWyswietlajacyGraf(Graf g){
		super();
		this.graf = g;
		setLayout(new BorderLayout());
		
		JPanel panE1 = new JPanel(new GridLayout(7,1,0,0));
		
		panE1.add(new JLabel(String.format("KPocz = %.2f", g.getKosztPoczatkowy())));
		
		chckMapa = new JCheckBox(new ImageIcon("./v01/ikony/odl2.gif"), false);
		chckMapa.setSelectedIcon(new ImageIcon("./v01/ikony/odl1.gif"));
		chckMapa.addChangeListener(this);
		panE1.add(chckMapa);
		chckNumery = new JCheckBox(new ImageIcon("./v01/ikony/nr0.gif"), false);
		chckNumery.setSelectedIcon(new ImageIcon("./v01/ikony/nr1.gif"));
		chckNumery.addChangeListener(this);
		panE1.add(chckNumery);
	
		
		labAlpha = new JLabel();
		labAlpha.setText(String.format("alpha = %1.2f", g.getAlpha()));
		panE1.add(labAlpha);
	
		slidAlpha = new JSlider(0, 100, (int)(g.getAlpha()*100));
		slidAlpha.setToolTipText("Ustaw parametr alpha");
		slidAlpha.setPreferredSize(new Dimension(80, 10));
		slidAlpha.addChangeListener(this);
		panE1.add(slidAlpha);
		
		labPozycja = new JLabel(String.format("%3d, %3d", 0, 0));
		panE1.add(labPozycja);
		
		JPanel panE = new JPanel();
		panE.add(panE1);
		
		JPanel panS = new JPanel();
		
		buttPolacz = new JButton("Polacz");
		buttPolacz.addActionListener(this);
		panS.add(buttPolacz);
		
		buttRozdziel = new JButton("Rozdziel");
		buttRozdziel.addActionListener(this);
		panS.add(buttRozdziel);
		
		spin1 = new JSpinner();
		spin1.setPreferredSize(new Dimension(35, 20));
		spin2 = new JSpinner();
		spin2.setPreferredSize(new Dimension(35, 20));
		panS.add(spin1);
		panS.add(spin2);

		
		add(panE, BorderLayout.EAST);
		add(panS, BorderLayout.SOUTH);
		addMouseMotionListener(this);
		setPreferredSize(new Dimension((int)Punkt.MAX_X+120, (int)Punkt.MAX_Y +50));
		repaint();
	}	// konstruktor
	
	public void paint(Graphics arg0) {
		super.paint(arg0);
		Graphics g = arg0.create();
		
		g.setColor(Color.white);
		g.fillRect(0,0,(int) Punkt.MAX_X, (int) Punkt.MAX_Y);
		
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
			}	// for j
		}	// for i
		
		try{
			for (Punkt p : graf.getWierzcholki()){
				g.setColor(Punkt.KOLOR_PUNKTU[p.getTyp()]);
				g.fillOval((int)p.getX()-3, (int)p.getY()-3, 7, 7);
				g.setColor(Color.black);
				if (flagaNumer==true)
					g.drawString(""+graf.indeks(p), (int)p.getX()-3, (int)p.getY()+3);
			}	// for p
		} catch (ConcurrentModificationException e) {};
		
		g.drawString(String.format("koszt = %.2f", graf.getKoszt() ),10, 10);
	}	// paint

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttMapa){
			graf.obliczMapeOdleglosci();
			repaint();
		}	// fi
		if (arg0.getSource() == buttPolacz){
			int i1 = (Integer) spin1.getValue();
			int i2 = (Integer) spin2.getValue();
			graf.polaczWierzcholki( graf.getWierzcholek(i1), graf.getWierzcholek(i2));
			repaint();
			return;
		}	// if
		if (arg0.getSource() == buttRozdziel){
			int i1 = (Integer) spin1.getValue();
			graf.rozdzielWierzcholek( graf.getWierzcholek(i1), 0);
			repaint();
			return;
		}	// if
			
	}	// actionPerformed()

	
	/** oznacza przeplyw kolorem zaleznym od intensywnosci
	 * @param przeplyw >=0
	 * @return kolor okreslajacy intensywnosc przeplywu
	 */
	public static Color kolorPrzeplywu(double przeplyw){
		if (przeplyw <0)
			return Color.white;
		
		int val =(int) ((przeplyw*2 - Math.floor(przeplyw*2))* 255);
		//System.out.println(val);
		
		// white -> blue
		if (przeplyw < 0.5)
			return	new Color(255- val, 255-val, 255);
		// blue -> teal
		if (przeplyw < 1.0)
			return	new Color(0, val, 255);
		// teal -> green
		if (przeplyw < 1.5)
			return	new Color(0, 255, 255-val);
		// green -> yellow
		if (przeplyw < 2.0)
			return	new Color(val, 255, 0);
		// yellow -> red
		if (przeplyw < 2.5)
			return	new Color(255, 255 - val, 0);
		// red -> violet
		if (przeplyw < 3.0)
			return	new Color(255, 0, val);
		return Color.decode("#ff00ff");
	}	// kolorPrzeplywu()
	
	
	
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
		if (arg0.getSource() == slidAlpha ){
			double val = slidAlpha.getValue() / 100.0;
			Graf.setAlpha(val);
			labAlpha.setText(String.format("alpha = %1.2f", val));
			
			repaint();
		}	// if
	}	// stateChanged()

	public void mouseDragged(MouseEvent arg0) {
	}	// mouseDragged()

	public void mouseMoved(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		
		if (x >= Punkt.MAX_X){
			x= (int) Punkt.MAX_X-1;
		}	// if
		
		labPozycja.setText(String.format("%3d, %3d", x, y));
		
	}	// MouseMoved();
}	// panelGraficzny