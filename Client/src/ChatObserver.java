import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatObserver extends Remote {
	public boolean refreshMessages(String sender, String receiver, String text) throws RemoteException;
}
