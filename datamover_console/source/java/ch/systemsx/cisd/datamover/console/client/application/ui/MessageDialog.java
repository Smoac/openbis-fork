/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MessageDialog extends DialogBox
{

    public static void showMessage(String title, String message)
    {
        final DialogBox dialogBox = new DialogBox(false, true);
        dialogBox.setText(title);

        VerticalPanel verticalPanel = new VerticalPanel();
        HorizontalPanel horizontalPanel = new HorizontalPanel();

        Label label = new Label(message);
        label.setStyleName("message-text");
        horizontalPanel.add(label);
        verticalPanel.add(horizontalPanel);
        Button ok = new Button("OK");
        ok.addClickListener(new ClickListener()
            {
                public void onClick(Widget sender)
                {
                    dialogBox.hide();
                }
            });
        verticalPanel.add(ok);
        dialogBox.setWidget(verticalPanel);
        dialogBox.center();
    }

}