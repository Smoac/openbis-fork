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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * A dialog that allows the user to type in a passphrase.
 * 
 * @author Bernd Rinn
 */
public final class PassphraseDialog
{

    private static final long serialVersionUID = 1L;

    private static final int GENERATED_MEMORABLE_PASSPHRASE_LENGTH = 10;

    private static final int GENERATED_STRONG_PASSPHRASE_LENGTH = 40;

    private static final int PASSPHRASE_FIELD_LENGTH = 40;

    /**
     * @return The passphrase, or <code>null</code> if the user cancelled entering the passphrase.
     */
    public static String tryGetPassphraseForEncrypt(final Frame parentComponent,
            final String oldPassphase, final PasswordGenerator generatorOrNull, final String title,
            final String message)
    {
        return tryGetPassphrase(parentComponent, generatorOrNull, title, message, oldPassphase,
                true, true);
    }

    /**
     * @return The passphrase, or <code>null</code> if the user cancelled entering the passphrase.
     */
    public static String tryGetPassphraseForDecrypt(final Frame parentComponent,
            final String oldPassphase, final String title, final String message)
    {
        return tryGetPassphrase(parentComponent, null, title, message, oldPassphase, false, true);
    }

    /**
     * @return The passphrase, or <code>null</code> if the user cancelled entering the passphrase or
     *         entered an empty passphrase.
     */
    public static String tryGetPassphraseForDecryptRetry(final Frame parentComponent,
            final String title, final String message)
    {
        return tryGetPassphrase(parentComponent, null, title, message, null, false, false);
    }

