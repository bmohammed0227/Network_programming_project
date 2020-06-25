import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
public interface ChatService extends Remote {
	public int sent(ArrayList list) throws RemoteException;
	public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException;
	public boolean sendImageTo(String sender, String receiver, ImageIcon image) throws RemoteException;
	boolean addChatObserver(ChatObserver chatObserver) throws RemoteException;
}
