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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

public final class DataSetListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<ExternalData>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<ExternalData> data,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, data);
        this.viewContext = viewContext;
        this.callback = callback;
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().deleteDataSets(getDataSetCodes(data), reason.getValue(),
                callback);
    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.DATA_SET.getDescription();
    }

    private List<String> getDataSetCodes(List<ExternalData> dataSets)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (ExternalData externalData : dataSets)
        {
            dataSetCodes.add(externalData.getCode());
        }
        return dataSetCodes;
    }

}