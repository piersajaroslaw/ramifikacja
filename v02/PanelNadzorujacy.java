package v02;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import v01.MapaDostaw;

/*
 * \u0105  a
 * \u0107  c
 * \u0119  e
 * \u0142  l
 * \u0144  n
 * \u00f3  o
 * \u015b  s
 * \u017c  z
 * \u017a  x
 * \u0104  A
 * \u0106  C
 * \u0118  E
 * \u0141  L
 * \u0143  N
 * \u00d3  O
 * \u015a  S
 * \u017b  Z
 * \u0179  X
 * 
 */

/** panel wyswietlajacy i zarzadzajacy calym algorytmem
 * @author Jaroslaw Piersa
 *
 * 2008-02-22
 * 		utworzenie, a raczej skopiowanie z okna
 * 		wyswietlenie numeru krolu i regulacja ilosci epok w algorytmie kohonena
 * 		regulacja porcji przeplywu
 * 2008-02-23
 * 		regulacja temperatury i sleeptimu
 * 2008-02-24
 * 		dodano wybor palety modyfikacji grafu
 * 2008-06-01
 * 		regulacja promienia
 * 		przyniesiono tu rowniez regulacje skalarow i parametru alpha
 * 		duzy panel boczny wrzucono jako  trzecia zakladka
 * 
 * 
 *
 */
