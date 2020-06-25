import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.Timer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatController implements Initializable, ChatObserver {

	ChatService chatService;
	ChatObserver chatObserver;
	String username;
		
    @FXML
    private AnchorPane mainPane;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button buttonLogout;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextArea chatBox;

    @FXML
    private Button buttonSend;

    @FXML
    private Label usernameLabel;

    @FXML
    private AnchorPane titleBar;

    @FXML
    private Button buttonClose;
    
    @FXML
    private VBox chatVBox;
    
    
	private Timer timer;

    @FXML
    void closeClicked(ActionEvent event) {

    }

    @FXML
    void logoutClicked(ActionEvent event) throws IOException {
    	timer.cancel();
		timer.purge();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("application.fxml"));
		Parent root = (Parent)loader.load();
		Scene scene = new Scene(root);
		Stage stage = (Stage) usernameLabel.getScene().getWindow();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Application de communication");
		stage.show();
    }

	// Send a message if it's not empty
    @FXML
    void sendClicked(ActionEvent event) throws RemoteException {
	String textMessage = chatBox.getText().trim();
    	if(textMessage.equals(""))
    		return;
        String receiver = "abc";
        chatService.sendTextTo(username, receiver, textMessage);
        chatBox.setText("");
        chatBox.requestFocus();
    }
    
    public void initUsername(String pseudo) {
    	username = pseudo;
    	usernameLabel.setText(pseudo);
		Stage stage = (Stage) usernameLabel.getScene().getWindow();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	          public void handle(WindowEvent we) {
	              System.exit(0);
	          }
	      }); 
	}
    
    public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	// Load messages when someone sends a message
    @Override
    public boolean refreshMessages(String sender, String receiver, String text) {
        System.out.println(sender + ": "+ text);
        Text textMessage = new Text(text);
        textMessage.getStyleClass().add("text");
        TextFlow tempFlow = new TextFlow();
        if(!username.equals(sender)) {
        	Text textName = new Text(sender +": ");
        	textName.getStyleClass().add("textName");
        	tempFlow.getChildren().add(textName);
        }
        
        tempFlow.getChildren().add(textMessage);
        tempFlow.setMaxWidth(350);
        
        TextFlow flow = new TextFlow(tempFlow);
        
        HBox hbox = new HBox();
        
        if (!username.equals(sender)) {
        	tempFlow.getStyleClass().add("tempFlowFlipped");
        	flow.getStyleClass().add("textFlowFlipped");
        	chatVBox.setAlignment(Pos.TOP_LEFT);
        	hbox.setAlignment(Pos.CENTER_LEFT);
        	hbox.getChildren().add(flow);
        }
        else {
        	tempFlow.getStyleClass().add("tempFlow");
        	flow.getStyleClass().add("textFlow");
        	hbox.setAlignment(Pos.BOTTOM_RIGHT);
        	hbox.getChildren().add(flow);
        }
        
        hbox.getStyleClass().add("hbox");
        Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
        return true;
    }
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        String SERVER_IP = "localhost";
        try {
			chatService = (ChatService) Naming.lookup("rmi://" + SERVER_IP + "/list");
			chatObserver = new ChatObserverImpl(this);
			chatService.addChatObserver(chatObserver);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
