import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SentList_Imp extends UnicastRemoteObject implements SentList {

    private Connection connection;
    private ArrayList<Compte> comptes = new ArrayList<>();

    protected SentList_Imp() throws RemoteException {
        super();
        //	Connect to the database
        connection = DatabaseConnection.getDatabaseConnection();
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
                System.out.println("Affichage des clients connectees : ");
                System.out.println("-----------");
                for (Compte compte : comptes) {
                    if (compte.isStatus() && ((new Date().getTime() - compte.getDate().getTime()) / 1000 > 1))
                        compte.setStatus(false);
                    if (compte.isStatus())
                        System.out.println(compte.getPseudo());
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
            if (isPseudoReserved(pseudo))
                return 1;
            else if (isEmailReserved(email))
                return 2;
            else {
                // Ajout du nouvel utilisteur a la liste de comptes
                Compte compte = new Compte(pseudo, (String)list.get(2), email, true);
                addUserToDatabase(compte);
                comptes.add(compte);
            }
        } else if ((int) list.get(0) == 1) { // Demande de connexion
            String password = (String) list.get(2);
            Compte compte = searchByPseudo(pseudo);
            if (compte == null)
                return 3;
            else if (!compte.getPassword().equals(password))
                return 4;
            else {
                // mis a jour de l'etat de l'utilisateur
                compte.setDate();
            }
        } else if ((int) list.get(0) == 2) { // Confirmation de presence
            Compte compte = searchByPseudo(pseudo);
            compte.setDate();
        } else { // Deconnexion
            Compte compte = searchByPseudo(pseudo);
            compte.setStatus(false);
        }
        return 0;
    }

    // //  Add these when you start using button on GUI
    // @Override
    // public boolean connect(String pseudo, String password) {
    //     Compte compte = searchByPseudo(pseudo);
    //     if (compte != null)
    //         if (compte.getPassword().equals(password)) {
    //             return true;  // Accept connection and start chat window
    //         }
    //     return false;  // Refuse connection (username or password incorrect)
    // }

    // @Override
    // public int register(Compte compte) {
    //     if (isPseudoReserved(compte.getPseudo()))
    //         return -1;  // Refuse registration (Username already used)
    //     else if (isEmailReserved(compte.getEmail()))
    //         return -2;  // Refuse registration (Email already used)
    //     else
    //         if(addUserToDatabase(compte))
    //             return 0;  // User registered successfully
    //         else
    //             return -3;  // Error while trying to add the user to the database
    // }

    // @Override
    // public void checkStatus(Compte compte, boolean isActive) {
    //     Compte searchResult = searchByPseudo(compte.getPseudo());
    //     if (isActive)
    //         compte.setDate();
    //     else
    //         compte.setStatus(false);

    // }

    private Compte searchByEmail(String email) {
        for (Compte compte : comptes) {
            if (compte.getEmail().equals(email))
                return compte;
        }
        return null;
    }

    private boolean isEmailReserved(String email) {
        if (searchByEmail(email) == null)
            return false;
        else
            return true;
    }

    private boolean isPseudoReserved(String pseudo) {
        if (searchByPseudo(pseudo) == null)
            return false;
        else
            return true;
    }

    private Compte searchByPseudo(String pseudo) {
        for (Compte compte : comptes) {
            if (compte.getPseudo().equals(pseudo))
                return compte;
        }
        return null;
    }

    public ArrayList<Compte> getComptes() {
        return this.comptes;
    }

    // registers a user in the database and returns true if succeeded or false if not
    private boolean addUserToDatabase(Compte compte) {
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

    // executes SELECT * FROM USERS and add every user to the ArrayList comptes then sends true if succeeded or false if not
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
