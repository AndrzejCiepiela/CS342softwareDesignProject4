import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientDriverClass extends Application{
	
	Stage clientWindow;
	Scene clientScene;
	
	ArrayList<String> activePlayers;
	Scene lobbyScene;
	Button challengeBtn;
	Button accept;
	Button update;
	Button returnToLobby;
	ArrayList<Button> challengeBtns = new ArrayList<Button>();
	ListView<String> playerList;
	int numPlayers = 1;
	String ID;
	Label idLabel;
	Object object;
	
	Button rock, paper, scissors, lizard, spock;
	
	private ClientNetwork conn = createClient("127.0.0.1", 5555);
	private TextArea messages = new TextArea();
	BorderPane lobby = new BorderPane();
	
	// NEW: returns an instance of the game lobby
	private Parent createLobby() {

		idLabel = new Label();
		playerList = new ListView();
		challengeBtn = new Button("challenge");
		accept = new Button("accept");
		update = new Button();
		lobby.setCenter(playerList);
		lobby.setRight(challengeBtn);
		lobby.setTop(idLabel);
		lobby.setBottom(accept);
		
		accept.setDisable(true);

		return lobby;

	}
	
	// create content that client will display
	private Parent createClientContent() {
		
		messages.setPrefHeight(300);
		
		EventHandler<ActionEvent> choice = event -> {
			
			// disable buttons when choice is made
			rock.setDisable(true);
			paper.setDisable(true);
			scissors.setDisable(true);
			lizard.setDisable(true);
			spock.setDisable(true);
			
			String client = "Your choice was: ";
			messages.clear();
			String message = "";
			
			if(event.getSource() == rock)
				message += "Rock";
			else if(event.getSource() == paper)
				message += "Paper";
			else if(event.getSource() == scissors)
				message += "Scissors";
			else if(event.getSource() == lizard)
				message += "Lizard";
			else if(event.getSource() == spock)
				message += "Spock";
			
			messages.appendText(client + message + "\n");
			try {
				conn.send(message);
			}
			catch(Exception e) {
				
			}
		};
		
		rock.setOnAction(choice);
		paper.setOnAction(choice);
		scissors.setOnAction(choice);
		lizard.setOnAction(choice);
		spock.setOnAction(choice);
		
		// root node for clientScene
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(30));
		pane.setStyle("-fx-background-color: #1ab546;");
		
		// create message area
		VBox root = new VBox(20, messages);
		pane.setCenter(root);
		
		// create game options
		VBox options = new VBox(20, returnToLobby);
		pane.setTop(options);
		options.setAlignment(Pos.CENTER);
		
		// create playable choices
		HBox buttons = new HBox(20, rock, paper, scissors, lizard, spock);
		pane.setBottom(buttons);
		
		return pane;
	}
	
	// NEW: updates the list displayed in the lobby
	public ArrayList<Button> updateLobby(ArrayList<String> arr) {
		// clear old active players
		int ID;
		// hold new array of buttons
		ArrayList<Button> tmp = new ArrayList<Button>();
		playerList.getItems().clear();
		String temp1, temp2;
		// update list with new active players
		for(int i = 0; i<arr.size(); i++) {
			//store the ID into a string so we can compare to this.String:ID
			ID = i + 1;
			temp1 = "" + ID;
			if((arr.get(i).equals("true") && ( !temp1.equals(this.ID) ))) {


				playerList.getItems().add("Player " + ID);
				
				String tmpID = Integer.toString(ID);
				tmp.add(new Button(tmpID));
			}

		}
		
		return tmp;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		ArrayList<String> choiceNames = new ArrayList<String>();
		choiceNames.add("Rock");
		choiceNames.add("Paper");
		choiceNames.add("Scissors");
		choiceNames.add("Lizard");
		choiceNames.add("Spock");
		
		ArrayList<ImageView> gameImages = new ArrayList<ImageView>();
		
		//create array of images
		for(int i = 0; i < 5; i++) {
			
			Image pic = new Image("GameIcons/" + choiceNames.get(i) + ".png");
			ImageView v = new ImageView(pic);
			v.setFitHeight(100);
			v.setFitWidth(50);
			v.setPreserveRatio(true);
			
			gameImages.add(v);
		}
		
		// creates new button
		rock = new Button("Rock");
		rock.setPadding(new Insets(5, 20, 5, 20));
		rock.setGraphic(gameImages.get(0));
		
		paper = new Button("Paper");
		paper.setPadding(new Insets(5, 20, 5, 20));
		paper.setGraphic(gameImages.get(1));
		
		scissors = new Button("Scissors");
		scissors.setPadding(new Insets(5, 20, 5, 20));
		scissors.setGraphic(gameImages.get(2));
		
		lizard = new Button("Lizard");
		lizard.setPadding(new Insets(5, 20, 5, 20));
		lizard.setGraphic(gameImages.get(3));
		
		spock = new Button("Spock");
		spock.setPadding(new Insets(5, 20, 5, 20));
		spock.setGraphic(gameImages.get(4));
		
		// disable game buttons until game starts
		rock.setDisable(true);
		paper.setDisable(true);
		scissors.setDisable(true);
		lizard.setDisable(true);
		spock.setDisable(true);
		
		// create and disable quit button
		returnToLobby = new Button("quit");
		returnToLobby.setPadding(new Insets(5, 20, 5, 20));
		returnToLobby.setDisable(true);
		
		// set window title
		primaryStage.setTitle("Client Window");
		
		// create first scene
		primaryStage.setScene(new Scene(createLobby()));
		
		// when challenge button is pressed
		EventHandler<ActionEvent> challengeButtons = event -> {
			
			// ID of opponent is registered depending on which button was pressed
			String ID = ((Button)event.getSource()).getText();

			// send a challenge message to the server
			try{
				conn.send(ID); }
			catch(Exception i) {
				i.printStackTrace();
			}
			
			messages.clear();
			
			// start the game
			primaryStage.setScene(new Scene(createClientContent()));
		};
		
		// when player accepts to play a game
		accept.setOnAction(e -> {
			
			// enable game buttons
			rock.setDisable(false);
			paper.setDisable(false);
			scissors.setDisable(false);
			lizard.setDisable(false);
			spock.setDisable(false);
			
			String acceptMessage = "accepted";
			
			try {
				conn.send(acceptMessage);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			messages.clear();
			
			// start the game
			primaryStage.setScene(new Scene(createClientContent()));
		});
		
		// update buttons by setting them to have an action event
		update.setOnAction(e -> {
			
			for(int i = 0; i < challengeBtns.size(); i++) {
				challengeBtns.get(i).setOnAction(challengeButtons);
			}
		});
		
		// when player presses quit button, return to lobby
		returnToLobby.setOnAction(e -> {
			
			
		});
		
		primaryStage.show();
	}
	
	@Override
	public void init() throws Exception{
		conn.startConn();
	}
	
	@Override
	public void stop() throws Exception{
		conn.closeConn();
	}
	
	private Client createClient(String address, int port) {
		return new Client(address, port, data -> {
			Platform.runLater(()->{
				messages.appendText(data.toString() + "\n");
				
				// if being challenged
				if(data.toString().intern() == "Your being challenged") {
					accept.setDisable(false);
				}
				
				// if opponent joined the game room
				if(data.toString().intern() == "Opponent has joined your game, begin playing") {
					
					// enable game buttons
					rock.setDisable(false);
					paper.setDisable(false);
					scissors.setDisable(false);
					lizard.setDisable(false);
					spock.setDisable(false);
				}
				
				// if game was a draw, continue playing
				if(data.toString().intern() == "There must be a winner, pick again") {
					
					// enable game buttons
					rock.setDisable(false);
					paper.setDisable(false);
					scissors.setDisable(false);
					lizard.setDisable(false);
					spock.setDisable(false);
				}
				
				// if there was a winner, enable quit button
				if(data.toString().intern() == "Thanks for playing, press quit to return to lobby") {
					returnToLobby.setDisable(false);
				}

				// parses the server message into an array of words
				String serverMessage[] =  data.toString().split(" ");
				ArrayList<String> updatedConnections = new ArrayList<String>();
				for(String word : data.toString().split(" ")) {
					updatedConnections.add(word);
				}
				
				// if first word of messaage is ID, set this.ID = to the corresponding ID (serverMessage[1])
				if(serverMessage[0].intern() == "ID") {
					this.ID = serverMessage[1];
					idLabel.setText("ID: " + serverMessage[1]);
				}

				// check if list of connected players has been updated
				// by checking if the first word sent is true or false
				// indicating a new list being sent
				try{
					if(updatedConnections.get(0).equals("true") || updatedConnections.get(0).equals("false") ) {
						challengeBtns = updateLobby(updatedConnections);
						
						VBox root = new VBox();
						
						// update buttons
						for(int i = 0; i < challengeBtns.size(); i++) {
							root.getChildren().add(challengeBtns.get(i));
						}
						
						// set buttons to lobby
						lobby.setRight(root);
						
						// call update event handler
						update.fire();
					}

				}
				catch(Exception e) {
					System.out.println("checked if data from inputstream was arrayList, was not");
				}
			});
		});
	}
}
