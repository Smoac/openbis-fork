/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * @author Pawel Glyzewski
 */
public class ErrorsPanel extends JPanel implements HyperlinkListener
{
    private static final long serialVersionUID = 5932698456919803620L;

    private static final String VALIDATION_ERRORS_CARD = "Errors";

    private static final String VALIDATION_WAIT_CARD = "Wait";

    private static final String VALIDATION_SUCCESS_CARD = "Success";

    private static final String WRONG_ICON_URL = ErrorsPanel.class.getResource("/wrong.png")
            .toString();

    private static final ImageIcon OK_ICON =
            new ImageIcon(ErrorsPanel.class.getResource("/ok.png"));

    private static final ImageIcon WAIT_ICON = new ImageIcon(
            ErrorsPanel.class.getResource("/wait.gif"));

    private final List<ValidationError> errors = new ArrayList<ValidationError>();

    private final ErrorMessageDialog errorMessageDialog;

    private final JEditorPane errorsArea;

    private final JScrollPane errorsAreaScroll;

    public ErrorsPanel(JFrame mainWindow)
    {
        super(new CardLayout());

        errorMessageDialog = new ErrorMessageDialog(mainWindow);

        errorsArea = new JEditorPane("text/html", "");
        errorsArea.addHyperlinkListener(this);
        errorsArea.setEditable(false);
        errorsArea.setBackground(getBackground());
        errorsArea.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
        errorsArea.setForeground(Color.RED);
        errorsAreaScroll = new JScrollPane(errorsArea);
        errorsAreaScroll.setBorder(BorderFactory.createEmptyBorder());
        errorsAreaScroll
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(errorsAreaScroll, VALIDATION_ERRORS_CARD);
        this.add(new JLabel("Validating, please wait...", WAIT_ICON, JLabel.CENTER),
                VALIDATION_WAIT_CARD);
        this.add(new JLabel("Validated successfully.", OK_ICON, JLabel.CENTER),
                VALIDATION_SUCCESS_CARD);
    }

    public void clear()
    {
        errorsArea.setText("");
        errorsArea.setToolTipText("");
        this.errors.clear();
    }

    public void reportError(ValidationError error)
    {
        errors.add(error);
        displayErrors();
    }

    private void displayErrors()
    {
        StringBuilder sb = new StringBuilder();

        int counter = 0;
        for (ValidationError error : errors)
        {
            sb.append(
                    "<center><div style='align: center' ><a href='http://openbis.ch/"
                            + (counter++)
                            + "' style='color: black; text-decoration: none; font-style: normal; vertical-align: middle' ><img border='0' hspace='10' src='")
                    .append(WRONG_ICON_URL).append("' />")
                    .append(truncateErrorMessage(error.getErrorMessage()))
                    .append(" [...]</a></div></center>");
        }

        errorsArea.setText(sb.toString());
    }

    private String truncateErrorMessage(String errorMessage)
    {
        int truncIndex = 80;

        int indexOfCR = errorMessage.indexOf('\r');
        if (indexOfCR > -1 && indexOfCR < truncIndex)
        {
            truncIndex = indexOfCR;
        }

        int indexOfLF = errorMessage.indexOf('\n');
        if (indexOfLF > -1 && indexOfLF < truncIndex)
        {
            truncIndex = indexOfLF;
        }

        return errorMessage.length() > truncIndex ? errorMessage.substring(0, truncIndex)
                : errorMessage;
    }

    public void hyperlinkUpdate(HyperlinkEvent event)
    {
        if (event.getEventType() == EventType.ACTIVATED)
        {
            URL url = event.getURL();
            int idx = Integer.parseInt(url.getPath().substring(1));
            errorMessageDialog.showErrorMessage(errors.get(idx).getErrorMessage());
        }
    }

    public void waitCard()
    {
        ((CardLayout) this.getLayout()).show(this, VALIDATION_WAIT_CARD);
    }

    public void showResult()
    {
        if (errors.isEmpty())
        {
            ((CardLayout) this.getLayout()).show(this, VALIDATION_SUCCESS_CARD);
        } else
        {
            ((CardLayout) this.getLayout()).show(this, VALIDATION_ERRORS_CARD);
        }
    }
}
