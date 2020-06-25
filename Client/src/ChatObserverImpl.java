import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.ImageIcon;

public class ChatObserverImpl extends UnicastRemoteObject implements ChatObserver {

	private ChatObserver observer;
	
	public ChatObserverImpl(ChatObserver chatObserver) throws RemoteException {
		this.observer = chatObserver;
	}

	@Override
	public boolean refreshMessages(String sender, String receiver, String text) throws RemoteException {
		System.out.println("ChatObserver : sending");
		return observer.refreshMessages(sender, receiver, text);
	}
    
	@Override
	public boolean refreshImages(String sender, String receiver, ImageIcon image) throws RemoteException {
		System.out.println("ChatObserver : sending");
		return observer.refreshImages(sender, receiver, image);
	}
}
