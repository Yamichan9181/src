import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;

public class NewClientFrame extends JFrame implements ActionListener {

    private String ipAddress;/*IP Address of current connected user*/
    private boolean conStatus;/*When connected true*/
    private NewClientCode clientObj;/*A client object (singular)*/
    private int initPort;/*Chosen port of 8080*/
    private int userType;/*Int type representing client or agent (subject to change)*/

    private JLabel instruction;/*Just the instruction*/
    private JTextField userInput;/*Input Username*/
    private JTextField ipAddInput, portNumInput;/*IP default localhost, port default 8080*/
    private JButton conButton; /*To connect*/
    private JButton disconButton;/*To disconnect*/
    private JButton activeUsers;
    private JButton linkClient;
    private JTextArea displayBox;
    private boolean justJoined;
    NewClientFrame(String serverID, int portClient, int userType, String user) {

        super(user);

        initPort = portClient;
        ipAddress = serverID;
        this.userType = userType;
        justJoined = false;
        instruction = new JLabel ("Input username and click 'Connect':");/*Just the instruction*/
        ipAddInput = new JTextField(serverID);/*IP address Default localhost*/
        ipAddInput.setColumns(8);
        portNumInput = new JTextField("" + portClient);/*Default port of 8080*/
        portNumInput.setColumns(6);
        userInput = new JTextField("Guest");  /*Input username*/
        userInput.setColumns(20);
        displayBox = new JTextArea("");/*Entire display area*/
        displayBox.setEditable(false);
        conButton = new JButton("Connect");/*To connect*/
        conButton.addActionListener(this);
        disconButton = new JButton("Disconnect");/*To disconnect*/
        disconButton.addActionListener(this);
        disconButton.setEnabled(false); 
        activeUsers = new JButton("Active Users");/*Check users online*/
        activeUsers.addActionListener(this);
        linkClient = new JButton("Connect to Client");/*For agent only: Connect to client*/
        linkClient.addActionListener(this);
        
        JPanel chatPanel = new JPanel();/*Display JPanel*/
        chatPanel.setLayout(new GridLayout(1,1));
        chatPanel.add(new JScrollPane(displayBox));
        add(chatPanel, BorderLayout.CENTER);

        JPanel funcPanel = new JPanel();
        funcPanel.setLayout(new GridLayout(4,2));
        JPanel flow1 = new JPanel();
        flow1.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel flow2 = new JPanel();
        flow2.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel flow3 = new JPanel();
        flow3.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel flow4 = new JPanel();
        flow4.setLayout(new FlowLayout(FlowLayout.CENTER));
        funcPanel.add(flow1);
        funcPanel.add(flow2);
        funcPanel.add(flow3);
        funcPanel.add(flow4);
        flow1.add(instruction);/*Display of instruction*/
        flow2.add(userInput);/*User to input name and message*/
        flow3.add(new JLabel("IP Address:"));
        flow3.add(ipAddInput);/*Input box for IP*/
        flow3.add(new JLabel("  "));
        flow3.add(new JLabel("Port Number:"));
        flow3.add(portNumInput);/*Input box for port*/
        flow3.add(new JLabel(""));
        flow4.add(conButton);
        flow4.add(disconButton);

        if (userType == 1) { 
            conButton.setText("Connect");
            instruction.setText("Admin: Input username and click 'Connect'");
            
            activeUsers.setEnabled(false); 
            flow4.add(activeUsers);
            linkClient.setEnabled(false); 			
            flow4.add(linkClient);
        } 
       add(funcPanel, BorderLayout.SOUTH);
       
    }

    void append(String str) {
            displayBox.append(str);
            displayBox.setCaretPosition(displayBox.getText().length() - 1);
    }

    void connectionFailed() {
            conButton.setEnabled(true);
            disconButton.setEnabled(false);
            if (userType == 1) {
                    activeUsers.setEnabled(false);
                    linkClient.setEnabled(false);
            }
            instruction.setText("Enter your username and click 'Connect'");

            portNumInput.setText("" + initPort);
            ipAddInput.setText(ipAddress);
            ipAddInput.setEditable(false);
            portNumInput.setEditable(false);
            userInput.removeActionListener(this);
            conStatus = false;
    }

