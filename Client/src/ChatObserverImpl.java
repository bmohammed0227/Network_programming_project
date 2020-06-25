import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
    
}
