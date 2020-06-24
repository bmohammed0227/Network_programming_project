import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
public interface SentList extends Remote {
	public int sent(ArrayList list) throws RemoteException;

    // //  Add these when you start using button in the GUI
    // public boolean connect(String pseudo, String password) throws RemoteException;

    // public int register(Compte compte) throws RemoteException;

    // public void checkStatus(Compte compte, boolean isActive) throws RemoteException;
}