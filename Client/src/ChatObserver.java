import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;

public interface ChatObserver extends Remote {
	public boolean refreshMessages(String sender, String receiver, String text) throws RemoteException;
	public boolean refreshImages(String sender, String receiver, ImageIcon image) throws RemoteException;
}
