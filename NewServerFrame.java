import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class NewServerFrame extends JFrame implements ActionListener, WindowListener{
	
    /*Declaration and Initialization*/
    private NewServerCode server;
    int port = 8080;
    
    /*GUI*/
    private JButton startButton;
    private JTextArea display;
    private JTextField portNum;
    
    /*Constructor*/
    NewServerFrame(int port) {
        server = null;

        display = new JTextArea(100,100);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        startButton = new JButton("Start Server");
        startButton.addActionListener(this);
        portNum = new JTextField(" " + port);
        
        /*Display Panel*/
        JPanel panel1 = new JPanel(); 
        panel1.setLayout(new GridLayout(1,1));
        panel1.add(new JScrollPane(display));
        add(panel1); 

        /*Bottom Panel*/
        JPanel panel2 = new JPanel(); 
        panel2.setLayout(new BorderLayout());
        panel2.add(portNum);
        panel2.add(startButton);
        
        add(panel2, BorderLayout.SOUTH);

        addWindowListener(this);
    }	
    
   
    void append(String message) {
            display.append(message);
    }
    
    /*Start Button Action*/
    public void actionPerformed(ActionEvent e) {
        int port;
        try {
            port = Integer.parseInt(portNum.getText().trim());
        }
        catch(Exception er) {
            append("Error, check the back end Port Number.");
            return;
        }
        server = new NewServerCode(port, this); 
        new startServer().start();
    }
    
    /*Start Server*/
    class startServer extends Thread {
        @Override
        public void run() {
            server.start();         
            append("Connection fail. \n"); 
            server = null;
        }
    }

    /*Primary Function*/
    public static void main(String[] arg) {
            NewServerFrame serverFrame = new NewServerFrame(8080);
            serverFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            serverFrame.setTitle("Server"); 
            serverFrame.setVisible(true); 
            serverFrame.setSize(300, 300); 
            serverFrame.setLocationRelativeTo(null); 
    }

   
    public void windowClosing(WindowEvent e) {
            if(server != null) {
                    try {
                            server.stop();			
                    }
                    catch(Exception eClose) {
                    }
                    server = null;
            }
            dispose();
            System.exit(0);
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    
}

