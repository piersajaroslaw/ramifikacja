package v02;

public class ModywikacjaGrafuException extends Throwable {

	private static final long serialVersionUID = 1L;
	private String msg = "";
	
	public ModywikacjaGrafuException(String s){
		msg = s;
	}	// konstruktor
	
	public String toString(){
		return msg;
	}	// toString
	
	public static void wypiszStanWywolania(String wiadomosc){
		try {
			throw new ModywikacjaGrafuException( wiadomosc );
		} catch (ModywikacjaGrafuException e){
			e.printStackTrace();
		}	// try
	}	//  wypiszStanWywolania()

}	// class
