

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

class NewMessageCode implements Serializable 
{
    /*Declaration and Initialization*/

    protected static final long serialVersionUID = -4117845330381045897L;

    /*Types of Static Message*/
    static final int ISCONNECTED = 0; 
    static final int MESSAGE = 1; 
    static final int DISCONNECT = 2;
    static final int CHKUSERS = 3;
	
    /*Message Details*/
    private int messageType;
    private String message;
    private String messageIP;
	
    /*Constructor*/
    NewMessageCode(int messageType, String message) 
    {
	this.messageType = messageType;
	this.message = message;
    }
	
    /*Constructor*/
    NewMessageCode(int messageType, String message, String messageIP) 
    {
	this.messageType = messageType;
	this.message = message;
	this.messageIP = messageIP;
    }
	
    // setter
    public void setMessageIP(String messageIP)
    {
	this.messageIP = messageIP;
    }
	
    /* Getters*/
    int getMessageType() {return messageType;}
    String getMessage() {return message;}
    String getMessageIP() {return messageIP;}
    
}/*End of Class MessageCode*/



public class NewServerCode 
{	
    /*Declaration and Initialization*/
    private static int specID;

    /*ArrayList to hold the Clients*/
    private ArrayList<ClientHandler> clientList;

    /*Server GUI*/
    private NewServerFrame SFrame;
        
    /*Date*/
    private SimpleDateFormat sdf;
        
    /*default port number*/ 
    private int port = 8080;
        
    /*Infinite While Loop*/
    private boolean run;
	
    /*Constructor for Port*/
    public NewServerCode(int port) 
    {
        this(port, null);
    }
	
    /*Server GUI Constructor*/
    public NewServerCode(int port, NewServerFrame SFrame) 
    {
        this.SFrame = SFrame;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        clientList = new ArrayList<ClientHandler>();
    }
	
    /*Function Start*/
    public void start() 
    {
	/*While run = true, execute*/
        run = true;

	try 
	{
            /*Create Server Socket with port*/
            ServerSocket serverSocket = new ServerSocket(port);

            while(run) 
            {
                /*initial Clients no = 0*/
                int NoOfClients = 0;

                /*waiting for connection*/
                appendFrame("Listening for Clients on port " + port + ".");

                /*accepts and establish connection*/
                Socket socket = serverSocket.accept();  	

                if(!run)
                {
                    break;
                }		

                /*Client Handler List*/
                ClientHandler handler = new ClientHandler(socket); 
                clientList.add(handler);									
                handler.start();

                for(ClientHandler a: clientList) 
                {	
                    /*if matches, counter increases*/			
                    if(a.socket.getInetAddress().equals(socket.getInetAddress()))
                    {
                        NoOfClients++;
                    }

                }

                /*Only Two Clients per IP*/				
                if(NoOfClients > 2) 
                {
                    handler.forwardMessage("Error: cannot run more than 2 client");
                    removeClient(this.specID);
                    appendFrame("More than 2 client for each IP for Client: " + socket.getInetAddress());
                }

            }/*end of while loop*/

            /*Close after all communication seized*/
            try 
            {
                /*Close server socket*/
                serverSocket.close();

                for(int i = 0; i < clientList.size(); ++i) 
                {
                    ClientHandler handler = clientList.get(i);

                    try 
                    {
                        handler.input.close();
                        handler.output.close();
                        handler.socket.close();
                    }

                    catch(IOException ioE) {}
                }
            }

            catch(Exception e) 
            {
                appendFrame("Error closing the server and clients: " + e);
            }
        }
        
        catch (IOException e) 
        {
            String message = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            appendFrame(message);
        }

    }/*End of Start*/	

   
    @SuppressWarnings("resource")
    protected void stop() 
    {
        run = false;
                   
        try 
        {
            new Socket("localhost", port);
        }
        
        catch(Exception e) {}
    
    }

	
    /*Print out time with msg*/
    private void appendFrame(String message) 
    {
        String time = sdf.format(new Date()) + " " + message;
        SFrame.append(time + "\n");
        
    }
	
    /*Send to all function*/
    private synchronized void toAll(String message) 
    {
	/*added time to comment*/
	String time = sdf.format(new Date());
	String messageLf = time + " " + message + "\n";
        SFrame.append(messageLf);
        
	for(int i = clientList.size()-1; i >= 0;i--) 
        {
            ClientHandler a = clientList.get(i);
			
            if(!a.forwardMessage(messageLf)) 
            {
                clientList.remove(i);
                appendFrame("Disconnected Client " + a.name + " removed from list.");
            }
	}
    }

    /*LOGOUT message*/
    synchronized void removeClient(int tempClientNo) 
    {	
        System.out.println("Entering removal + " + clientList.size());
        /*scan the handler list for Client id*/
        System.out.println("Temp is " + tempClientNo);
        for(int i = clientList.size()-1; i >=0; i--) 
        {
            System.out.println("i: " + i);
            ClientHandler handler = clientList.get(i);
             System.out.println("Handler: "+handler.clientNo);
            if(handler.clientNo == tempClientNo) 
            {                       
                                     System.out.println("Client No + " + tempClientNo);
                                    clientList.get(i).interrupt();
		clientList.remove(i);
		return;
            }
        }
        System.out.println("Exiting removal + " + clientList.size());
    }

