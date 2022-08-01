package v01;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * 2007-06-08
 * 		utworzenie
 * inne zmiany "przy okazji" wraz z postepem prac
 * 
 * @author Jaroslaw Piersa
 *
 */
// TODO ustawianie parametru maxT, podglad T (ilosc iteracji) podglad zmian kosztu sleeptime

public class TestOkno extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private GrafTransportu gt = null;
	private MapaDostaw m = null;
	private JButton buttOk,  buttNast, buttStart;
	private JPanel pan1;
	
	private boolean flagaPracy = false;
	private int sleeptime = 100;
	
	public TestOkno(){
		super("Optymalizacja Sieci");
		m = new MapaDostaw(3, 3);
		gt = new GrafTransportu(m);
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panelG = new JPanel(new BorderLayout());
		
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
		JTabbedPane tabbPane = new JTabbedPane();
		
		pan1 = new JPanel();
		pan1.add(gt.getGraf().getPodgladGrafu());
		
		JPanel pan3 = new JPanel();
		pan3.add(m.getEdytorMapy(this));
		
		tabbPane.add("Graf", pan1);
		tabbPane.add("Edytor", pan3);
		
		
		panelG.add(tabbPane, BorderLayout.CENTER);
		panelG.add(panS, BorderLayout.SOUTH);
		
		getContentPane().add(panelG);
		
		pack();
		setVisible(true);
		
	}	// konstruktor
	
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttOk){
			gt.setMapa(m);
			pan1.removeAll();
			pan1.add(gt.getGraf().getPodgladGrafu());
			repaint();
			return;
		}	// if
		if (arg0.getSource() == buttNast){
			gt.nastepnyKrok();
			repaint();
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
	

	public static void main(String[] args) {
		new TestOkno();
	}	// main

	private class WatekLiczacy extends Thread{
		public void run(){
			while (flagaPracy){
				gt.nastepnyKrok();
				repaint();
				try {Thread.sleep(sleeptime);}
				catch (InterruptedException e) {}
			}	// while
		}	// run
	}	// class Watek
	
	
}	// class okno
