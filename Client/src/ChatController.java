import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.healthmarketscience.rmiio.RemoteOutputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ChatController implements Initializable, ChatObserver {

	ChatService chatService;
	ChatObserver chatObserver;
	String username;
	String receiver2;
	String SERVER_IP = "localhost";
	HashMap<String, VBox> listChat = new HashMap<>();
	ArrayList<Text> listT = new ArrayList<>();
	HashMap<String, EventHandler<MouseEvent>> hashMapEvent = new HashMap<>();
	private Stage stageCreatController;
//	String SERVER_IP = "172.23.139.139";
		
    @FXML
    private AnchorPane mainPane;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button buttonLogout;
    
    @FXML
    private Button buttonCreateGroup;
    
    @FXML
    private AnchorPane chatAnchorPane = new AnchorPane();

    @FXML
    private TextArea chatBox;

    @FXML
    private Button buttonSend;

    @FXML
    private Label usernameLabel;

    @FXML
    private AnchorPane titleBar;

    @FXML
    private VBox chatVBox;
    
    @FXML
    private VBox usersVBox;
    
	private Timer timer;
	
	private void closeWindowEvent(WindowEvent event) throws IOException {
		logoutClicked(null);
        System.exit(0);
    }
   
	@FXML
    void handleCreateGroup(ActionEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("creatGroupWindow.fxml"));
 	    Parent root = (Parent)loader.load();
 		Scene scene = new Scene(root);
 		stageCreatController= new Stage();
 		stageCreatController.setScene(scene);
 		stageCreatController.setResizable(false);
 		stageCreatController.setTitle("Création d'un groupe");
 		creatGroupController groupController = loader.getController();
 		groupController.setOnlineUsers(new ArrayList<String>(listChat.keySet()));
 		groupController.setUsername(username);
 		stageCreatController.show();
    }
	
    @FXML
    void logoutClicked(ActionEvent event) throws IOException {
    	chatService.removeChatObserver(chatObserver);
    	chatService.updateOnlineUsers();
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
        String receiver = receiver2;
        
        if(receiver.charAt(0) != '#') {
        	chatService.sendTextTo(username, receiver, textMessage);
        }else{
        	//Envoie groupe
        	chatService.sendTextToGroup(username, receiver, textMessage);
        }
        
        // on l'affiche directement sans l'envoyer et le recevoir
        Text TtextMessage = new Text(textMessage);
		TtextMessage.getStyleClass().add("text");
		TextFlow tempFlow = new TextFlow();
		tempFlow.getChildren().add(TtextMessage);
		tempFlow.setMaxWidth(350);
		TextFlow flow = new TextFlow(tempFlow);
		HBox hbox = new HBox();
		tempFlow.getStyleClass().add("tempFlow");
		flow.getStyleClass().add("textFlow");
		hbox.setAlignment(Pos.BOTTOM_RIGHT);
		hbox.getChildren().add(flow);
		hbox.getStyleClass().add("hbox");
		chatVBox = listChat.get(receiver2);
		Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
        
        
        chatBox.setText("");
        chatBox.requestFocus();
    }
    
    public void initUsername(String pseudo) throws RemoteException {
    	System.out.println("PSEUDO:["+pseudo+"]");
    	username = pseudo;
    	usernameLabel.setText(pseudo);
    	if (chatService != null)
			chatService.updateOnlineUsers();
		Stage stage = (Stage) usernameLabel.getScene().getWindow();
		stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, arg0 -> {
			try {
				closeWindowEvent(arg0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
    
    @FXML
    void sendAudioClicked(ActionEvent event) throws FileNotFoundException, RemoteException {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Selectionez une image pour l'envoyer:");
    	ExtensionFilter mp3Filter = new ExtensionFilter("Audio MP3 (.mp3)", "*.mp3");
    	ExtensionFilter wavFilter = new ExtensionFilter("Audio WAV (.wav)", "*.wav");
    	fileChooser.getExtensionFilters().add(mp3Filter);
    	fileChooser.getExtensionFilters().add(wavFilter);
    	File audio = fileChooser.showOpenDialog(buttonSend.getScene().getWindow());
    	String audioName = audio.getName();
    	if (audioName == null)
		  System.out.println("You cancelled the choice");
    	else if(audio.length() < 25000000) {
    		System.out.println("You chose " + audioName);
    		
    		FileInputStream inputStream = new FileInputStream(audio);
    		
    		SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
    		
    		
    		if(receiver2.charAt(0) != '#') {
    			if(chatService.sendFile(username, receiver2, audioName, remoteFileData)) {
        			displayAudio(username, receiver2, audio.getAbsolutePath());
        		}
	        }else{
	        	//Envoie groupe
	        	if(chatService.sendFileToGroup(username, receiver2, audioName, remoteFileData)) {
	    			displayAudio(username, receiver2, audio.getAbsolutePath());
	    		}
	        }
    		
    	}
    	else {
    		Alert alert = new Alert(AlertType.ERROR, "The audio file "+audioName+" is larger than 250 MB !", ButtonType.OK);
    		alert.showAndWait();
    	}
    }

    @FXML
    void sendFileClicked(ActionEvent event) throws RemoteException, FileNotFoundException {
    	FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Selectionez un fichier pour l'envoyer:");
	    File file = fileChooser.showOpenDialog(buttonSend.getScene().getWindow());
	    String fileName = file.getName();
	    if (file !=null) {
	    	if(file.length() < 25000000) {
	    		System.out.println("You chose " + file.getName());
				FileInputStream inputStream = new FileInputStream(file);
				
				SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
				
				if(receiver2.charAt(0) != '#') {
					if(chatService.sendFile(username, receiver2, fileName, remoteFileData)) {
						displayFile(username, receiver2, file.getAbsolutePath());
					}
		        }else{
		        	//Envoie groupe
		        	if(chatService.sendFileToGroup(username, receiver2, fileName, remoteFileData)) {
		        		displayFile(username, receiver2, file.getAbsolutePath());
					}
		        }
				
			}
			else {
				Alert alert = new Alert(AlertType.ERROR, "The file "+file.getName()+" is larger than 25 MB !", ButtonType.OK);
				alert.showAndWait();
			}
	    }
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
    	ImageIcon image2 = null;
    	if (imageName == null)
  		  System.out.println("You cancelled the choice");
      	else if(image.length() < 25000000) {
      		System.out.println("You chose " + imageName);
      		image2 = new ImageIcon(image.getAbsolutePath());
  			if(receiver2.charAt(0) != '#') {
  				chatService.sendImageTo(username, receiver2, image2);
  	        }else{
  	        	System.out.println("username :"+username);
  	        	System.out.println("receiver2:"+receiver2);
  	        	//Envoie groupe
  	        	chatService.sendImageToGroup(username, receiver2, image2);
  	        }
      	}
      	else {
      		Alert alert = new Alert(AlertType.ERROR, "The image "+imageName+" is larger than 25 MB !", ButtonType.OK);
      		alert.showAndWait();
      	}    
    	
    	
    	// affichage de l'image directement sans l'envoyer puis le recevoir
    	TextFlow tempFlow = new TextFlow();
    	TextFlow flow = new TextFlow(tempFlow);
    	 HBox hbox = new HBox();
	     Rectangle rectangle = new Rectangle();
	     if(image2.getIconWidth() > 300) {
	        rectangle.setWidth(300);
	        rectangle.setHeight(image2.getIconHeight() * 300 / image2.getIconWidth());
	     }
	     else {
	    	 rectangle.setWidth(image2.getIconWidth());
	    	 rectangle.setHeight(image2.getIconHeight());
	        }
	     rectangle.getStyleClass().add("imageView");
	     // Create a buffered image with transparency
	     BufferedImage bufferedImage = new BufferedImage(image2.getIconWidth(), image2.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
	     // Draw the image on to the buffered image
	     Graphics2D bGr = bufferedImage.createGraphics();
	     bGr.drawImage(image2.getImage(), 0, 0, null);
	     bGr.dispose();
	     rectangle.setFill(new ImagePattern(SwingFXUtils.toFXImage(bufferedImage, null)));
	     chatVBox = listChat.get(receiver2);
	     tempFlow.getStyleClass().add("tempFlow");
	     flow.getStyleClass().add("textFlow");
	     hbox.setAlignment(Pos.BOTTOM_RIGHT);
	     hbox.getChildren().add(flow);
	     hbox.getChildren().add(rectangle);
	     hbox.getStyleClass().add("hbox");
	     Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));

    	
    	
    	
    }

    
    @FXML
    void sendVideoClicked(ActionEvent event) throws RemoteException, IOException {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Selectionez une image pour l'envoyer:");
    	ExtensionFilter mp4Filter = new ExtensionFilter("Videos MP4 (.mp4)", "*.mp4");
    	ExtensionFilter mkvFilter = new ExtensionFilter("Videos MKV (.mkv)", "*.mkv");
    	fileChooser.getExtensionFilters().add(mp4Filter);
    	fileChooser.getExtensionFilters().add(mkvFilter);
    	File video = fileChooser.showOpenDialog(buttonSend.getScene().getWindow());
    	String videoName = video.getName();
    	if (videoName == null)
		  System.out.println("You cancelled the choice");
    	else if(video.length() < 250000000) {
    		System.out.println("You chose " + videoName);
    		
    		FileInputStream inputStream = new FileInputStream(video);
    		
    		SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
    		
    		if(receiver2.charAt(0) != '#') {
    			if(chatService.sendFile(username, receiver2, videoName, remoteFileData)) {
        			displayVideo(username, receiver2, video.getAbsolutePath());
        		}
	        }else{
	        	//Envoie groupe
	        	if(chatService.sendFileToGroup(username, receiver2, videoName, remoteFileData)) {
	    			displayVideo(username, receiver2, video.getAbsolutePath());
	    		}
	        }
    		
    	}
    	else {
    		Alert alert = new Alert(AlertType.ERROR, "The video "+videoName+" is larger than 250 MB !", ButtonType.OK);
    		alert.showAndWait();
    	}
    }

    
    public void setTimer(Timer timer) {
		this.timer = timer;
	}

	// Load messages when someone sends a message
    @Override
	public boolean refreshMessages(String sender, String receiver, String text) {
    	String groupName=null;
    	boolean toGroup = false;
    	if(sender.charAt(0)=='#') {
    		groupName = "#"+sender.split("#")[1];
    		sender = sender.split("#")[2];
    		toGroup = true;
    	}
    	
	    //if(!toGroup || (toGroup&&!sender.equals(receiver))) {
	   	if(receiver.equals(username)) {
		   	if (text.startsWith("[") && text.endsWith("]")) {
		    	String filename = text.substring(1, text.length()-1);
		    	if (filename.endsWith(".png") || filename.endsWith(".jpeg") || filename.endsWith(".jpg"))
		    		displayImage(sender, receiver, filename);
		    	else if (filename.endsWith(".mp4")) {
		    		if (!username.equals(sender)) {
						if(toGroup) displayVideo(sender, groupName, filename);
		    			else displayVideo(sender, receiver, filename);
		    		}
		    	}
		    	else if (filename.endsWith(".mp3") || filename.endsWith(".wav")) {
		    		if (!username.equals(sender)) {
		    			if(toGroup) displayAudio(sender, groupName, filename);
		    			else displayAudio(sender, receiver, filename);
		    		}
		    	}
		    	else {
		    		if (!username.equals(sender)) {
		    			if(toGroup) displayFile(sender, groupName, filename);
		    			else displayFile(sender, receiver, filename);
		    		}
		    	}
		    	return true;
		    }
		    else {
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
					if(!toGroup) chatVBox = listChat.get(sender);
						else chatVBox = listChat.get(groupName);
					tempFlow.getStyleClass().add("tempFlowFlipped");
					flow.getStyleClass().add("textFlowFlipped");
					chatVBox.setAlignment(Pos.TOP_LEFT);
					hbox.setAlignment(Pos.CENTER_LEFT);
					hbox.getChildren().add(flow);
				}
				else {
					if(!toGroup) chatVBox = listChat.get(receiver2);
    				else chatVBox = listChat.get(groupName);
					tempFlow.getStyleClass().add("tempFlow");
					flow.getStyleClass().add("textFlow");
					hbox.setAlignment(Pos.BOTTOM_RIGHT);
					hbox.getChildren().add(flow);
				}
				
				hbox.getStyleClass().add("hbox");
				Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		    }	
	    }
    	return true;
    }
    
	private boolean displayAudio(String sender, String receiver3, String filename) {
		//if(receiver3.equals(username) || sender.equals(username)) {
			System.out.println("displayVideo");
			System.out.println(sender + " has sent a video");
			TextFlow tempFlow = new TextFlow();
			if(!username.equals(sender)) {
				Text textName = new Text(sender +": ");
				textName.getStyleClass().add("textName");
				tempFlow.getChildren().add(textName);
			}
			
			TextFlow flow = new TextFlow(tempFlow);
			
			HBox hbox = new HBox();
			Image pauseButtonImage = new Image("file:res/pause-circle-fill.png");
			Image playButtonImage = new Image("file:res/play-circle-fill.png");
			ImageView imagePlayPause = new ImageView(playButtonImage);
			javafx.scene.control.Slider slider = new Slider();
			slider.setMinSize(300, 24);
			
			Task<Boolean> task = new Task<Boolean>() {
				public Boolean call() {
					try {
						if (!username.equals(sender))
							getFile(filename);
						return true;
					} catch (RemoteException e1) {
						e1.printStackTrace();
						return false;
					}
				}
			};
			task.setOnSucceeded(e -> {
				Boolean result = task.getValue();
				if (result) {
					Media audio = null;
					if(!username.equals(sender))
						audio = new Media(new File(username+"-"+filename).toURI().toString());
					else
						audio = new Media(new File(filename).toURI().toString());
					MediaPlayer mediaPlayer = new MediaPlayer(audio);

				    mediaPlayer.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
				        slider.setValue(newValue.toSeconds());
				    });
				    slider.setOnMouseClicked((MouseEvent mouseEvent) -> {
				        mediaPlayer.seek(Duration.seconds(slider.getValue()));
				    });	
					mediaPlayer.setOnEndOfMedia(new Runnable() {
						@Override
						public void run() {
							mediaPlayer.stop();
							imagePlayPause.setImage(playButtonImage);
						}
					});
					imagePlayPause.setOnMouseClicked(event -> {
						if(mediaPlayer.getStatus().equals(Status.PLAYING)) {
							mediaPlayer.pause();
							imagePlayPause.setImage(playButtonImage);
						}
						else {
							mediaPlayer.play();
							slider.maxProperty().bind(Bindings.createDoubleBinding(() -> mediaPlayer.getTotalDuration().toSeconds(), mediaPlayer.totalDurationProperty()));
							imagePlayPause.setImage(pauseButtonImage);
						}
					});
					System.out.println("Playing video");
				}
			});
			new Thread(task).start();
	
			if (!username.equals(sender)) {
				//TODO
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(sender);
				else chatVBox = listChat.get(receiver3);
				tempFlow.getStyleClass().add("tempFlowFlipped");
				flow.getStyleClass().add("textFlowFlipped");
				chatVBox.setAlignment(Pos.TOP_LEFT);
				hbox.setAlignment(Pos.CENTER_LEFT);
				hbox.getChildren().add(flow);
				hbox.getChildren().add(imagePlayPause);
				hbox.getChildren().add(slider);
			}
			else {
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(receiver2);
				else chatVBox = listChat.get(receiver3);
				tempFlow.getStyleClass().add("tempFlow");
				flow.getStyleClass().add("textFlow");
				hbox.setAlignment(Pos.BOTTOM_RIGHT);
				hbox.getChildren().add(flow);
				hbox.getChildren().add(imagePlayPause);
				hbox.getChildren().add(slider);
			}
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}
	
	//TODO
	private boolean displayFile(String sender, String receiver3, String filename) {
		//if(receiver3.equals(username) || sender.equals(username)) {
			System.out.println("DisplayFile");
			Hyperlink fileLink = new Hyperlink(filename.substring(filename.lastIndexOf('/')+1));
			fileLink.getStyleClass().add("filename");
			fileLink.setOnAction(event -> {
				Task<Boolean> task = new Task<Boolean>() {
					public Boolean call() {
						try {
							if (!username.equals(sender)) {
								getFile(filename);
							}
								
							return true;
						} catch (RemoteException e1) {
							e1.printStackTrace();
							return false;
						}
					}
				};
				task.setOnSucceeded(e -> {
					Boolean result = task.getValue();
					if (result) {
						File file = null;
						if(!username.equals(sender))
							file = new File(username+"-"+filename);
						else
							file = new File(filename);
						Desktop.getDesktop();
						if (Desktop.isDesktopSupported()) {
							new Thread(() -> {
						           try {
						        	   if(username.equals(sender))Desktop.getDesktop().open(new File(filename));
						        	   else Desktop.getDesktop().open(new File(username+"-"+filename));
						           } catch (IOException  e1) {
						               e1.printStackTrace();
						           }
						       }).start();
						}
						System.out.println("Opening File");
					}
				});
				new Thread(task).start();
			});
			TextFlow tempFlow = new TextFlow();
			if(!username.equals(sender)) {
				Text textName = new Text(sender +": ");
				textName.getStyleClass().add("textName");
				tempFlow.getChildren().add(textName);
			}
			
			tempFlow.getChildren().add(fileLink);
			tempFlow.setMaxWidth(350);
			
			TextFlow flow = new TextFlow(tempFlow);
			
			HBox hbox = new HBox();
			if (!username.equals(sender)) {
				//TODO
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(sender);
				else chatVBox = listChat.get(receiver3);
				//chatVBox = listChat.get(sender);
				tempFlow.getStyleClass().add("tempFlowFlipped");
				flow.getStyleClass().add("textFlowFlipped");
				chatVBox.setAlignment(Pos.TOP_LEFT);
				hbox.setAlignment(Pos.CENTER_LEFT);
				hbox.getChildren().add(flow);
			}
			else {
				//chatVBox = listChat.get(receiver3);
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(receiver2);
				else chatVBox = listChat.get(receiver3);
				tempFlow.getStyleClass().add("tempFlow");
				flow.getStyleClass().add("textFlow");
				hbox.setAlignment(Pos.BOTTOM_RIGHT);
				hbox.getChildren().add(flow);
			}
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}

	private boolean displayVideo(String sender, String receiver3, String filename) {
		//if(receiver3.equals(username) || sender.equals(username)) {
			System.out.println("displayVideo");
			System.out.println(sender + " has sent a video");
			TextFlow tempFlow = new TextFlow();
			if(!username.equals(sender)) {
				Text textName = new Text(sender +": ");
				textName.getStyleClass().add("textName");
				tempFlow.getChildren().add(textName);
			}
			
			TextFlow flow = new TextFlow(tempFlow);
			
			HBox hbox = new HBox();
			MediaView mv = new MediaView();
			mv.setFitWidth(350);
			
			Task<Boolean> task = new Task<Boolean>() {
				public Boolean call() {
					try {
						if (!username.equals(sender))
							getFile(filename);
						return true;
					} catch (RemoteException e1) {
						e1.printStackTrace();
						return false;
					}
				}
			};
			task.setOnSucceeded(e -> {
				Boolean result = task.getValue();
				if (result) {
					Media video = null;
					if(!username.equals(sender))
						video = new Media(new File(username+"-"+filename).toURI().toString());
					else
						video = new Media(new File(filename).toURI().toString());
					MediaPlayer mediaPlayer = new MediaPlayer(video);
					mv.setMediaPlayer(mediaPlayer);
					mediaPlayer.setOnEndOfMedia(new Runnable() {
						@Override
						public void run() {
							mediaPlayer.stop();
						}
					});
					mv.setOnMouseClicked(event -> {
						if(mediaPlayer.getStatus().equals(Status.PLAYING)) {
							mediaPlayer.pause();
						}
						else {
							mediaPlayer.play();
						}
					});
					System.out.println("Playing video");
				}
			});
			new Thread(task).start();
	
			if (!username.equals(sender)) {
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(sender);
				else chatVBox = listChat.get(receiver3);
				tempFlow.getStyleClass().add("tempFlowFlipped");
				flow.getStyleClass().add("textFlowFlipped");
				chatVBox.setAlignment(Pos.TOP_LEFT);
				hbox.setAlignment(Pos.CENTER_LEFT);
				hbox.getChildren().add(flow);
				hbox.getChildren().add(mv);
			}
			else {
				if(receiver3.charAt(0)!='#') chatVBox = listChat.get(receiver2);
				else chatVBox = listChat.get(receiver3);
				tempFlow.getStyleClass().add("tempFlow");
				flow.getStyleClass().add("textFlow");
				hbox.setAlignment(Pos.BOTTOM_RIGHT);
				hbox.getChildren().add(flow);
				hbox.getChildren().add(mv);
			}
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}

	private boolean displayImage(String sender, String receiver3, String image) {
		if(receiver3.equals(username) || sender.equals(username)) {
			System.out.println("displayImages");
			try {
				if(new File(image).isFile() || getFile(image)) {
					File imageFile = new File(image);
					System.out.println(sender + " has sent an image");
					TextFlow tempFlow = new TextFlow();
					if(!username.equals(sender)) {
						Text textName = new Text(sender +": ");
						textName.getStyleClass().add("textName");
						tempFlow.getChildren().add(textName);
					}
					
					TextFlow flow = new TextFlow(tempFlow);
					
					HBox hbox = new HBox();
					Rectangle rectangle = new Rectangle();
					BufferedImage tempBufferedImage = ImageIO.read(imageFile);
					// Create a buffered image with transparency
					BufferedImage bufferedImage = new BufferedImage (tempBufferedImage.getWidth(), tempBufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB); 
					if(bufferedImage.getWidth() > 300) {
						rectangle.setWidth(300);
						rectangle.setHeight(bufferedImage.getHeight() * 300 / bufferedImage.getWidth());
					}
					else {
						rectangle.setWidth(bufferedImage.getWidth());
						rectangle.setHeight(bufferedImage.getHeight());
					}
					rectangle.getStyleClass().add("imageView");
	
					// Draw the image on to the buffered image
					Graphics2D bGr = bufferedImage.createGraphics();
					bGr.drawImage(tempBufferedImage, 0, 0, null);
					bGr.dispose();
	
					rectangle.setFill(new ImagePattern(SwingFXUtils.toFXImage(bufferedImage, null)));
					if (!username.equals(sender)) {
						chatVBox = listChat.get(sender);
						tempFlow.getStyleClass().add("tempFlowFlipped");
						flow.getStyleClass().add("textFlowFlipped");
						chatVBox.setAlignment(Pos.TOP_LEFT);
						hbox.setAlignment(Pos.CENTER_LEFT);
						hbox.getChildren().add(flow);
						hbox.getChildren().add(rectangle);
					}
					else {
						chatVBox = listChat.get(receiver2);
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

// Load messages when someone sends a message
    @Override
    public boolean refreshImages(String sender, String receiver, ImageIcon image) {
    	
    	String groupName=null;
    	boolean toGroup = false;
    	if(sender.charAt(0)=='#') {
    		groupName = "#"+sender.split("#")[1];
    		sender = sender.split("#")[2];
    		toGroup = true;
    	}
    	
    	if(receiver.equals(username)) {
	    	System.out.println(sender + " has sent an image");
	        TextFlow tempFlow = new TextFlow();
	        if(!username.equals(sender)) {
	        	Text textName = new Text(sender +": ");
	        	textName.getStyleClass().add("textName");
	        	tempFlow.getChildren().add(textName);
	        }
	        
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
	        	if(!toGroup) chatVBox = listChat.get(sender);
				else chatVBox = listChat.get(groupName);
	        	tempFlow.getStyleClass().add("tempFlowFlipped");
	        	flow.getStyleClass().add("textFlowFlipped");
	        	chatVBox.setAlignment(Pos.TOP_LEFT);
	        	hbox.setAlignment(Pos.CENTER_LEFT);
	        	hbox.getChildren().add(flow);
	        	hbox.getChildren().add(rectangle);
	        }
	        else {
	        	if(!toGroup) chatVBox = listChat.get(receiver2);
				else chatVBox = listChat.get(groupName);
	        	tempFlow.getStyleClass().add("tempFlow");
	        	flow.getStyleClass().add("textFlow");
	        	hbox.setAlignment(Pos.BOTTOM_RIGHT);
	        	hbox.getChildren().add(flow);
	        	hbox.getChildren().add(rectangle);
	        }
	        
	        hbox.getStyleClass().add("hbox");
	        Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
    	}
        return true;
    }

    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			chatService = (ChatService) Naming.lookup("rmi://" + SERVER_IP + "/list");
			chatObserver = new ChatObserverImpl(this);
			chatService.addChatObserver(chatObserver);
			System.out.println("initialize");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		chatBox.setDisable(true);
	}

	@Override
	public boolean refreshOnlineUsers(ArrayList<String> onlineUsersList) throws RemoteException {
		for(int i=0; i<listT.size();i++) {
			listT.get(i).removeEventFilter(MouseEvent.MOUSE_CLICKED, this.function_event(listT.get(i).getText()));
		}
		hashMapEvent.clear();
		listT.clear();
		Platform.runLater(() -> usersVBox.getChildren().clear());
		int i = 0;
		for (String onlineUser : onlineUsersList) {
			if(!onlineUser.equals(this.username)) {
				Text textUsername = new Text(onlineUser);
				listT.add(textUsername);
				if(listChat.get(onlineUser)==null) { // Si un autre utilisateur vient de se connecter
					VBox v = new VBox(); // on creer le vbox associï¿½ a lui
					v.setPrefHeight(440);
					v.setPrefWidth(470);
					listChat.put(onlineUser, v); // on l'ajoute a la liste
				}
				listT.get(i).addEventFilter(MouseEvent.MOUSE_CLICKED, this.function_event(listT.get(i).getText()));
				i++;
//				textUsername.setTextAlignment(TextAlignment.CENTER);
//				textUsername.setFont(Font.font(20));
				textUsername.getStyleClass().add("onlineUser");
				TextFlow flow = new TextFlow();
				flow.getChildren().add(textUsername);
				flow.setMaxWidth(180);

				HBox hbox = new HBox();

				usersVBox.setAlignment(Pos.TOP_LEFT);
				hbox.setAlignment(Pos.CENTER_LEFT);
				hbox.getChildren().add(flow);

				hbox.getStyleClass().add("hbox");
				Platform.runLater(() -> usersVBox.getChildren().addAll(hbox));
			}
		}
		if(hashMapEvent.get(receiver2)==null) chatBox.setDisable(true);
        return true;
	}
	
	EventHandler<MouseEvent> function_event(String user) {
		if(this.hashMapEvent.get(user)==null) {
			EventHandler<MouseEvent> event =  new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					   chatBox.setDisable(false);
					   receiver2 = user; // on affecte le receiver pour l'envoie des messages
						Platform.runLater(()->{
							System.out.println("L'utilisateur selectionnï¿½ : "+user);
							chatAnchorPane.getChildren().clear(); // on efface l'ancienne fenetre
							chatAnchorPane.getChildren().setAll(listChat.get(user)); // on ajoute la fenetre selectionnï¿½
						});
				   } 
				};  
				hashMapEvent.put(user, event);
				return event;
		}else return hashMapEvent.get(user);
	}
	
	@Override
	public String getUsername() throws RemoteException {
		return username;
	}

	public ChatObserver getChatObserver() {
		return chatObserver;
	}
	
	public boolean getFile(String filename) throws RemoteException {
		RemoteOutputStreamServer outputStream = null;
		  try {
		    outputStream = new SimpleRemoteOutputStream(new BufferedOutputStream(new FileOutputStream(username+"-"+filename)));
		    chatService.getFile(outputStream.export(), filename);
		  System.out.println("Downloaded : "+filename);
			  return true;
		  } catch (FileNotFoundException e) {
			e.printStackTrace();
			  return false;
		} finally {
		    if(outputStream != null) {
		    	outputStream.close();
		    }
		  }
	}

	
}
