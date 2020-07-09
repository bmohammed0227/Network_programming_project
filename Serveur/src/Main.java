import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Main {
	
	public static void main(String[] args) {
		System.out.println("Serveur");
		receive_choice();
	}

	private static void receive_choice() {
		ChatServiceImpl skeleton = null;
		try {
			LocateRegistry.createRegistry(1099);
			skeleton = new ChatServiceImpl();
			Naming.rebind("rmi://localHost:1099/list", skeleton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