    public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o == disconButton) {/*Disconnect the client*/
                    clientObj.forwardMessage(new NewMessageCode(NewMessageCode.DISCONNECT, ""));
                    return;
            }
            if (o == activeUsers) {/*Check for active users*/
                    clientObj.forwardMessage(new NewMessageCode(NewMessageCode.ISCONNECTED, ""));
                    return;
            }

            if (o == linkClient) {/*Agent only: link to client*/
                    if (linkClient.getText().equals("Connect to Client")) {
                            String destinationIP = userInput.getText().trim();
                            if (destinationIP.length() == 0)
                                    return;
                            clientObj.clientChkExist(destinationIP);
                            displayBox.append("Connecting...");
                            try {
                                    Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                            }
                            if (clientObj.getIsExist()) {
                                    clientObj.setTargetIP(destinationIP);
                                    userInput.setText("");
                                    instruction.setText("Type message here and press ENTER");
                                    conStatus = true;
                                    userInput.addActionListener(this);
                            } else {
                                    displayBox.append("IP address not found, unable to find client.\n");
                            }
                            linkClient.setText("Disconnect with client");
                    } else {
                            Date date = new Date();
                            linkClient.setText("Connect to Client");
                            conStatus = false;
                            userInput.setText("");
                            userInput.removeActionListener(this);
                            instruction.setText("Enter client's IP Address and click 'Connect Client'");
                            String filePath = System.getProperty("user.home");
                            File conversationLog = new File("log.txt");

                            System.out.println(conversationLog);
                            try {
                                    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(conversationLog, true));
                                    fileWriter.write(displayBox.getText());
                                    fileWriter.close();
                                    displayBox.append("Chat has been recorded. ");
                                    clientObj.forwardMessage(new NewMessageCode(NewMessageCode.MESSAGE, "Chat has been disconnected.", clientObj.getTargetIP()));
                            } catch(Exception exp){System.out.println("Exception while printing log file: " + exp);
                            }
                    }
            }

            if (conStatus) {/*No action performed as condition as this one is when user press ENTER*/
                    if (clientObj.getTargetIP() != null) {
                        
                            if(justJoined){/*If just joined do not send message*/
                                clientObj.forwardMessage(new NewMessageCode(NewMessageCode.MESSAGE, userInput.getText(), clientObj.getTargetIP()));
                                displayBox.append("You: " + userInput.getText() + "\n");
                            }
                    } else {
                            displayBox.append("Agent not found.\n");
                    }

                    userInput.setText("");
                    return;
            }


            if (o == conButton) {
                    String name = userInput.getText().trim();
                    if (name.length() == 0)
                            return;
                    String server = ipAddInput.getText().trim();
                    if (server.length() == 0)
                            return;
                    String portClient = portNumInput.getText().trim();
                    if (portClient.length() == 0)
                            return;
                    int port = 0;
                    try {
                            port = Integer.parseInt(portClient);
                    } catch (Exception ef) {
                            return; 
                    }

                    clientObj = new NewClientCode(server, port, name, this);/*Start a client*/
                    if (!clientObj.start())
                            return;

                    if (userType == 1) {/*If agent*/
                            userInput.setText("");
                            instruction.setText("Enter client's IP Address and click 'Connect Client'");
                            activeUsers.setEnabled(true);
                            linkClient.setEnabled(true);
                    } else {/*If client*/
                            userInput.setText("");
                            instruction.setText("Type your message and press ENTER to send");
                            conStatus = true;/*Set online status to true*/
                            userInput.addActionListener(this);/*Add action listener for ENTER on textbox*/
                    }

                    conButton.setEnabled(false);/*Declick conbutton, allow click on disconnect*/
                    disconButton.setEnabled(true);
                    ipAddInput.setEditable(false);/*Disallow edit of ip and port*/
                    portNumInput.setEditable(false);
            }
            justJoined = true;
    }

    public static void main(String[] args) {
        String [] choice = new String[]{"Client", "Agent"};
        String modeName = "";
        int mode = JOptionPane.showOptionDialog(null, "Login as: ", "Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choice, choice[0]);

        if(mode == 0){
            modeName = "Client";
        }else if(mode == 1){
            modeName = "Agent";
        }

        if(mode == 1){
            NewClientFrame clientFrame = new NewClientFrame("localhost", 8080, mode, modeName);
            clientFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            clientFrame.setSize(500, 400);
            clientFrame.setVisible(true);
            clientFrame.setLocationRelativeTo(null);
        }
        else if(mode == 0){
            NewClientFrame clientFrame = new NewClientFrame("localhost", 8080, mode, modeName);
            clientFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            clientFrame.setSize(400, 400);
            clientFrame.setVisible(true);
            clientFrame.setLocationRelativeTo(null);
        }
    }
}

