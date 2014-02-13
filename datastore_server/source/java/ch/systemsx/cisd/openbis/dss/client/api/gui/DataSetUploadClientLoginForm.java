/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author Juan Fuentes
 */
public class DataSetUploadClientLoginForm extends javax.swing.JFrame {

	//Default Serial Version ID
	private static final long serialVersionUID = 1L;
	
	// Variables declaration                    
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField serverURLField;
    private javax.swing.JLabel serverURLLabel;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JLabel titleLabel2;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    
    public javax.swing.JPasswordField getPasswordField() {
    	return passwordField;
    }

    public javax.swing.JTextField getServerURLField() {
    	return serverURLField;
    }

    public javax.swing.JTextField getUserNameField() {
    	return userNameField;
    }
    
    public javax.swing.JButton getLoginButton() {
    	return loginButton;
    }
    
    /**
     * Creates new form LoginForm
     */
    public DataSetUploadClientLoginForm() {
        initComponents();
    }
    
    private void initComponents() {

        loginButton = new javax.swing.JButton();
        serverURLLabel = new javax.swing.JLabel();
        serverURLField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        titleLabel1 = new javax.swing.JLabel();
        titleLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Data Set Uploader");
        setResizable(false);

        loginButton.setFont(loginButton.getFont());
        loginButton.setText("Login");

        serverURLLabel.setFont(serverURLLabel.getFont());
        serverURLLabel.setText("openBIS Server URL:");

        serverURLField.setFont(serverURLField.getFont());
        serverURLField.setText("http://localhost:8888");
        serverURLField.addKeyListener(new CopyPasteFromClipboard(serverURLField));
        
        userNameLabel.setFont(userNameLabel.getFont());
        userNameLabel.setText("User Name:");

        userNameField.setFont(userNameField.getFont());
        userNameField.setText("yourUser");
        userNameField.addKeyListener(new CopyPasteFromClipboard(userNameField));
        
        passwordLabel.setFont(passwordLabel.getFont());
        passwordLabel.setText("Password:");

        passwordField.setFont(passwordField.getFont());
        passwordField.setText("");
        passwordField.addKeyListener(new CopyPasteFromClipboard(passwordField));
        
        titleLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 70)); // NOI18N
        titleLabel1.setText("openBIS");

        titleLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        titleLabel2.setText("Data Set Uploader");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverURLLabel)
                            .addComponent(userNameLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(serverURLField, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(titleLabel1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(passwordLabel)
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loginButton)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(titleLabel2)
                .addGap(169, 169, 169))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(titleLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(titleLabel2)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverURLLabel)
                    .addComponent(serverURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loginButton)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        pack();
    }
    
    private class CopyPasteFromClipboard implements KeyListener {
    	
    	private final javax.swing.JTextField textField;
    	boolean isActionPressed = false;
    	boolean isPastePressed = false;
    	boolean isCopyPressed = false;
    	
    	public CopyPasteFromClipboard(javax.swing.JTextField textField) {
    		this.textField = textField;
    	}
    	
    	@Override
		public void keyPressed(KeyEvent keyPressed) {
    		if(keyPressed.getKeyCode() == KeyEvent.VK_CONTROL || keyPressed.getKeyCode() == KeyEvent.VK_META) {
    			isActionPressed = true;
    		} else if(keyPressed.getKeyCode() == KeyEvent.VK_V) {
    			isPastePressed = true;
    		} else if(keyPressed.getKeyCode() == KeyEvent.VK_C) {
    			isCopyPressed = true;
    		}
		}
    	
		@Override
		public void keyReleased(KeyEvent keyRelease) {
			
			if(isActionPressed && isCopyPressed) {
				textField.copy();
			} else if(isActionPressed && isPastePressed) {
				textField.paste();
			} 
			
			if(keyRelease.getKeyCode() == KeyEvent.VK_CONTROL || keyRelease.getKeyCode() == KeyEvent.VK_META) {
    			isActionPressed = false;
    		} else if(keyRelease.getKeyCode() == KeyEvent.VK_V) {
    			isPastePressed = false;
    		} else if(keyRelease.getKeyCode() == KeyEvent.VK_C) {
    			isCopyPressed = false;
    		}
		}
		
		@Override
		public void keyTyped(KeyEvent arg0) {
		}
    	
    }
}
