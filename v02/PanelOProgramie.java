package v02;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
/** Panel informacyjny
 * TODO napisac
 * 2008-06-01
 * 		Utworzenie
 */
public class PanelOProgramie extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	public static final String O_PROGRAMIE = "O programie";
	
	public PanelOProgramie(){
		add(new JLabel("Jaros\u0142aw Piersa"));
	}	// this()

	public void actionPerformed(ActionEvent arg0) {
		

	}	// actionPerformed

}	// class
