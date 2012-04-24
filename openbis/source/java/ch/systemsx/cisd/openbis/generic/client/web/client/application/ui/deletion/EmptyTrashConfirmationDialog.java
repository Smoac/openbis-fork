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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;

public final class EmptyTrashConfirmationDialog extends
        AbstractDataConfirmationDialog<List<Deletion>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final DeletionForceOptions forceOptions;

    public EmptyTrashConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            AsyncCallback<Void> callback)
    {
        super(viewContext, Collections.<Deletion> emptyList(), viewContext
                .getMessage(Dict.EMPTY_TRASH_CONFIRMATION_TITLE));

        setStyleName("emptyTrashConfirmationDialog");
        this.viewContext = viewContext;
        this.callback = callback;
        this.forceOptions = new DeletionForceOptions(viewContext);
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().emptyTrash(
                forceOptions.getForceNotExistingLocationsValue(),
                forceOptions.getForceDisallowedTypesValue(),
                AsyncCallbackWithProgressBar.decorate(callback,
                        viewContext.getMessage(Dict.EMPTY_TRASH_PROGRESS)));
    }

    @Override
    protected String createMessage()
    {
        return viewContext.getMessage(Dict.EMPTY_TRASH_CONFIRMATION_MSG, data.size());
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);
        formPanel.add(forceOptions);
    }

}
