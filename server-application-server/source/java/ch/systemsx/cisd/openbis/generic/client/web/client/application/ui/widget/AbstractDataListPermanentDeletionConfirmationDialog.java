/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;

/**
 * {@link AbstractDataListDeletionConfirmationDialog} abstract implementation for a confirmation dialog shown before permanent deletion of list of
 * data.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataListPermanentDeletionConfirmationDialog<T> extends
        AbstractDataListDeletionConfirmationDialog<T>
{
    public AbstractDataListPermanentDeletionConfirmationDialog(IViewContext<?> viewContext,
            List<T> data, AsyncCallback<Void> deletionCallback)
    {
        super(viewContext, data, deletionCallback);
    }

    @Override
    protected DeletionType getDeletionType()
    {
        return DeletionType.PERMANENT;
    }

    @Override
    String getOperationName()
    {
        return messageProvider.getMessage(Dict.DELETING_PERMANENTLY);
    }

    @Override
    String getProgressMessage()
    {
        return messageProvider
                .getMessage(Dict.DELETE_PERMANENTLY_PROGRESS_MESSAGE, getEntityName());
    }

}
