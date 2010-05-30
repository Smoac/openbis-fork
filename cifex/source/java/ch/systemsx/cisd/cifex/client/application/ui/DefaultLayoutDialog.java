/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * {@link Dialog} adjusted to project settings.
 * 
 * @author Christian Ribeaud
 */
public class DefaultLayoutDialog extends Dialog
{
    public static final int DEFAULT_WIDTH = 500;

    public static final int DEFAULT_HEIGHT = 300;

    public DefaultLayoutDialog(final String title)
    {
        this(title, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public DefaultLayoutDialog(final String title, final int width, final int height)
    {
        this(title, width, height, false, true);
    }

    public DefaultLayoutDialog(final String title, final int width, final int height,
            final boolean modal, final boolean closable)
    {
        setHeading(title);
        setModal(modal);
        setWidth(width);
        setHeight(height);
        setClosable(closable);
        setScrollMode(Scroll.AUTO);
        getButtonBar().remove(getButtonById(Dialog.OK));
        if (closable)
        {
            addButton(createCloseButton());
        }
    }

    private final Button createCloseButton()
    {
        final Button button = new Button(msg(DIALOG_CLOSE_BUTTON_LABEL));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    hide();
                }
            });
        return button;
    }

}