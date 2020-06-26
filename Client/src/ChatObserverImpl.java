import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.healthmarketscience.rmiio.RemoteInputStream;

public class ChatObserverImpl extends UnicastRemoteObject implements ChatObserver {

	private ChatObserver observer;
	
	public ChatObserverImpl(ChatObserver chatObserver) throws RemoteException {
		this.observer = chatObserver;
	}

	@Override
	public boolean refreshMessages(String sender, String receiver, String text) throws RemoteException {
		return observer.refreshMessages(sender, receiver, text);
	}
    
	@Override
	public boolean refreshImages(String sender, String receiver, ImageIcon image) throws RemoteException {
		return observer.refreshImages(sender, receiver, image);
	}

	@Override
	public boolean refreshOnlineUsers(ArrayList<String> onlineUsersList) throws RemoteException {
		return observer.refreshOnlineUsers(onlineUsersList);
	}
	
	@Override
	public String getUsername() throws RemoteException {
		return observer.getUsername();
	}

	@Override
	public boolean refreshVideos(String sender, String receiver, String filename, RemoteInputStream remoteFileData)
			throws RemoteException {
		return observer.refreshVideos(sender, receiver, filename, remoteFileData);
	}
}
