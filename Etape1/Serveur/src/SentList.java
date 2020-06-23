import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
public interface SentList extends Remote {
	public int sent(ArrayList list) throws RemoteException;
}