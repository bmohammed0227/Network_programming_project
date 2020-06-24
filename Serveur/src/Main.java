import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("Serveur");
		receive_choice();
	}

	private static void receive_choice() {
		SentList_Imp skeleton = null;
		try {
			LocateRegistry.createRegistry(1099);
			skeleton = new SentList_Imp();
			Naming.rebind("rmi://localHost:1099/list", skeleton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
