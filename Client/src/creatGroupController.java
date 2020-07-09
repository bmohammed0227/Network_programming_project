import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class creatGroupController {
	@FXML
	private TableView<String> TV_OnlineUsers;
	
	@FXML
	private TableColumn<String, String> oneline_users_Column;
	
	@FXML
	private TextField nameGroup;

	@FXML
	private Button add;
	
	private ArrayList<Group> chatGroups;
	
	private Group group;
	
	String SERVER_IP;
	
	String username;
	public creatGroupController(String IPAddress) {
		SERVER_IP = IPAddress;
	}

	@FXML
	void handleAdd(ActionEvent event) throws RemoteException, MalformedURLException, NotBoundException {
		ObservableList<String> selectedItems = TV_OnlineUsers.getSelectionModel().getSelectedItems();

		if(nameGroup.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR, "Veuillez entrer un nom de groupe", ButtonType.OK);
    		alert.showAndWait();
		}if(nameGroup.getText().contains("#")) {
			Alert alert = new Alert(AlertType.ERROR, "Veuillez enlever le(s) symbole(s) '#'", ButtonType.OK);
    		alert.showAndWait();
		}else if(exist(nameGroup.getText())) {
			Alert alert = new Alert(AlertType.ERROR, "Ce nom existe d�j� veuillez choisir un autre nom", ButtonType.OK);
    		alert.showAndWait();
		}else if (selectedItems.size()==0){
			Alert alert = new Alert(AlertType.ERROR, "Veuillez selectionner au moins un utilisateur", ButtonType.OK);
    		alert.showAndWait();
		}else{
			ArrayList<String> usersSelected = new ArrayList<>();
			for(String item:selectedItems) 
				usersSelected.add(item);
			usersSelected.add(username); 
			group = new Group(nameGroup.getText(), usersSelected);
			ChatService chatService = (ChatService) Naming.lookup("rmi://" + SERVER_IP + "/list");
			chatService.addGroup(group);
			((Stage)(add.getScene().getWindow())).close();
		}
		
	}
	
	private boolean exist(String chosenName) {
		for(Group group:chatGroups) 
			if(group.getName().equals(chosenName))
				return true;
		return false;
	}

	public void setOnlineUsers(ArrayList<String> onlineUsers) {
		oneline_users_Column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		TV_OnlineUsers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ObservableList<String> listItems = FXCollections.observableArrayList();
		for(String item: onlineUsers)
			if(item.charAt(0)!='#')
				listItems.add(item);
		TV_OnlineUsers.setItems(listItems);
	}
	
	public Group getGroup() {return group;}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setChatGroups(ArrayList<Group> g) {
		this.chatGroups = g;
	}
	

}
