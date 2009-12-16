/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.page;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;

/**
 * A pop-up dialog with a form for editing user settings.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class SettingsDialogController extends AbstractDialogController
{
    private static final int SETTINGS_DIALOG_WIDTH = 720;

    private final EditUserWidget editUserWidget;

    private final Dialog dialog;

    public SettingsDialogController(final ViewContext context)
    {
        super(context, context.getMessageResources().getEditUserLabel());

        // Otherwise the user can remove its own admin rights.
        final boolean allowPermanentUsers = false;
        editUserWidget =
                new EditUserWidget(context, allowPermanentUsers, context.getModel().getUser(),
                        false)
                    {
                        @Override
                        protected final void finishEditing()
                        {
                            // Now we can hide the dialog
                            dialog.hide();
                            
                            // Force a refresh of the entire UI
                            context.getPageController().refreshMainPage();
                        }
                    };

        dialog = new Dialog();
        initializeDialog();
        dialog.add(editUserWidget);
    }

    @Override
    public Dialog getDialog()
    {
        return dialog;
    }

    private void initializeDialog()
    {
        // Set the basic properties of the dialog
        dialog.setHeading(panelTitle);
        dialog.setModal(true);
        dialog.setWidth(SETTINGS_DIALOG_WIDTH);
        dialog.setHeight(DefaultLayoutDialog.DEFAULT_HEIGHT);
        dialog.setClosable(true);
        dialog.setScrollMode(Scroll.AUTO);
        dialog.setButtons(Dialog.OKCANCEL);

        // Add handlers for the OK and Cancel buttons

        Button okButton = dialog.getButtonById(Dialog.OK);
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    editUserWidget.submitForm();
                }
            });

        Button cancelButton = dialog.getButtonById(Dialog.CANCEL);
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    dialog.hide();
                }
            });
    }
}
