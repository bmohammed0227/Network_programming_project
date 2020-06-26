import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.ImageIcon;

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
	public boolean refreshFiles(String sender, String receiver, File file) throws RemoteException{
		return observer.refreshFiles(sender, receiver, file);
	}
}