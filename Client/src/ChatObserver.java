import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public interface ChatObserver extends Remote {
	public boolean refreshMessages(String sender, String receiver, String text) throws RemoteException;
	public boolean refreshImages(String sender, String receiver, ImageIcon image) throws RemoteException;
	public boolean refreshOnlineUsers(ArrayList<String> onlineUsersList) throws RemoteException;
	public String getUsername() throws RemoteException;
}
