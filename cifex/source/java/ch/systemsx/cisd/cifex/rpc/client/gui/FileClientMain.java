package ch.systemsx.cisd.cifex.rpc.client.gui;

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.awt.BorderLayout;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

public class FileClientMain {
	//
	// CONSTANTS
	//
	private static final String TITLE = "CIFEX";
	private static final int WIDTH = 830;
	private static final int HEIGHT = 550;
	
	public static void main(String[] args) 
	{
		setLookAndFeelToNative();
		launchLogin();
	}
	
    private static void setLookAndFeelToNative()
    {
        // Set the look and feel to the native system look and feel, if possible
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex)
        {
            // just ignore -- no big deal
        }
    }
    
	private static void launchLogin()
	{
    	// Retrieve the user preferences for the login form
    	final Preferences prefs = Preferences.userNodeForPackage(FileClientLoginForm.class);

    	// Preference key name
    	final String PREF_NAME_SERVER = "PREF_NAME_SERVER";
    	final String PREF_NAME_USER = "PREF_NAME_USER";
    	
    	//Get the value of the preference
    	String serverValue = prefs.get(PREF_NAME_SERVER, null);
    	String userValue = prefs.get(PREF_NAME_USER, null);
    	
    	final FileClientLoginForm loginForm = new FileClientLoginForm();
    	loginForm.getLoginButton().addActionListener(new java.awt.event.ActionListener() 
    	{
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
            	//Collect login information
                String serverURL = loginForm.getServerURLField().getText();
                String userName = loginForm.getUserNameField().getText();
                String password = new String(loginForm.getPasswordField().getPassword());
                String cifexRPCService = "cifex/rpc-service";
                if(!serverURL.endsWith("/")) {
                    cifexRPCService = "/" + cifexRPCService;
                }
                String[] args = {serverURL + cifexRPCService, userName, password};
                
                try 
                {
                    //Check password
                    if(password.isEmpty()) {
                        throw new Exception("Password can't be empty.");
                    }
                	// Try To Login
                	CIFEXCommunicationState commState = new CIFEXCommunicationState(args);
                	// Display Main View
                	displayMainView(commState);
                	// Hide Login
                	loginForm.setVisible(false);
                	// Save correct preferences
                	prefs.put(PREF_NAME_SERVER, serverURL);
                	prefs.put(PREF_NAME_USER, userName);
                	prefs.flush();
                } catch(Exception ex)
                {
                	final JFrame frame = new JFrame(TITLE);
                    String message = ex.getMessage();
                    if (null == message || message.length() == 0)
                    {
                        message = ex.toString();
                    }
                    JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
    	
    	if(serverValue != null) 
    	{
    		loginForm.getServerURLField().setText(serverValue);
            loginForm.getUserNameField().setText(userValue);
    	}
        
    	loginForm.setVisible(true);
    	
    }
	
	private static JFrame displayMainView(CIFEXCommunicationState commState)
	{
		//
		// Tab Views
		//
		FileUploadClient uploadClient = new FileUploadClient(commState, SYSTEM_TIME_PROVIDER, false);
		FileDownloadClient downloadClient = new FileDownloadClient(commState, SYSTEM_TIME_PROVIDER, false);
		
		//
		// Panels View
		//
		JTabbedPane tabbedPanel = new JTabbedPane();
		tabbedPanel.addTab("Uploader", null, uploadClient.getWindowFrame().getContentPane(), "Uploader Text");
		tabbedPanel.addTab("Downloader", null, downloadClient.getWindowFrame().getContentPane(), "Downloader Text");
		
		//
		// Main Window
		//
		JFrame frame = new JFrame(TITLE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add content to the window.
		frame.add(tabbedPanel, BorderLayout.CENTER);

		//Display the window.
		//frame.pack();
		frame.setVisible(true);
		
		return frame;
	}
}
