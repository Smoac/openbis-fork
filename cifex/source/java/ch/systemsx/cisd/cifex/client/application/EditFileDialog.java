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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.Date;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.EditFileWidget;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>AbstractLayoutDialog</code> to edit a file.
 * <p>
 * This dialog window is shown when the user edits an uploaded file.
 * </p>
 * 
 * @author Bernd Rinn
 */
public final class EditFileDialog extends DefaultLayoutDialog
{
    private static final int HEIGHT = 260;

    private final EditFileWidget editFileWidget;

    private final AsyncCallback<Date> refreshCallback;

    public EditFileDialog(final ViewContext context, final FileInfoDTO file,
            final AsyncCallback<Date> refreshCallback)
    {
        super(msg(EDIT_FILE_DIALOG_TITLE, file.getName()), EditFileWidget.WIDTH + 30, HEIGHT);
        this.editFileWidget = new EditFileWidget(context, file);
        this.refreshCallback = refreshCallback;
        add(editFileWidget);
        createUpdateButton();
    }

    private final void createUpdateButton()
    {
        final Button button = new Button("Update");
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    editFileWidget.submitForm(button, refreshCallback);
                    hide();
                }
            });
        addButton(button);
    }

}