    public static void main(String[] args) 
    {
	/*Default main port : 8080*/
	int portMain = 8080;

	switch(args.length) 
        {
            case 1:
                try 
                {
                    portMain = Integer.parseInt(args[0]);
                }
				
                catch(Exception e) 
                {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
                    
            case 0:
                break;
	
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;		
	}

	/*Creating a server object*/
	NewServerCode server = new NewServerCode(portMain);
		
	/*Start object*/
	server.start();
    }

    /*Client Handler Function with Multithreading*/
    class ClientHandler extends Thread 
    {
	/*Socket and I/O Stream*/
	Socket socket;
	ObjectInputStream input;
	ObjectOutputStream output;
		
	/*Client Detail*/
	int clientNo;
	String name;
	NewMessageCode msgObj;

	/*Date*/
	String date;
	private int isNew = 0;
		
	/*Constructor*/
	ClientHandler(Socket socket) 
                   {
                            System.out.println("Making maself!");
                            clientNo = specID++;
                            this.socket = socket;

                            /* Creating both Data Stream */
                            System.out.println("Thread trying to create Object Input/Output Streams");
                            try
                            {
                                /*I/O Stream Creation*/
                                output = new ObjectOutputStream(socket.getOutputStream());
                                input  = new ObjectInputStream(socket.getInputStream());

                                /*Read Name*/
                                name = (String) input.readObject();
                                appendFrame(name + " just connected.");

                            }

                            catch (IOException e) 
                            {
                                appendFrame("Exception creating new Input/output Streams: " + e);
                                return;
                            }

                            // have to catch ClassNotFoundException
                            // but I read a String, I am sure it will work
                            catch (ClassNotFoundException e) {}

                            date = new Date().toString() + "\n";
            
	}/*end of clienthandler*/
		
	public int getIsNew() {return this.isNew;}
	public void setIsNew(int isNew) {this.isNew = isNew;}

	/*Read Msg*/
        @Override
	public void run() 
                  {
                        boolean run = true;

                        while(run) 
                        {
                            /*Read MsgObj*/
                            try 
                                               {
                                                      msgObj = (NewMessageCode) input.readObject();
                            }

                            catch (IOException e) 
                                              {
                                                    appendFrame(name + " Exception reading Streams: " + e);
                                                    break;				
                            }

                            catch(ClassNotFoundException e2) 
                                                {
                                                       break;
                            }

                            /*Get Message from MsgObj*/
                            String message = msgObj.getMessage();

                            /*Get IP from MsgObj*/
                            String targetIP = msgObj.getMessageIP();

                            /*Msg Type*/
                            switch(msgObj.getMessageType()) 
                                                    {
                                                            /*Type of Msg*/
                                                            case NewMessageCode.MESSAGE:
                                                                for(ClientHandler a: clientList) 
                                                                {
                                                                    // find target IP to send message
                                                                    if(a.socket.getInetAddress().toString().equals(("/" + targetIP).toString()))
                                                                    {					
                                                                        /*If No Client Connect, close server*/
                                                                        if(!socket.isConnected()) 
                                                                        {
                                                                            closeServer();
                                                                        }

                                                                        /*If there is*/
                                                                        if(a.getIsNew() == 0)
                                                                        {
                                                                            try 
                                                                            {
                                                                                a.setIsNew(1);
                                                                                a.output.writeObject("Agent connected with IP:" + socket.getInetAddress().toString() + "\n" + name + ":" + message + "\n");
                                                                            } 
                                                                            catch (IOException e) 
                                                                            {
                                                                                // TODO Auto-generated catch block
                                                                                e.printStackTrace();
                                                                            }

                                                                        } 
                                                                        else 
                                                                        {
                                                                            try 
                                                                            {
                                                                                a.output.writeObject(name + ":" + message + "\n");
                                                                            } 
                                                                            catch (IOException e) 
                                                                            {
                                                                                // TODO Auto-generated catch block
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                            break; /*Break for Case 1*/

                                                            /*If user disconnect, execute this part*/
                                                            case NewMessageCode.DISCONNECT:
                                                                forwardMessage("Chat disconnected, thank you please come again." + "\n");
                                                                appendFrame(name + " disconnected with a LOGOUT message.");
                                                                run = false;
                                                                
                                                            break;

                                                            /*If Connected, execute*/
                                                            case NewMessageCode.ISCONNECTED:

                                                                forwardMessage("List of the users connected at " + sdf.format(new Date()) + "\n");

                                                                for(int i = 0; i < clientList.size(); ++i) 
                                                                {
                                                                    ClientHandler handler = clientList.get(i);
                                                                    forwardMessage((i+1) + ") " + handler.name + " since " + handler.date + " with IP: " + handler.socket.getInetAddress() + "\n");
                                                                }

                                                            break;

                                                            /*To see current active users*/
                                                            case NewMessageCode.CHKUSERS:

                                                                for(ClientHandler a: clientList) 
                                                                {
                                                                    if(a.socket.getInetAddress().toString().equals("/" + message))
                                                                    {
                                                                        forwardMessage("Connect client successfully.\n");
                                                                    }
                                                                }
                                                            break;
                                                  }
                                    }

                                    removeClient(clientNo);
                                    closeServer();
	}
		

	/*Close function*/
	private void closeServer() 
                      {
                            try 
                            {
                                if(output != null) output.close();
                            }
                            catch(Exception e) {}
			
                            try 
                            {
                                if(input != null) input.close();
                            }
                            catch(Exception e) {};

                            try 
                            {
                                if(socket != null) socket.close();
                            }
                            catch (Exception e) {}
                    }

	/*Write msg*/
	private boolean forwardMessage(String message) 
                      {
                            /*If client not connected*/
                            if(!socket.isConnected()) 
                            {
                                closeServer();
                                return false;
                            }

                            /*write the msg to the output stream*/
                            try 
                            {
                                output.writeObject(message);
                            }

                            /*if cant msg*/
                            catch(IOException e) 
                            {
                                appendFrame("Error sending message to " + name);
                                appendFrame(e.toString());
                            }
                            return true;
	}
        
    }/*End of ClientHandler extends Thread*/
    
}/*End of Class NewServerCode*/




