/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A dialog that allows the user to type in a passphrase.
 * 
 * @author Bernd Rinn
 */
public final class PassphraseDialog
{

    private static final long serialVersionUID = 1L;

    /**
     * @return The passphrase, or <code>null</code> if the user cancelled entering the passphrase.
     */
    public static String tryGetPassphrase(Frame parentComponent, String title, String message)
    {
        final JDialog dialog = new JDialog(parentComponent, title, true);
        final JPanel panel = new JPanel(new SpringLayout());
        dialog.getContentPane().add(panel);

        final JLabel messageLabel =
                new JLabel("<html><center>" + message + "</center></html>", JLabel.CENTER);

        final JPanel passphrasePanel = new JPanel();
        final JLabel passphraseLabel = new JLabel("Passphrase");
        final JPasswordField passphraseField = new JPasswordField(40);
        final JPanel buttonPanel = new JPanel();
        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        passphrasePanel.add(passphraseLabel);
        passphrasePanel.add(passphraseField);
        // Make pressing Enter try decryption again.
        passphraseField.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (passphraseField.getPassword().length > 0)
                    {
                        dialog.dispose();
                    }
                }
            });
        passphraseField.getDocument().addDocumentListener(new DocumentListener()
            {
                public void removeUpdate(DocumentEvent e)
                {
                    toggleOKButton();
                }

                public void insertUpdate(DocumentEvent e)
                {
                    toggleOKButton();
                }

                void toggleOKButton()
                {
                    okButton.setEnabled(passphraseField.getPassword().length > 0);
                }

                public void changedUpdate(DocumentEvent e)
                {
                    // Not interesting.
                }
            });

        // Make pressing "OK" button try decryption again.
        okButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (passphraseField.getPassword().length > 0)
                    {
                        dialog.dispose();
                    }
                }
            });
        okButton.setEnabled(false);
        // Make pressing "Cancel" button try decryption again.
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    passphraseField.setText(null);
                    dialog.dispose();
                }
            });
        panel.add(messageLabel);
        panel.add(passphrasePanel);
        panel.add(buttonPanel);
        SpringLayoutUtilities.makeCompactGrid(panel, 3, 1, 5, 5, 5, 5);

        // Make ESC cancel the dialog.
        dialog.getRootPane().registerKeyboardAction(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    passphraseField.setText(null);
                    dialog.dispose();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.pack();
        dialog.setVisible(true);

        if (passphraseField.getPassword().length == 0)
        {
            return null;
        } else
        {
            return new String(passphraseField.getPassword());
        }
    }

}
