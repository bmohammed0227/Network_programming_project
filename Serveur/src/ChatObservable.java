import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class ChatObservable {
	private final ArrayList<ChatObserver> chatObserverList = new ArrayList<>();
	private final ArrayList<String> chatUsers = new ArrayList<>();

    public boolean sendTextTo(String sender, String receiver, String text) throws RemoteException {
        for (ChatObserver observer : chatObserverList) {
            observer.refreshMessages(sender, receiver, text);
        }
        return true;
    }

    public boolean sendImageTo(String sender, String receiver, ImageIcon image) throws RemoteException {
        for (ChatObserver observer : chatObserverList) {
            observer.refreshImages(sender, receiver, image);
        }
        return true;
    }
    
    public boolean addChatObserver(ChatObserver chatObserver) throws RemoteException {	
    	return chatObserverList.add(chatObserver);
    }
    
    public boolean updateOnlineUsers() throws RemoteException {
    	ArrayList<String> onlineUsersList = new ArrayList<>();
    	for(ChatObserver observer : chatObserverList) {
    		onlineUsersList.add(observer.getUsername());
    	}
    	for(ChatObserver observer : chatObserverList) {
    		observer.refreshOnlineUsers(onlineUsersList);
    	}
    	return true;
    }

	public boolean removeChatObserver(ChatObserver chatObserver) {
		return chatObserverList.remove(chatObserver);
	}
}
