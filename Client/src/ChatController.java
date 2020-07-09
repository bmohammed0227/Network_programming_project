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
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Timer;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
//import org.im4java.core.ConvertCmd;
//import org.im4java.core.IM4JavaException;
//import org.im4java.core.IMOperation;
import com.healthmarketscience.rmiio.RemoteOutputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;
import javafx.application.Platform;
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
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
	HashMap<String, VBox> listChat = new HashMap<>();
	ArrayList<Text> listT = new ArrayList<>();
	HashMap<String, EventHandler<MouseEvent>> hashMapEvent = new HashMap<>();
	private Stage stageCreatController;
	String SERVER_IP;
		
	@FXML
    private Text info;
	
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
    private Button buttonSendText; 
   
    @FXML
    private Button buttonSendFile;

    @FXML
    private Button buttonSendImg;

    @FXML
    private Button buttonSendaudio;

    @FXML
    private Button buttonSendVid;
    
    @FXML
    private Label usernameLabel;

    @FXML
    private AnchorPane titleBar;

    @FXML
    private VBox chatVBox;
    
    @FXML
    private VBox usersVBox;
    
    @FXML
    private Button startCameraBtn;
    
    @FXML
    private VBox cameraVBox;
    
	private Timer timer;
	
	private boolean stopTask;
	
	private boolean logOut = false;
	public ChatController(String IPAddress) {
		SERVER_IP = IPAddress;
	}

	private void closeWindowEvent(WindowEvent event) throws IOException {
		if(!logOut) {
			logoutClicked(null);
	        System.exit(0);
		}
    }
   
	@FXML
    void handleCreateGroup(ActionEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("creatGroupWindow.fxml"));
    	loader.setControllerFactory(c -> {
    		return new creatGroupController(SERVER_IP);
    	}); 
 	    Parent root = (Parent)loader.load();
 		Scene scene = new Scene(root);
 		stageCreatController= new Stage();
 		stageCreatController.setScene(scene);
 		stageCreatController.setResizable(false);
 		stageCreatController.setTitle("Crï¿½ation d'un groupe");
 		creatGroupController groupController = loader.getController();
 		groupController.setOnlineUsers(new ArrayList<String>(listChat.keySet()));
 		groupController.setUsername(username);
 		groupController.setChatGroups(this.chatService.getAllgroups());
 		stageCreatController.show();
    }
	
    @FXML
    void logoutClicked(ActionEvent event) throws IOException {
    	logOut = true;
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
		
		if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
			chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
		
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
    	File audio = fileChooser.showOpenDialog(buttonSendText.getScene().getWindow());
    	String audioName = audio.getName();
    	if (audioName == null)
		  System.out.println("You cancelled the choice");
    	else if(audio.length() < 25000000) {
    		System.out.println("You chose " + audioName);
    		
    		FileInputStream inputStream = new FileInputStream(audio);
    		
    		SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
    		
    		
				Task<Boolean> task = new Task<Boolean>() {
					public Boolean call() {
						boolean result = false;
						if(receiver2.charAt(0) != '#') {
							try {
								result = chatService.sendFile(username, receiver2, audioName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}else{
							try {
								result = chatService.sendFileToGroup(username, receiver2, audioName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						return result;
					}
				};
				task.setOnSucceeded(e -> {
        			displayAudio(username, receiver2, audio.getAbsolutePath());
				});
				new Thread(task).start();
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
	    File file = fileChooser.showOpenDialog(buttonSendText.getScene().getWindow());
	    String fileName = file.getName();
	    if (file !=null) {
	    	if(file.length() < 25000000) {
	    		System.out.println("You chose " + file.getName());
				FileInputStream inputStream = new FileInputStream(file);
				
				SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
				
				Task<Boolean> task = new Task<Boolean>() {
					public Boolean call() {
						boolean result = false;
						if(receiver2.charAt(0) != '#') {
							try {
								result = chatService.sendFile(username, receiver2, fileName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}else{
							try {
								result = chatService.sendFileToGroup(username, receiver2, fileName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						return result;
					}
				};
				task.setOnSucceeded(e -> {
						displayFile(username, receiver2, file.getAbsolutePath());
				});
				new Thread(task).start();
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
    	ExtensionFilter jpegFilter = new ExtensionFilter("Images JPEG (.jpeg)", "*.jpeg");
    	ExtensionFilter jpgFilter = new ExtensionFilter("Images JPG (.jpg)", "*.jpg");
    	fileChooser.getExtensionFilters().add(jpegFilter);
    	fileChooser.getExtensionFilters().add(jpgFilter);
    	File image = fileChooser.showOpenDialog(buttonSendText.getScene().getWindow());
    	String imageName = image.getName();
    	File outputImage = null;
    	String outputImageName = null;
    	final File finalOutputImage;
    	final String finalOutputImageName;
    	outputImage = image;
    	outputImageName = outputImage.getName();
    	finalOutputImage = outputImage;
    	finalOutputImageName = outputImageName;
    	if (outputImageName == null) {
          System.out.println("You cancelled the choice");
      }
      else if(outputImage.length() < 25000000) {
      		System.out.println("You chose " + imageName);

    		FileInputStream inputStream = new FileInputStream(outputImage);

    		SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
			Task<Boolean> task = new Task<Boolean>() {
				public Boolean call() {
					boolean result = false;
					  if(receiver2.charAt(0) != '#') {
						try {
							result = chatService.sendFile(username, receiver2, finalOutputImageName, remoteFileData);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}else{
						System.out.println("username :"+username);
						System.out.println("receiver2:"+receiver2);
						//Envoie groupe
						try {
							result = chatService.sendFileToGroup(username, receiver2, finalOutputImageName, remoteFileData);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					return result;
				}
			};
			task.setOnSucceeded(e -> {
				
							displayImage(username, receiver2, finalOutputImage.getAbsolutePath());
				
			});
			new Thread(task).start();

		  }
      else {
            Alert alert = new Alert(AlertType.ERROR, "The image "+outputImageName+" is larger than 25 MB !", ButtonType.OK);
            alert.showAndWait();
    }    
}

    
    @FXML
    void sendVideoClicked(ActionEvent event) throws RemoteException, IOException {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Selectionez une image pour l'envoyer:");
    	ExtensionFilter mp4Filter = new ExtensionFilter("Videos MP4 (.mp4)", "*.mp4");
    	ExtensionFilter mkvFilter = new ExtensionFilter("Videos MKV (.mkv)", "*.mkv");
    	fileChooser.getExtensionFilters().add(mp4Filter);
    	fileChooser.getExtensionFilters().add(mkvFilter);
    	File video = fileChooser.showOpenDialog(buttonSendText.getScene().getWindow());
    	String videoName = video.getName();
    	if (videoName == null)
		  System.out.println("You cancelled the choice");
    	else if(video.length() < 250000000) {
    		System.out.println("You chose " + videoName);
    		
    		FileInputStream inputStream = new FileInputStream(video);
    		
    		SimpleRemoteInputStream remoteFileData = new SimpleRemoteInputStream(inputStream);
    		
				Task<Boolean> task = new Task<Boolean>() {
					public Boolean call() {
						boolean result = false;
						if(receiver2.charAt(0) != '#') {
							try {
								result = chatService.sendFile(username, receiver2, videoName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}else{
							try {
								result = chatService.sendFileToGroup(username, receiver2, videoName, remoteFileData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						return result;
					}
				};
				task.setOnSucceeded(e -> {
						displayVideo(username, receiver2, video.getAbsolutePath());
				});
				new Thread(task).start();
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
		    	String extension = filename.substring(filename.lastIndexOf('.'));
	    		if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg")) {
		    		if (!username.equals(sender)) {
						if(toGroup) displayImage(sender, groupName, filename);
		    			else displayImage(sender, receiver, filename);
		    		}
		    	}
		    	else if (extension.equalsIgnoreCase(".mp4")) {
		    		if (!username.equals(sender)) {
						if(toGroup) displayVideo(sender, groupName, filename);
		    			else displayVideo(sender, receiver, filename);
		    		}
		    	}
		    	else if (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".wav")) {
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
				
				if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
					chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
				
				hbox.getStyleClass().add("hbox");
				Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		    }	
	    }
    	return true;
    }
    
	private boolean displayAudio(String sender, String receiver3, String filename) {
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
			
			if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
				chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}
	
	private boolean displayFile(String sender, String receiver3, String filename) {
			System.out.println("DisplayFile");
			System.out.println("fileName:"+filename);
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
			
			if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
				chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}

	private boolean displayVideo(String sender, String receiver3, String filename) {
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
			// Resize
			if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
				chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
			
			
			hbox.getStyleClass().add("hbox");
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
		//}
		return true;
	}

	private boolean displayImage(String sender, String receiver3, String filename) {
		System.out.println("displayImages");
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
				BufferedImage bufferedImage = null;
				if(!username.equals(sender))
					try {
						File imageFile = new File(username+"-"+filename);
						System.out.println(imageFile.getAbsolutePath());
						bufferedImage = ImageIO.read(imageFile.getAbsoluteFile());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				else
					try {
						System.out.println(new File(filename).toString());
						bufferedImage = ImageIO.read(new File(filename));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				// Create a buffered image with transparency
				BufferedImage newBufferedImage = null;
				if (bufferedImage != null) {
					newBufferedImage = new BufferedImage (bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB); 
					if(newBufferedImage.getWidth() > 350) {
						rectangle.setWidth(350);
						rectangle.setHeight(newBufferedImage.getHeight() * 350 / newBufferedImage.getWidth());
					}
					else {
						rectangle.setWidth(newBufferedImage.getWidth());
						rectangle.setHeight(newBufferedImage.getHeight());
					}
					rectangle.getStyleClass().add("imageView");
	
					// Draw the image on to the buffered image
					Graphics2D bGr = newBufferedImage.createGraphics();
					bGr.drawImage(bufferedImage, 0, 0, null);
					bGr.dispose();
					rectangle.setFill(new ImagePattern(SwingFXUtils.toFXImage(newBufferedImage, null)));
				}
				else 
					System.out.println("Couldn't load the image");
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
			hbox.getChildren().add(rectangle);
		}
		else {
			if(receiver3.charAt(0)!='#') chatVBox = listChat.get(receiver2);
			else chatVBox = listChat.get(receiver3);
			tempFlow.getStyleClass().add("tempFlow");
			flow.getStyleClass().add("textFlow");
			hbox.setAlignment(Pos.BOTTOM_RIGHT);
			hbox.getChildren().add(flow);
			hbox.getChildren().add(rectangle);
		}
		
		// Resize
		if(chatVBox.getHeight()>chatAnchorPane.getHeight()-350) 
			chatAnchorPane.setPrefHeight(chatAnchorPane.getHeight()+350);
		
		hbox.getStyleClass().add("hbox");
		Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
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
		buttonSendText.setDisable(true);
		buttonSendFile.setDisable(true);
		buttonSendImg.setDisable(true);
		buttonSendaudio.setDisable(true);
		buttonSendVid.setDisable(true);
		info.setText("");
		chatBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				try {
					if(keyEvent.getCode() == KeyCode.ENTER)
						sendMessageClicked(null);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
		});
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
					v.setPrefWidth(460);
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
		if(hashMapEvent.get(receiver2)==null) {
			chatBox.setDisable(true);
			buttonSendText.setDisable(true);
			buttonSendFile.setDisable(true);
			buttonSendImg.setDisable(true);
			buttonSendaudio.setDisable(true);
			buttonSendVid.setDisable(true);
			info.setText("");
		}
        return true;
	}
	
	EventHandler<MouseEvent> function_event(String user) {
		if(this.hashMapEvent.get(user)==null) {
			info.setText("");
			EventHandler<MouseEvent> event =  new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					   chatBox.setDisable(false);
					   buttonSendText.setDisable(false);
					   buttonSendFile.setDisable(false);
					   buttonSendImg.setDisable(false);
					   buttonSendaudio.setDisable(false);
					   buttonSendVid.setDisable(false);
					   receiver2 = user; // on affecte le receiver pour l'envoie des messages
						Platform.runLater(()->{
							System.out.println("L'utilisateur selectionnï¿½ : "+user);
							chatAnchorPane.getChildren().clear(); // on efface l'ancienne fenetre
							chatAnchorPane.getChildren().setAll(listChat.get(user)); // on ajoute la fenetre selectionnï¿½
							String information = "";
							try {
								if(user.charAt(0)=='#') {
									Group group = chatService.getGroup(user);
									information = "Membres:\n"+ group.getParticipants().toString();
								}else {
									Compte compte = chatService.getCompte(user);
									information = "Nom: "+compte.getFamilyname()+"\nPrénom: "+compte.getFirstname()+"\nAdresse Mail: "+compte.getEmail();
								}
								
							} catch (RemoteException e2) {
								e2.printStackTrace();
							}
							info.setText(information);
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

	@FXML
	protected void startCamera(ActionEvent event) {
		if(cameraVBox.getChildren().isEmpty()) {
			stopTask = false;
			Image loadingImage = new Image("file:res/loading.png");
			ImageView ownWebcam = new ImageView();
			ownWebcam.setFitWidth(cameraVBox.getPrefWidth());
			ownWebcam.setPreserveRatio(true);
			ownWebcam.setImage(loadingImage);
			cameraVBox.getChildren().add(ownWebcam);
			Task<Boolean> task = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(-1);
		            grabber.setImageWidth(1280);	// Recording Dimentsions
		            grabber.setImageHeight(720);
		            grabber.start();
					final Java2DFrameConverter converter = new Java2DFrameConverter();

					while (stopTask != true) {
						Frame frame = grabber.grab();
						if (frame == null) {
							break;
						}
						if (frame.image != null) {
							final Image image = SwingFXUtils.toFXImage(converter.convert(frame), null);
							ownWebcam.setImage(image);
						} else if (frame.samples != null) {
							final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
							channelSamplesShortBuffer.rewind();

							final ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

							for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
								short val = channelSamplesShortBuffer.get(i);
								outBuffer.putShort(val);
							}

						}
					}
					grabber.stop();
					grabber.release();
					return true;
				}
			};
			new Thread(task).start();
		}
		else {
			stopTask = true;
			cameraVBox.getChildren().clear();
		}
	}
}
