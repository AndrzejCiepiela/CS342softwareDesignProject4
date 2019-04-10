
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ServerDriverClass extends Application{
	
	Stage serverWindow;
	Scene serverScene;
	Button portBtn, offBtn;
	
	int clientID = 1;
	
	private ServerNetwork conn = createServer();;
	private TextArea messages = new TextArea();
	
	// create content that server will display
	private Parent createServerContent() {
		
		messages.setPrefHeight(300);
		
		// root node for serverScene
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(30));
		pane.setStyle("-fx-background-color: #008B8B;");
		
		VBox root = new VBox(20, messages);
		pane.setCenter(root);
		
		// create game options
		VBox options = new VBox(20, offBtn);
		pane.setBottom(options);
		options.setAlignment(Pos.CENTER);
		
		return pane;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		offBtn = new Button("Turn Off Server");
		offBtn.setPadding(new Insets(5, 20, 5, 20));
		
		// set window title
		primaryStage.setTitle("Server Window");
		
		// create first scene
		primaryStage.setScene(new Scene(createServerContent()));
		
		// close server
		offBtn.setOnAction(event -> {
			primaryStage.close();
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
	
	private Server createServer() {
		return new Server(5555, data-> {
			Platform.runLater(()->{
				messages.appendText(data.toString() + "\n");
			
			});
		});
	}
}