    /**
     * @return The passphrase, or <code>null</code> if the user cancelled entering the passphrase.
     */
    public static String tryGetPassphrase(final Frame parentComponent,
            final PasswordGenerator generatorOrNull, final String title, final String message,
            final String oldPassphraseOrNull, final boolean encrypt,
            final boolean allowEmptyPassphrase)
    {
        // Whether the dialog has been cancelled. We use an AtomicBoolean to be able to change it
        // from anonymous classes which only can access final local variables.
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        final JDialog dialog = new JDialog(parentComponent, title, true);
        final JPanel panel = new JPanel(new SpringLayout());
        dialog.getContentPane().add(panel);

        final JLabel messageLabel =
                new JLabel("<html><center>" + message + "</center></html>", JLabel.CENTER);

        final JPanel passphrasePanel = new JPanel();
        final JLabel passphraseLabel = new JLabel("Passphrase");
        final JPasswordField passphraseField = new JPasswordField(PASSPHRASE_FIELD_LENGTH);
        passphraseField.setText(oldPassphraseOrNull);

        passphrasePanel.add(passphraseLabel);
        passphrasePanel.add(passphraseField);

        final JPanel buttonPanel = new JPanel();
        final JButton okButton = new JButton("OK");

        final JButton cancelButton = new JButton("Cancel");
        // Make pressing "Cancel" button react.
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    cancelled.set(true);
                    dialog.dispose();
                }
            });

        if (encrypt)
        {
            passphrasePanel.setLayout(new SpringLayout());
            final JLabel passphraseLabelRepeated = new JLabel("Passphrase (repeat)");
            final JPasswordField passphraseRepeatedField =
                    new JPasswordField(PASSPHRASE_FIELD_LENGTH);
            passphraseRepeatedField.setText(oldPassphraseOrNull);
            passphrasePanel.add(passphraseLabelRepeated);
            passphrasePanel.add(passphraseRepeatedField);
            SpringLayoutUtilities.makeCompactGrid(passphrasePanel, 2, 2, 5, 5, 5, 5);

            // Make pressing "Enter" in passphrase field react
            passphraseField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (canCloseRegularly(passphraseField, allowEmptyPassphrase))
                        {
                            passphraseRepeatedField.requestFocus();
                        }
                    }
                });
            // Make pressing "Enter" in passphraseRepeatedField field react
            passphraseRepeatedField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (canCloseRegularly(parentComponent, passphraseField,
                                passphraseRepeatedField, allowEmptyPassphrase))
                        {
                            dialog.dispose();
                        }
                    }
                });

            // Make pressing "OK" button react.
            okButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (canCloseRegularly(passphraseField, allowEmptyPassphrase))
                        {
                            dialog.dispose();
                        }
                    }
                });
            okButton.setEnabled(canCloseRegularly(passphraseField, allowEmptyPassphrase));

            final JButton clearPassphraseButton = new JButton("Clear");
            clearPassphraseButton.setToolTipText("Clear the currently entered passphrase");
            buttonPanel.add(clearPassphraseButton);
            clearPassphraseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        passphraseField.setText("");
                        passphraseRepeatedField.setText("");
                    }
                });

            if (generatorOrNull != null)
            {
                final JButton generateStrongPassphraseButton = new JButton("Generate Strong");
                generateStrongPassphraseButton
                        .setToolTipText("Create an extra strong (random) phassphrase");
                buttonPanel.add(generateStrongPassphraseButton);
                generateStrongPassphraseButton
                        .addActionListener(createStrongPassphraseGenerator(passphraseField,
                                passphraseRepeatedField, parentComponent, generatorOrNull));

                final JButton generateMemorablePassphraseButton = new JButton("Generate Memorable");
                generateMemorablePassphraseButton.setToolTipText("Create a memorable passphase");
                buttonPanel.add(generateMemorablePassphraseButton);
                generateMemorablePassphraseButton
                        .addActionListener(createMemorablePassphraseGenerator(passphraseField,
                                passphraseRepeatedField, parentComponent, generatorOrNull));
            }
        } else
        {
            // Make pressing "OK" button react.
            okButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (canCloseRegularly(passphraseField, allowEmptyPassphrase))
                        {
                            dialog.dispose();
                        }
                    }
                });
            okButton.setEnabled(canCloseRegularly(passphraseField, allowEmptyPassphrase));

            // Make pressing "Enter" in passphrase field react
            passphraseField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (canCloseRegularly(passphraseField, allowEmptyPassphrase))
                        {
                            dialog.dispose();
                        }
                    }
                });

            final JButton passphrasePasteFromClipboardButton = new JButton("Paste");
            passphrasePasteFromClipboardButton.setToolTipText("Paste Passphrase from Clipboard");
            buttonPanel.add(passphrasePasteFromClipboardButton);
            passphrasePasteFromClipboardButton.addActionListener(createPassphrasePaster(
                    passphraseField, parentComponent));
        }

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        if (allowEmptyPassphrase == false)
        {
            passphraseField.getDocument().addDocumentListener(
                    createOKButtonStateUpdater(okButton, passphraseField));
        }

        panel.add(messageLabel);
        panel.add(passphrasePanel);
        panel.add(buttonPanel);
        SpringLayoutUtilities.makeCompactGrid(panel, 3, 1, 5, 5, 5, 5);

        // Make ESC cancel the dialog.
        dialog.getRootPane().registerKeyboardAction(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    cancelled.set(true);
                    dialog.dispose();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Make pressing the "Close" window icon cancel the dialog.
        dialog.addWindowListener(createCloseWindowCancellingListener(cancelled));

        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.pack();
        dialog.setVisible(true);

        if (cancelled.get())
        {
            return null;
        } else
        {
            return new String(passphraseField.getPassword());
        }
    }

    private static ActionListener createStrongPassphraseGenerator(
            final JPasswordField passphraseField, final JPasswordField passphraseRepeatedField,
            final Frame parentComponent, final PasswordGenerator generatorOrNull)
    {
        return new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final String passphrase =
                            generatorOrNull.generatePassword(GENERATED_STRONG_PASSPHRASE_LENGTH,
                                    false);
                    passphraseField.setText(passphrase);
                    passphraseRepeatedField.setText(passphrase);
                    ClipboardUtils.copyToClipboard(passphrase);

                    JOptionPane.showMessageDialog(parentComponent,
                            "<html><center>The generated passphrase has been copied "
                                    + "to the clipboard.<br><br>"
                                    + "<em>Make sure you keep it safe or you "
                                    + "won't be able to decrypt the file!</em></center></html>",
                            "Your passphrase", JOptionPane.INFORMATION_MESSAGE);
                }
            };
    }

    private static ActionListener createMemorablePassphraseGenerator(
            final JPasswordField passphraseField, final JPasswordField passphraseRepeatedField,
            final Frame parentComponent, final PasswordGenerator generatorOrNull)
    {
        return new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final String passphrase =
                            generatorOrNull.generatePassword(GENERATED_MEMORABLE_PASSPHRASE_LENGTH,
                                    true);
                    passphraseField.setText(passphrase);
                    passphraseRepeatedField.setText(passphrase);
                    ClipboardUtils.copyToClipboard(passphrase);

                    JOptionPane.showMessageDialog(parentComponent,
                            "<html><center>The generated password is:<br><br><code><font size=+3>"
                                    + passphrase + "</font></code><br><br>(It has been copied to "
                                    + "the clipboard.)</center></html>", "Your password",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
    }

    private static ActionListener createPassphrasePaster(final JPasswordField passphraseField,
            final Frame parentComponent)
    {
        return new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        final String passphraseOrNull = ClipboardUtils.tryPasteClipboard();
                        if (passphraseOrNull == null)
                        {
                            JOptionPane.showMessageDialog(parentComponent,
                                    "No content in the clipboard.");
                        } else
                        {
                            passphraseField.setText(passphraseOrNull);
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(parentComponent,
                                "Error accessing clipboard.", "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
    }

    private static DocumentListener createOKButtonStateUpdater(final JButton okButton,
            final JPasswordField passphraseField)
    {
        return new DocumentListener()
            {
                public void removeUpdate(DocumentEvent e)
                {
                    updateOKButtonState();
                }

                public void insertUpdate(DocumentEvent e)
                {
                    updateOKButtonState();
                }

                void updateOKButtonState()
                {
                    okButton.setEnabled(passphraseField.getPassword().length > 0);
                }

                public void changedUpdate(DocumentEvent e)
                {
                    // Not interesting.
                }
            };
    }

    private static WindowListener createCloseWindowCancellingListener(final AtomicBoolean cancelled)
    {
        return new WindowListener()
            {
                public void windowOpened(WindowEvent e)
                {
                }

                public void windowIconified(WindowEvent e)
                {
                }

                public void windowDeiconified(WindowEvent e)
                {
                }

                public void windowDeactivated(WindowEvent e)
                {
                }

                public void windowClosing(WindowEvent e)
                {
                    cancelled.set(true);
                }

                public void windowClosed(WindowEvent e)
                {
                }

                public void windowActivated(WindowEvent e)
                {
                }
            };
    }

    private static boolean canCloseRegularly(final JPasswordField passphraseField,
            final boolean allowEmptyPassphrase)
    {
        return allowEmptyPassphrase || passphraseField.getPassword().length > 0;
    }

    private static boolean canCloseRegularly(final Frame parentComponent,
            final JPasswordField passphraseField, final JPasswordField passphraseRepeatedField,
            final boolean allowEmptyPassphrase)
    {
        final char[] passphrase = passphraseField.getPassword();
        final char[] passphraseRepeat = passphraseRepeatedField.getPassword();
        if (Arrays.equals(passphrase, passphraseRepeat) == false)
        {
            JOptionPane.showMessageDialog(parentComponent,
                    "You entered different passphrases, please check your input.",
                    "Passphrase mismatch",

                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return allowEmptyPassphrase || passphrase.length > 0;
    }
}
