import java.io.IOException;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.healthmarketscience.rmiio.RemoteInputStream;
public interface ChatService extends Remote {
	public int sent(ArrayList list) throws RemoteException;
	public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException;
	public boolean sendImageTo(String sender, String receiver, ImageIcon image) throws RemoteException;
	public boolean sendVideoTo(String sender, String receiver, String filename, RemoteInputStream remoteFileData) throws RemoteException, IOException;
	public boolean sendFileTo(String sender, String receiver, File file) throws RemoteException;
	public File getFile(String name) throws RemoteException;
	public boolean addChatObserver(ChatObserver chatObserver) throws RemoteException;
	public boolean removeChatObserver(ChatObserver chatObserver) throws RemoteException;
	public boolean updateOnlineUsers() throws RemoteException;
}
