import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;
public interface ChatService extends Remote {
	public int sent(ArrayList list) throws RemoteException;
	public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException;
	public boolean sendFile(String sender, String receiver, String filename,RemoteInputStream inputFile) throws RemoteException;
	public boolean getFile(RemoteOutputStream remoteOutputStream, String filename) throws RemoteException;
	public boolean addChatObserver(ChatObserver chatObserver) throws RemoteException;
	public boolean removeChatObserver(ChatObserver chatObserver) throws RemoteException;
	public boolean updateOnlineUsers() throws RemoteException;
	public void addGroup(Group group) throws RemoteException;
	public void sendTextToGroup(String username, String receiver, String textMessage) throws RemoteException;
	boolean sendFileToGroup(String sender, String receiver, String filename, RemoteInputStream inputFile)
			throws RemoteException;
	public ArrayList<Group> getAllgroups() throws RemoteException; 
	public Compte getCompte(String user) throws RemoteException;
	public Group getGroup(String groupName) throws RemoteException;
}
