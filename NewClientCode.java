
import java.net.*;
import java.io.*;
import java.util.*;

public class NewClientCode  {

	
	private ObjectInputStream input;		
	private ObjectOutputStream output;		
	private Socket socket = null;

	private NewClientFrame CFrame;
	private String serverID, name, targetIP;
	private int port;
	private boolean isExist = false;
                    
	
	NewClientCode(String serverID, int port, String name) {
		this(serverID, port, name, null);
	}

	NewClientCode(String serverID, int port, String name, NewClientFrame CFrame) {
		this.serverID = serverID;
		this.port = port;
		this.name = name;
		this.CFrame = CFrame;
	}
	
	public String getTargetIP() {return targetIP;}
	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;}
	
	public boolean getIsExist() {return isExist;}
	public void setIsExist(boolean isExist) {
		this.isExist = isExist;}


	/*Start Function*/	
	public boolean start() {
		
		try {
			/*Client Socket*/
			socket = new Socket(serverID, port);
		} 

		catch(Exception e) {
			System.out.println("Error connecting at start:" + e);
			return false;
		}
		
		/*If success, prints accept with address and message*/
		String message = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		appendFrame(message);
	
		/* IO Stream for Client */
		try
		{
			input  = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
			System.out.println("Exception while creating I/O: " + e);
			return false;
		}

		/*Start listening for server*/
		new serverListener().start();
		

		try
		{
			output.writeObject(name);
		}
		catch (IOException e) {
			System.out.println("Error while writing name to output : " + e);
			closeClient();
			return false;
		}

		/*If works*/
		return true;
	}

	/*Print Message Function*/
	private void appendFrame(String message) {
			CFrame.append(message + "\n");
	}
	
	
	/*Forward Message Function*/
	void forwardMessage(NewMessageCode message) {
		try {
			output.writeObject(message);
		}
		catch(IOException e) {
			System.out.println("Exception writing to server: " + e);
		}
	}

	/*Close Connection*/
	private void closeClient() {
		try { 
			if(input != null) input.close();
		}
		catch(Exception e) {} 
		try {
			if(output != null) output.close();
		}
		catch(Exception e) {}
                                     try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		
		
		if(CFrame != null)
			CFrame.connectionFailed();
			
	}


	/*Multithreading Function that listens and creates thread per new connection*/
	class serverListener extends Thread {

		public void run() {
                                         boolean run = true;
			while(run) {
				try {
					String message = (String) input.readObject();
					
					
						/*Prints IN GUI*/
						CFrame.append(message);
						if(message.contains("Agent connected with IP:")){
							String destIP[] = message.substring(25).split("\n");
							setTargetIP(destIP[0]);
						}
						if(message.contains("Connect client successfully.")){
							setIsExist(true);
						}
						if(message.equals("Error: cannot run more than 2 client")){
                                                                                                                            run = false;
							CFrame.connectionFailed();
						}
						if(message.contains("Chat disconnected, thank you please come again.")) {
                                                                                                                            run = false;
                                                                                                                            CFrame.connectionFailed();
							setTargetIP(null);
						}
					
				}
				catch(IOException e) {
					System.out.println("Server has close the connection: " + e);
					
						
					break;
				}
				
				catch(ClassNotFoundException no2) {
				}
			}
		}
	}

	/*To check if theres any current active clients*/
	public synchronized void clientChkExist(String ipAddress) {
		this.forwardMessage(new NewMessageCode(NewMessageCode.CHKUSERS, ipAddress));
	}

}

