import java.rmi.RemoteException;
import java.util.ArrayList;

public class ChatObservable {
	private final ArrayList<ChatObserver> chatObserverList = new ArrayList<>();

    public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException {
    	System.out.println("ChatObservable : sending");
        for (ChatObserver observer : chatObserverList) {
            observer.refreshMessages(sender, receiver, text);
        }
        return true;
    }
    
    public boolean addChatObserver(ChatObserver chatObserver) throws RemoteException {	
    	return chatObserverList.add(chatObserver);
    }
}
