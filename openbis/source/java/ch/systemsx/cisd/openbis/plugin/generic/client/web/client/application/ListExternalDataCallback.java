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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

public final class ListExternalDataCallback extends AbstractAsyncCallback<List<ExternalData>>
{
    private final AsyncCallback<BaseListLoadResult<ExternalDataModel>> delegate;

    public ListExternalDataCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
            final AsyncCallback<BaseListLoadResult<ExternalDataModel>> callback)
    {
        super(viewContext);
        this.delegate = callback;
    }

    //
    // AbstractAsyncCallback
    //

    @Override
    protected void finishOnFailure(final Throwable caught)
    {
        delegate.onFailure(caught);
    }

    @Override
    protected final void process(final List<ExternalData> result)
    {
        final List<ExternalDataModel> externalDataModels =
                ExternalDataModel.asExternalDataModels(result);
        final BaseListLoadResult<ExternalDataModel> baseListLoadResult =
                new BaseListLoadResult<ExternalDataModel>(externalDataModels);
        delegate.onSuccess(baseListLoadResult);
    }
}