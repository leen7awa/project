package server;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer
{ 
	Connection conn;
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  
  public void handleMessageFromClient(Object msg, ConnectionToClient client)
  {
	  String data = (String) msg;
	  
		System.out.println("Message received: " + msg + " from " + client);
		try {
			ParseClientData(data, client);
			client.sendToClient(data);// added
		} 
		//catch (SQLException e) {e.printStackTrace();} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//this.sendToAllClients(msg);
	   
  }

    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
	  ConnectToDB();
	  System.out.println("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println("Server has stopped listening for connections.");
  }
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
      
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
  
  public void ConnectToDB() {
	  try {
	      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
	      System.out.println("Driver definition succeed");
	  } catch (Exception ex) {
		  /* handle the error*/
		  System.out.println("Driver definition failed");
      }
      
      try 
      {
          conn = DriverManager.getConnection("jdbc:mysql://localhost/prototype?serverTimezone=IST","root","Bv654gF11!");
          System.out.println("SQL connection succeed");
   	  } catch (SQLException ex)  { /* handle any errors*/
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
   	  }
  }
  
  @SuppressWarnings("unchecked")
  public void SaveToDB(Object message) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO subscriber "
				+ "(first_name, last_name, id, phone_number, email_address,"
				+ " credit_card_number, subscriber_number) VALUES (?, ?, ?, ?, ?, ?, ?)");
		ArrayList<String> data = (ArrayList<String>) message;
		
		try 
		{
			
			
			for (int i = 1; i < 8; i++)
				ps.setString(i, data.get(i-1));
			
			ps.executeQuery();
		} catch (SQLException e) { e.printStackTrace(); }
	}
  
  @SuppressWarnings("unchecked")
  public void UpdateToDB(String[] details) throws SQLException {
	  // data = {command id credit_card subscriber_num}
		PreparedStatement ps = conn.prepareStatement("UPDATE subscriber "
				+ "Set credit_card_number = ?, subscriber_number = ? "
				+ "Where id = ?");
		
		try 
		{
			ps.setString(2, details[3]);  // credit_card
			ps.setString(1, details[2]);  // subscriber_number
			ps.setString(3, details[1]);  // id
			
			ps.executeUpdate();
		} catch (SQLException e) { e.printStackTrace(); }
	}
  
  
  // CHANGE*****************************
  public ArrayList<String> ReadFromDB(String id) throws SQLException {
		Statement stmt;
		ArrayList<String> alldatabase = new ArrayList<>();
		try 
		{
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM subscriber WHERE id = " + id + ";");
	 		while(rs.next())
	 		{
				 // Print out the values
	 			for (int i = 1; i < 8; i++) {
	 				alldatabase.add(rs.getString(i));  // added
	 			}
			}
	 		
			rs.close();
		} catch (SQLException e) { e.printStackTrace(); }
		
		return alldatabase;
	}
  
  
  public void ParseClientData(String data, ConnectionToClient client) throws IOException {
	  String[] parsedData = data.split(" ");
	  ArrayList<String> temp = new ArrayList<String>();
	  //enum check possibility
	  try {
		  if (parsedData[0].equals("Update"))
			  UpdateToDB(parsedData);
		  
		  else if (parsedData[0].equals("Read"))
		  {
			  temp = ReadFromDB(parsedData[1]);
			  client.sendToClient(temp);
		  }
	  } catch(SQLException e) {e.printStackTrace();}
  }
  
  
}
//End of EchoServer class
