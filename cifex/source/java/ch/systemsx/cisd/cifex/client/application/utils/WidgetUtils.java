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

package ch.systemsx.cisd.cifex.client.application.utils;

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.MESSAGE_BOX_ERROR_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.MESSAGE_BOX_INFO_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.MESSAGE_BOX_WARNING_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.msg;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.cifex.shared.basic.dto.Message;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message.Type;

/**
 * Useful utility functions concerning widgets.
 * 
 * @author Franz-Josef Elmer
 */
public final class WidgetUtils
{
    /**
     * Shows the specified method.
     */
    public final static void showMessage(final Message message)
    {
        showMessage(message, null);
    }

    /**
     * Shows the specified method.
     */
    public final static void showMessage(final Message message,
            final Listener<MessageBoxEvent> eventListenerOrNull)
    {
        final Type type = message.getType();
        String title;
        if (Message.Type.INFO.equals(type))
        {
            title = msg(MESSAGE_BOX_INFO_TITLE);
        } else if (Message.Type.WARNING.equals(type))
        {
            title = msg(MESSAGE_BOX_WARNING_TITLE);
        } else
        {
            title = msg(MESSAGE_BOX_ERROR_TITLE);
        }
        MessageBox.alert(title, message.getMessageText(), eventListenerOrNull);
    }

    private WidgetUtils()
    {
    }
}
