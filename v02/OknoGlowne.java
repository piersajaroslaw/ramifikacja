package v02;

import javax.swing.JFrame;

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
/**
 * 
 * @author piersaj
 *
 */
public class OknoGlowne extends JFrame {

	private static final long serialVersionUID = 1L;

	
	public OknoGlowne(){
		super("Optymalizacja Transportu");
//		WejscieDoAlgorytmu w = new WejscieDoAlgorytmu(new MapaDostaw(4,4));
		
		getContentPane().add(new PanelNadzorujacy(this));
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}	// konstruktor
	
	
	public static void main(String[] args) {
		new OknoGlowne();
	}	// main	

}	// class
