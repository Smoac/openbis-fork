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

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;

/**
 * Useful utility functions concerning widgets.
 * 
 * @author Franz-Josef Elmer
 */
public final class WidgetUtils
{
    /**
     * Shows the specified method.
     * 
     * @param messageResources Message resources used for getting message box title.
     */
    public final static void showMessage(final Message message,
            final IMessageResources messageResources)
    {
        final String type = message.getType();
        String title;
        if (Message.INFO.equals(type))
        {
            title = messageResources.getMessageBoxInfoTitle();
        } else if (Message.WARNING.equals(type))
        {
            title = messageResources.getMessageBoxWarningTitle();
        } else
        {
            title = messageResources.getMessageBoxErrorTitle();
        }
        MessageBox.alert(title, message.getMessageText(), null);
    }

    private WidgetUtils()
    {
    }

    public static <T extends ModelData, M extends T> void reloadStore(Grid<T> grid, List<M> models)
    {
        grid.el().mask();
        grid.getStore().removeAll();
        grid.getStore().add(models);
        grid.reconfigure(grid.getStore(), grid.getColumnModel());
        grid.el().unmask();
    }
}
