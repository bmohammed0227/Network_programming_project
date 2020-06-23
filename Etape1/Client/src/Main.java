import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

	public static void main(String[] args) throws RemoteException {
		System.out.println("Client");
		//Client ouvre l'application
		System.out.println("Ouverture de l'application");
		System.out.println("Veuillez entrer vos choix : [0: inscription, !0:connexion]");
		Scanner scanner = new Scanner(System.in);
		int choice = Integer.valueOf(scanner.nextLine());
		String pseudo = null;
		String password = null;
		ArrayList list = new ArrayList();
		if(choice == 0) {
			//Demande d'inscription
			System.out.println("[Menu d'inscription]");
			System.out.println("Choisissez un pseudo :");
			pseudo = scanner.nextLine();
			System.out.println("Choisissez un mot de passe :");
			password = scanner.nextLine();
			System.out.println("Entrez votre addresse Email :");
			String email = scanner.nextLine();
			list.add(0);
			list.add(pseudo);
			list.add(password);
			list.add(email);
		}
		else {
			//Demande d'inscription
			System.out.println("[Menu d'inscription]");
			System.out.println("Entrez votre pseudo :");
			pseudo = scanner.nextLine();
			System.out.println("Entrez votre mot de passe :");
			password = scanner.nextLine();
			list.add(1);
			list.add(pseudo);
			list.add(password);
		}
		int response = send(list);
		switch(response) {
		//Successful connection
		case 0:{
			if((int)list.get(0)==0) System.out.println("Votre compte a ete cree ! vous etes maintenant connectee");
			System.out.println("<== Affichage de la messagerie ==>");
			connected(pseudo);
		}
		//failed inscription
		break;
		case 1: System.out.println("Le pseudo que vous avez entrer existe deje");
		break;
		case 2: System.out.println("L'email que vous avez entrer existe deja");
		break;
		//failed connection
		case 3: System.out.println("Pseudo erronee");
		break;
		case 4: System.out.println("Mot de passe erronee");
		break;
		}
	}

	private static void connected(String pseudo) throws RemoteException {
		// on definit un timer pour que le serveur sache que le client est toujours connectee
		TimerTask repeatedTask = new TimerTask() {
	        public void run() {
	        	ArrayList list = new ArrayList();
	        	list.add(2);
	        	list.add(pseudo);
	        	try {
					send(list);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
	        }
	    };
	    
	    Timer timer = new Timer("Timer"); 
	    timer.scheduleAtFixedRate(repeatedTask, 0, 1000);
	    System.out.println("[Menu Envoie de messages et fichies :]");
	    Scanner scanner = new Scanner(System.in);
	    while(true) {
	    	System.out.println("voulez-vous vous deconnecter ?");
	    	int deconnection = scanner.nextInt();
	    	if(deconnection==0) {
	    		ArrayList list = new ArrayList();
	    		list.add(3);
	    		list.add(pseudo);
	    		System.exit(0);
	    	}
	    }
	}
	

	private static int send(ArrayList list) throws RemoteException {
    String SERVER_IP = "localhost";
    // String SERVER_IP = "172.23.139.139";
		SentList stub = null;
		try {
			stub =  (SentList)Naming.lookup("rmi://"+SERVER_IP+"/list");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stub.sent(list);
	}

}
