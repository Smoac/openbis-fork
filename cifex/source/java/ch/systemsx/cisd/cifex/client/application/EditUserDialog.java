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

import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.LayoutDialogConfig;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.ContentPanel;
import com.gwtext.client.widgets.layout.LayoutRegionConfig;

import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */

public class EditUserDialog extends LayoutDialog
{
    private static int HEIGTH = 150;

    private static int WIDTH = 800;

    /** The User to edit */
    private final User editUser;

    private ContentPanel editUserPanel;

    public EditUserDialog(ViewContext context, User user)
    {
        super(createLayoutDialogConfig(), createLayoutRegionConfig());
        assert user != null;
        this.editUser = user;

        editUserPanel = new ContentPanel();
        editUserPanel.setWidth("90%");
        final EditUserWidget editUserWidget =
                new EditUserWidget(context, context.getModel().getUser().isAdmin(), editUser);
        editUserPanel.add(editUserWidget);
        editUserPanel.setTitle(context.getMessageResources().getEditUserLabel());
        this.getLayout().add(LayoutRegionConfig.CENTER, editUserPanel);
        Button button = editUserWidget.getSubmitButton();
        button.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(final Button but, final EventObject e)
                {
                    destroy();
                }

            });
        editUserPanel.add(button);
    }

    private static LayoutDialogConfig createLayoutDialogConfig()
    {
        LayoutDialogConfig layoutDialogConfig = new LayoutDialogConfig();
        layoutDialogConfig.setTitle("Edit User");
        layoutDialogConfig.setModal(true);
        layoutDialogConfig.setProxyDrag(true);
        layoutDialogConfig.setWidth(WIDTH);
        layoutDialogConfig.setHeight(HEIGTH);
        return layoutDialogConfig;
    }

    private static final LayoutRegionConfig createLayoutRegionConfig()
    {
        LayoutRegionConfig layoutRegionConfig = new LayoutRegionConfig();
        layoutRegionConfig.setAutoScroll(true);
        return layoutRegionConfig;
    }
}