public class PanelNadzorujacy extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private WejscieDoAlgorytmu gt = null;
	private MapaDostaw m = null;
	private JButton buttOk,  buttNast, buttStart;
	private JPanel pan1;
	private PanelZOpcjami panelOpcji = null;
	
	private boolean flagaPracy = false;
	private int sleeptime = 25;
	

	
	public PanelNadzorujacy(JFrame parent){
		super();
		m = new MapaDostaw(4, 4);
		gt = new WejscieDoAlgorytmu(m);
		
		// panel S
		JPanel panS = new JPanel();
		buttOk = new JButton("Ok");
		buttOk.addActionListener(this);
		panS.add(buttOk);
		
		buttStart = new JButton("Start/Stop");
		buttStart.addActionListener(this);
		panS.add(buttStart);
		
		buttNast = new JButton(">>");
		buttNast.addActionListener(this);
		panS.add(buttNast);
		
		
		// panel C
		JTabbedPane tabbPaneC = new JTabbedPane();
		
		pan1 = new JPanel();
		pan1.add(gt.getGraf().getPodgladGrafu());
		
		JPanel pan3 = new JPanel();
		pan3.add(m.getEdytorMapy(parent));
		
		panelOpcji = new PanelZOpcjami();
		
		tabbPaneC.add("Graf", pan1);
		tabbPaneC.add("Edytor", pan3);
		tabbPaneC.add("Opcje", panelOpcji);
		tabbPaneC.add("O programie", new PanelOProgramie());
		
		
		// ladowanie
		setLayout(new BorderLayout());
		add(tabbPaneC, BorderLayout.CENTER);
		add(panS, BorderLayout.SOUTH);
	}	// konstruktor
	
	private void odswierzOpisy(){
		panelOpcji.odswierzOpisy();
		repaint();
	}	// odswierzOpisy
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttOk){
			gt.setMapa(m);
			pan1.removeAll();
			pan1.add(gt.getGraf().getPodgladGrafu());
			panelOpcji.getSliderTemperatura().setValue(1);
			gt.setTemperatura(panelOpcji.getSliderTemperatura().getValue()*.01);
			odswierzOpisy();
			return;
		}	// if
		
		if (arg0.getSource() == buttNast){
			gt.nastepnyKrok();
			odswierzOpisy();
			return;
		}	// if
		
		if (arg0.getSource() == buttStart){
			flagaPracy = !flagaPracy;
			if (flagaPracy == true)
				wykonujIteracje();
			return;
		}	// if
	}	// actionPerformed()

	
	private void wykonujIteracje(){
		new WatekLiczacy().start();
	}	// fi
	
	private class WatekLiczacy extends Thread{
		public void run(){
			while (flagaPracy){
				gt.nastepnyKrok();
				odswierzOpisy();
				try {Thread.sleep(sleeptime);}
				catch (InterruptedException e) {}
			}	// while
		}	// run
	}	// class Watek

	
	private class PanelZOpcjami extends JPanel implements ActionListener, ChangeListener{
		private static final long serialVersionUID = 1L;
		
		private JLabel labKrok = null;
		private JLabel labTemperatura = null;
		private JLabel labPromien = null;
		private JLabel labAlpha = null;
		private JLabel labSkalar1 = null, labSkalar2 = null;
		private JSlider slidTemperatura = null;
		private JSlider slidPromien = null;
		private JSlider slidAlpha = null;
		private JSlider slidSkalar1 = null, slidSkalar2 = null;
		private JComboBox comboMaxT = null;
		private JComboBox<Integer> comboD = null;
		private JComboBox comboSleeptime = null;
		private JCheckBox chkboxLaczenie = null;
		private JCheckBox chkboxPrzesunieciaLosowe = null;
		private JCheckBox chkboxPrzesunieciaKohonena = null;
		private JCheckBox chkboxModyfikacjaPrzeplywu = null;
		private JCheckBox chkboxModyfikacjaRozgalezien = null;
		
		public PanelZOpcjami(){
			// panel C
			JPanel panC = new JPanel(new GridLayout(12,2));
			
			// aktualny krok
			panC.add(new  JLabel("Krok"));
			labKrok = new JLabel(""+gt.getT());
			panC.add(labKrok);
			
			// ilosc krokow w alg Kohonena
			panC.add(new  JLabel("Max Krok"));
			Object obj1[] = {1000, 5000, 10000, 50000, 100000};
			comboMaxT = new JComboBox(obj1);
			comboMaxT.addActionListener(this);
			panC.add(comboMaxT);
			
			
			// zmiana przeplywu
			panC.add(new  JLabel("Zmiana przepływu"));
			Integer obj2[] = { 1, 2, 3, 4, 5, 6};
			comboD = new JComboBox<Integer>(obj2);
			comboD.addActionListener(this);
			panC.add(comboD);
			
			// sleeptime
			panC.add(new  JLabel("Czas odświeżania"));
			Object obj3[] = { 25, 50, 75, 100, 150, 250, 500, 1000};
			comboSleeptime = new JComboBox(obj3);
			comboSleeptime.addActionListener(this);
			panC.add(comboSleeptime);
			
			
			// temperatura
			labTemperatura = new JLabel(String.format("temperatura = %.2f", gt.getTemperatura()));
			slidTemperatura = new JSlider(1,2000, 1);
			slidTemperatura.addChangeListener(this);
			panC.add(labTemperatura);
			panC.add(slidTemperatura);
			
			
			// promien
			labPromien = new JLabel(String.format("Promień = %3d", (int) gt.getPromien()));
			panC.add(labPromien);
			slidPromien = new JSlider(10, 100, 50);
			slidPromien.addChangeListener(this);
			panC.add(slidPromien);
			
			// alpha
			labAlpha = new JLabel();
			labAlpha.setText(String.format("alpha = %1.2f", gt.getGraf().getAlpha()));
			panC.add(labAlpha);
		
			slidAlpha = new JSlider(0, 100, (int)(gt.getGraf().getAlpha()*100));
			slidAlpha.setToolTipText("Ustaw parametr alpha");
			slidAlpha.setPreferredSize(new Dimension(80, 10));
			slidAlpha.addChangeListener(this);
			panC.add(slidAlpha);
			
			// skalary
			labSkalar1 = new JLabel();
			labSkalar1.setText(String.format("skalar1 = %1.1f", GrafWrazZPrzeplywem.getSkalarKoszt1()));
			panC.add(labSkalar1);
		
			slidSkalar1 = new JSlider(0, 1000, (int)(GrafWrazZPrzeplywem.getSkalarKoszt1()*10));
			slidSkalar1.setToolTipText("Ustaw skalar kosztu 1");
			slidSkalar1.setPreferredSize(new Dimension(80, 10));
			slidSkalar1.addChangeListener(this);
			panC.add(slidSkalar1);
			
			
			labSkalar2 = new JLabel();
			labSkalar2.setText(String.format("skalar2 = %1.1f", GrafWrazZPrzeplywem.getSkalarKoszt2()));
			panC.add(labSkalar2);
			
			
			slidSkalar2 = new JSlider(0, 1000, (int)(GrafWrazZPrzeplywem.getSkalarKoszt2()*10));
			slidSkalar2.setToolTipText("Ustaw skalar kosztu 2");
			slidSkalar2.setPreferredSize(new Dimension(80, 10));
			slidSkalar2.addChangeListener(this);
			panC.add(slidSkalar2);
			
			// chckbox kohonen
			chkboxPrzesunieciaKohonena = new JCheckBox("Przesunięcia Kohonena", true);
			chkboxPrzesunieciaKohonena.addActionListener(this);
			panC.add(chkboxPrzesunieciaKohonena);
			
			// chckbox losowe
			chkboxPrzesunieciaLosowe = new JCheckBox("Przesunięcia losowe", true);
			chkboxPrzesunieciaLosowe.addActionListener(this);
			panC.add(chkboxPrzesunieciaLosowe);
			
			// chckbox laczenia
			chkboxLaczenie = new JCheckBox("Laczenia i rozlaczenia", true);
			chkboxLaczenie.addActionListener(this);
			panC.add(chkboxLaczenie);
			
			// chckbox Przeplyw
			chkboxModyfikacjaPrzeplywu = new JCheckBox("Zmiany przeplywu", true);
			chkboxModyfikacjaPrzeplywu.addActionListener(this);
			panC.add(chkboxModyfikacjaPrzeplywu);
			// chckbox Przeplyw
			chkboxModyfikacjaRozgalezien = new JCheckBox("Zmiany rozgalezien", true);
			chkboxModyfikacjaRozgalezien.addActionListener(this);
			panC.add(chkboxModyfikacjaRozgalezien);
			
			
			JPanel panC1 = new JPanel();
			panC1.add(panC);
			
			add(panC1);
		}	// konstuktor()

		JSlider getSliderTemperatura(){
			return slidTemperatura;
		}	// 
		
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() ==  comboMaxT){
				Integer i = (Integer) comboMaxT.getSelectedItem();
				gt.setMaxT(i.intValue());
				return;
			}	// if
			
			if (arg0.getSource() ==  comboD){
				Integer d = (Integer) comboD.getSelectedItem();
				GrafWrazZPrzeplywem.setD(d.intValue());
				return;
			}	// if
			
			if (arg0.getSource() ==  comboSleeptime){
				Integer i = (Integer) comboSleeptime.getSelectedItem();
				sleeptime = i;
				return;
			}	// if
			
			if (arg0.getSource() == chkboxPrzesunieciaKohonena){
				gt.setCzyDopuszczonePrzesuwanieKohonena(chkboxPrzesunieciaKohonena.isSelected());
				return;
			}	// if
			
			if (arg0.getSource() == chkboxPrzesunieciaLosowe){
				gt.setCzyDopuszczonePrzesuwanieLosowe(chkboxPrzesunieciaLosowe.isSelected());
				return;
			}	// if
			
			if (arg0.getSource() == chkboxLaczenie){
				gt.setCzyDopuszczoneLaczenie(chkboxLaczenie.isSelected());
				return;
			}	// if
			
			if (arg0.getSource() == chkboxModyfikacjaPrzeplywu){
				gt.setCzyDopuszczoneModyfikowaniePrzeplywu(chkboxModyfikacjaPrzeplywu.isSelected());
				return;
			}	// if
			if (arg0.getSource() == chkboxModyfikacjaRozgalezien){
				gt.setCzyDopuszczoneModyfikowanieRozgalezien(chkboxModyfikacjaRozgalezien.isSelected());
				return;
			}	// if
			
		}	// actionPerformed()

		public void stateChanged(ChangeEvent arg0) {
			if (arg0.getSource() == slidTemperatura){
				int val=slidTemperatura.getValue();
				gt.setTemperatura(val * .01);
				labTemperatura.setText(String.format("temperatura = %.2f", gt.getTemperatura()));
			}	// if
			
			if (arg0.getSource().equals(slidPromien)){
				int val = slidPromien.getValue();
				labPromien.setText(String.format("Promień = %3d", val));
				gt.setPromien(val);
			}	// if
			
			if (arg0.getSource() == slidAlpha ){
				double val = slidAlpha.getValue() / 100.0;
				GrafWrazZPrzeplywem.setAlpha(val);
				labAlpha.setText(String.format("alpha = %1.2f", val));
				
				repaint();
			}	// if
			
			if (arg0.getSource() == slidSkalar1 ){
				double val = slidSkalar1.getValue() / 10.0;
				GrafWrazZPrzeplywem.setSkalarKoszt1(val);
				labSkalar1.setText(String.format("skalar1 = %1.1f", val));
				
				repaint();
			}	// if
			
			if (arg0.getSource() == slidSkalar2 ){
				double val = slidSkalar2.getValue() / 10.0;
				GrafWrazZPrzeplywem.setSkalarKoszt2(val);
				labSkalar2.setText(String.format("skalar2 = %1.1f", val));
				
				repaint();
			}	// if
		}	// stateChanged
		
		
		private void odswierzOpisy(){
			labKrok.setText(""+gt.getT());
			labTemperatura.setText(String.format("temperatura = %.2f", gt.getTemperatura()));
			labPromien.setText(String.format("Promień = %3d", slidPromien.getValue()));
			repaint();
		}	// odswierz opisy
		
	}	// class
	
}	// class okno
