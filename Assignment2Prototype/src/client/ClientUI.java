package client;

import gui_client.LoginController;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientUI extends Application {
	public static ClientConsole chat; //only one instance

	public static void main( String args[] ) throws Exception
    { 
		launch(args);  
    }
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
		chat = new ClientConsole("localhost", 5555);
						  		
		LoginController loginFrame = new LoginController(); // create StudentFrame
		 
		loginFrame.start(primaryStage);
	}
}
