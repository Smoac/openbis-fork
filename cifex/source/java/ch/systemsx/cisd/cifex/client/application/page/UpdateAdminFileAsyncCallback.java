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

package ch.systemsx.cisd.cifex.client.application.page;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.AdminFileGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;

/**
 * An {@link AsyncCallback} that updates the list of files after a file has been updated.
 */

class UpdateAdminFileAsyncCallback extends AbstractAsyncCallback<Void>
{
    private final GridWidget<AbstractFileGridModel> modelBasedGrid;

    UpdateAdminFileAsyncCallback(final GridWidget<AbstractFileGridModel> modelBasedGrid,
            final ViewContext viewContext)
    {
        super(viewContext);
        this.modelBasedGrid = modelBasedGrid;
    }

    public final void onSuccess(final Void result)
    {
        final ViewContext context = getViewContext();
        context.getCifexService().listFiles(
                new AbstractAsyncCallback<List<OwnerFileInfoDTO>>(context)
                    {
                        public final void onSuccess(final List<OwnerFileInfoDTO> res)
                        {
                            modelBasedGrid.setDataAndRefresh(AdminFileGridModel.convert(res));
                        }
                    });
    }
}
