import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class SentList_Imp extends UnicastRemoteObject implements SentList {

    private Connection connection;
    private ArrayList<Compte> comptes = new ArrayList<>();

    protected SentList_Imp() throws RemoteException {
        super();
        connection = getDatabaseConnection();
        if (connection == null) {
            System.out.println("Can't connect to the database");
        }
        else {
            System.out.println("Connected to the database");
            boolean received = getAllUsers();
            if (received) {
                System.out.println("SELECT query finished successfully");
            }
            for (Compte compte : comptes) {
                System.out.println(compte.toString());
            }
        }

        // on definit un timer pour actualiser les infos a chaque 2 minutes
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                System.out.println("Affichage des clients connectee : ");
                System.out.println("-----------");
                for (int i = 0; i < comptes.size(); i++) {
                    Compte c = comptes.get(i);
                    if (c.isStatus() && ((new Date().getTime() - c.getDate().getTime()) / 1000 > 1))
                        c.setStatus(false);
                    if (c.isStatus())
                        System.out.println(c.getPseudo());
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
            if (searchByPseudo(pseudo) != null)
                return 1;
            else if (searchByEmail(email) != null)
                return 2;
            else {
                // Ajout du nouvel utilisteur a la liste de comptes
                Compte c= new Compte(pseudo, (String)list.get(2), email, true);
                registerNewUser(c);
                comptes.add(c);
                // Ecriture du nouvel utilisateur sur le fichier comptes.txt
                try (BufferedWriter br = new BufferedWriter(new FileWriter(new File("comptes.txt"), true))) {
                    br.write(pseudo + "\n");
                    br.write((String) list.get(2) + "\n");
                    br.write(email + "\n");
                    System.out.println("Ecriture effectuee");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if ((int) list.get(0) == 1) { // Demande de connexion
            String password = (String) list.get(2);
            Compte c = searchByPseudo(pseudo);
            if (c == null)
                return 3;
            else if (!c.getPassword().equals(password))
                return 4;
            else {
                // mis a jour de l'etat de l'utilisateur
                c.setDate();
            }
        } else if ((int) list.get(0) == 2) { // Confirmation de presence
            Compte c = searchByPseudo(pseudo);
            c.setDate();
        } else { // Deconnexion
            Compte c = searchByPseudo(pseudo);
            c.setStatus(false);
        }
        return 0;
    }

    private Compte searchByEmail(String email) {
        Iterator<Compte> it = this.comptes.iterator();
        while (it.hasNext()) {
            Compte c = it.next();
            if (c.getEmail().equals(email))
                return c;
        }
        return null;
    }

    private Compte searchByPseudo(String pseudo) {
        Iterator<Compte> it = this.comptes.iterator();
        while (it.hasNext()) {
            Compte c = it.next();
            if (c.getPseudo().equals(pseudo))
                return c;
        }
        return null;
    }

    public ArrayList<Compte> getComptes() {
        return this.comptes;
    }

    private Connection getDatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver loaded");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:database.db");
            System.out.println("Connected Successfully");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean registerNewUser(Compte compte) {
        try {
            Statement statement = connection.createStatement();
            String query = "INSERT INTO USERS " + "(username, email, firstname, familyname, password) "
                + "VALUES('"+compte.getPseudo()+"', '" + compte.getEmail() + "', '" + compte.getFirstname() +"', '" + compte.getFamilyname() +"', '" + compte.getPassword() +"')";
            statement.executeUpdate(query);
            System.out.println("User registered successfully");
            return true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private boolean getAllUsers() {
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USERS");
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                comptes.add(new Compte(resultSet.getString("username"), resultSet.getString("email"), resultSet.getString("firstname"), resultSet.getString("familyname"), resultSet.getString("password"), false));
            }
            System.out.println("User registered successfully");
            return true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}
