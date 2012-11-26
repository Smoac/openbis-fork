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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Info handler which shows info in a popup dialog.
 * 
 * @author Franz-Josef Elmer
 */
public class PopupDialogBasedInfoHandler extends Dialog implements IInfoHandler
{

    private static final int WIDTH = 550;

    private static final int HEIGHT = 400;

    private IMessageProvider messageProvider;

    private Label label;

    public PopupDialogBasedInfoHandler(IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;
        this.label = new Label();

        setBodyStyle("padding: 10px");
        setSize(WIDTH, HEIGHT);
        setScrollMode(Scroll.AUTOY);
        setButtons(Dialog.CLOSE);
        setHideOnButtonClick(true);
        setModal(true);
        setLayout(new FitLayout());
        add(label);
    }

    @Override
    public void displayInfo(String text)
    {
        display(InfoType.INFO, text);
    }

    @Override
    public void displayError(String text)
    {
        display(InfoType.ERROR, text);
    }

    @Override
    public void displayProgress(String text)
    {
        display(InfoType.PROGRESS, text);
    }

    public void display(InfoType type, String text)
    {
        setHeading(messageProvider.getMessage(type.getMessageKey()));
        label.setText(text);
        layout();
        show();
    }

}
