import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
public interface ChatService extends Remote {
	public int sent(ArrayList list) throws RemoteException;
	public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException;
	boolean addChatObserver(ChatObserver chatObserver) throws RemoteException;
}
