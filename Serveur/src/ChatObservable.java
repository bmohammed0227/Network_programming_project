import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.healthmarketscience.rmiio.RemoteInputStream;

public class ChatObservable {
	private final ArrayList<ChatObserver> chatObserverList = new ArrayList<>();
	private final ArrayList<Group> chatGroups = new ArrayList<>();
	
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
    		ArrayList<String> onlineUsersList2 = new ArrayList<>();
    		copy(onlineUsersList2, onlineUsersList);
    		for(Group g: chatGroups) { // on actualise les groupes uniquement pour les membres
    			if(g.getParticipants().contains(observer.getUsername())) {
    				onlineUsersList2.add("#"+g.getName());
    			}
    		}
    		observer.refreshOnlineUsers(onlineUsersList2);
    	}
    	return true;
    }

	private void copy(ArrayList<String> onlineUsersList2, ArrayList<String> onlineUsersList) {
		for(int i=0; i<onlineUsersList.size();i++) {
			onlineUsersList2.add(onlineUsersList.get(i));
		}
	}

	public boolean removeChatObserver(ChatObserver chatObserver) {
		return chatObserverList.remove(chatObserver);
	}

	public boolean sendFileTo(String sender, String receiver, String filename) throws RemoteException {
        for (ChatObserver observer : chatObserverList) {
        	String text = "["+filename+"]";
            observer.refreshMessages(sender, receiver, text);
        }
        return true;
	}

	public void add(Group group) throws RemoteException {
		chatGroups.add(group);
		this.updateOnlineUsers();
	}
	
	public Group getGroup(String name) {
		Group group = null;
		
		for(Group g:chatGroups) {
			if (g.getName().equals(name.substring(1))) {
				group = g;
			}
		}
		return group;
	}
}
