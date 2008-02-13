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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;

/**
 * An <code>AbstractMainPage</code> extension for administrators.
 * 
 * @author Franz-Josef Elmer
 */
final class EditCurrentUserPage extends AbstractMainPage
{

    private VerticalPanel editUserPanel;

    EditCurrentUserPage(ViewContext context)
    {
        super(context);
    }

    private final void createEditUserWidget()
    {
        // Otherwise the user can remove its own admin rights.
        boolean allowPermanentUsers = false;
        editUserPanel = createVerticalPanelPart();
        editUserPanel.add(createPartTitle(messageResources.getEditUserLabel()));
        EditUserWidget editUserWidget = new EditUserWidget(context, allowPermanentUsers, context.getModel().getUser());
        editUserPanel.add(editUserWidget);
        editUserPanel.add(editUserWidget.getSubmitButton());
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel mainPanel = new ContentPanel(Ext.generateId());

        createUserPanel = createVerticalPanelPart();
        createEditUserWidget();

        mainPanel.add(editUserPanel);
        return mainPanel;
    }

}
