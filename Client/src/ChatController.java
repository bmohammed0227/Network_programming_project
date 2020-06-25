import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatController implements Initializable, ChatObserver {

	ChatService chatService;
	ChatObserver chatObserver;
	String username;
	String receiver = "abc";
		
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
    void sendMessageClicked(ActionEvent event) throws RemoteException {
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
    
    @FXML
    void sendAudioClicked(ActionEvent event) {

    }

    @FXML
    void sendFileClicked(ActionEvent event) {

    }

    @FXML
    void sendImageClicked(ActionEvent event) throws RemoteException, IOException {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Selectionez une image pour l'envoyer:");
    	ExtensionFilter pngFilter = new ExtensionFilter("Images PNG (.png)", "*.png");
    	ExtensionFilter jpegFilter = new ExtensionFilter("Images JPEG (.jpeg)", "*.jpeg");
    	ExtensionFilter jpgFilter = new ExtensionFilter("Images JPG (.jpg)", "*.jpg");
    	fileChooser.getExtensionFilters().add(pngFilter);
    	fileChooser.getExtensionFilters().add(jpegFilter);
    	fileChooser.getExtensionFilters().add(jpgFilter);
    	File image = fileChooser.showOpenDialog(buttonSend.getScene().getWindow());
    	String imageName = image.getName();
    	if (imageName == null)
		  System.out.println("You cancelled the choice");
    	else if(image.length() < 25000000) {
    		System.out.println("You chose " + imageName);
			chatService.sendImageTo(username, receiver, new ImageIcon(image.getAbsolutePath()));
    	}
    	else {
    		Alert alert = new Alert(AlertType.ERROR, "The image "+imageName+" is larger than 25 MB !", ButtonType.OK);
    		alert.showAndWait();
    	}
    }

    @FXML
    void sendVideoClicked(ActionEvent event) {

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
    
 // Load messages when someone sends a message
    @Override
    public boolean refreshImages(String sender, String receiver, ImageIcon image) {
    	System.out.println(sender + " has sent an image");
//    	Image imageMessage = new Image(image);
        TextFlow tempFlow = new TextFlow();
        if(!username.equals(sender)) {
        	Text textName = new Text(sender +": ");
        	textName.getStyleClass().add("textName");
        	tempFlow.getChildren().add(textName);
        }
//        tempFlow.setMaxWidth(350);
        
        TextFlow flow = new TextFlow(tempFlow);
        
        HBox hbox = new HBox();
        Rectangle rectangle = new Rectangle();
        if(image.getIconWidth() > 300) {
        	rectangle.setWidth(300);
        	rectangle.setHeight(image.getIconHeight() * 300 / image.getIconWidth());
        }
        else {
        	rectangle.setWidth(image.getIconWidth());
			rectangle.setHeight(image.getIconHeight());
        }
        rectangle.getStyleClass().add("imageView");

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image.getImage(), 0, 0, null);
        bGr.dispose();

        rectangle.setFill(new ImagePattern(SwingFXUtils.toFXImage(bufferedImage, null)));
        if (!username.equals(sender)) {
        	tempFlow.getStyleClass().add("tempFlowFlipped");
        	flow.getStyleClass().add("textFlowFlipped");
        	chatVBox.setAlignment(Pos.TOP_LEFT);
        	hbox.setAlignment(Pos.CENTER_LEFT);
        	hbox.getChildren().add(flow);
        	hbox.getChildren().add(rectangle);
        }
        else {
        	tempFlow.getStyleClass().add("tempFlow");
        	flow.getStyleClass().add("textFlow");
        	hbox.setAlignment(Pos.BOTTOM_RIGHT);
        	hbox.getChildren().add(flow);
        	hbox.getChildren().add(rectangle);
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
