import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class SentList_Imp extends UnicastRemoteObject implements SentList{
	private ArrayList<Compte> comptes = new ArrayList<>();
	protected SentList_Imp() throws RemoteException {
		super();
		// chargement des comptes
		int i = 1;
		String pseudo = null;
		String password = null;
		String email = null;
		try (BufferedReader br = new BufferedReader(new FileReader(new File("comptes.txt"))); 
				) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if(i == 1) pseudo = line;
		       else if(i == 2) password = line;
		       else if(i == 3) {
		    	   email = line;
		    	   i = 0;
		    	   Compte c = new Compte(pseudo,password,email,false);
		    	   comptes.add(c);
		       }
		       i++;
		    }
		}catch(IOException e) {e.printStackTrace();}
		
		// on définit un timer pour actualiser les infos a chaque 2 minutes
		TimerTask repeatedTask = new TimerTask() {
	        public void run() {
	        	System.out.println("Affichage des clients connecté : ");
	        	System.out.println("-----------");
	        	for(int i=0; i<comptes.size(); i++) {
	        		Compte c = comptes.get(i);
	        		if (c.isStatus() && ((new Date().getTime()-c.getDate().getTime())/1000>1)) c.setStatus(false);
	        		if(c.isStatus()) System.out.println(c.getPseudo());
	        	}
	        	System.out.println("-----------");
	        }
	    };
	    Timer timer = new Timer("Timer");
	    timer.scheduleAtFixedRate(repeatedTask, 0, 1000);
	
	}
	
	@Override
	public int sent(ArrayList list) throws RemoteException {
		String pseudo = (String)list.get(1);
		if((int)list.get(0) == 0) { // Demande d'inscription
			String email = (String) list.get(3);
			if(searchByPseudo(pseudo) != null) return 1;
			else if(searchByEmail(email) != null) return 2;
			else {
				// Ajout du nouvel utilisteur a la liste de comptes
				Compte c= new Compte(pseudo, (String)list.get(2), email, true);
				comptes.add(c);
				// Ecriture du nouvel utilisateur sur le fichier comptes.txt
				try(BufferedWriter br = new BufferedWriter(new FileWriter(new File("comptes.txt"),true))){
					br.write(pseudo+"\n");
					br.write((String)list.get(2)+"\n");
					br.write(email+"\n");
					System.out.println("Ecriture effectué");
				}catch(IOException e) {e.printStackTrace();}
			}
		}else if((int)list.get(0) == 1){ // Demande de connexion
			String password = (String) list.get(2);
			Compte c = searchByPseudo(pseudo);
			if(c == null) return 3;
			else if(!c.getPassword().equals(password)) return 4;
			else {
				// mis a jour de l'état de l'utilisateur
				c.setDate();
			}
		} else if((int)list.get(0) == 2){ // Confirmation de présence
			Compte c = searchByPseudo(pseudo);
			c.setDate();
		}else { // Deconnexion
			Compte c = searchByPseudo(pseudo);
			c.setStatus(false);
		}
		return 0;
	}
	
	

	private Compte searchByEmail(String email) {
		Iterator<Compte> it = this.comptes.iterator();
		while(it.hasNext()) {
			Compte c = it.next();
			if(c.getEmail().equals(email)) return c;
		}
		return null;
	}

	private Compte searchByPseudo(String pseudo) {
		Iterator<Compte> it = this.comptes.iterator();
		while(it.hasNext()) {
			Compte c = it.next();
			if(c.getPseudo().equals(pseudo)) return c;
		}
		return null;
	}

	public ArrayList<Compte> getComptes(){
		return this.comptes;
	}
	
}
