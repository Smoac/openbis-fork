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

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * An {@link AsyncCallback} that updates the list of files after a file has been updated.
 */

class UpdateUploadedFileAsyncCallback extends AbstractAsyncCallback<Void>
{
    private final GridWidget<AbstractFileGridModel> modelBasedGrid;

    private final ViewContext viewContext;

    UpdateUploadedFileAsyncCallback(final GridWidget<AbstractFileGridModel> modelBasedGrid,
            final ViewContext viewContext)
    {
        super(viewContext);
        this.viewContext = viewContext;
        this.modelBasedGrid = modelBasedGrid;
    }

    public final void onSuccess(final Void result)
    {

        viewContext.getCifexService().listOwnedFiles(
                new AbstractAsyncCallback<List<FileInfoDTO>>(viewContext)
                    {
                        public final void onSuccess(final List<FileInfoDTO> res)
                        {
                            modelBasedGrid.setDataAndRefresh(OwnedFileGridModel.convert(viewContext
                                    .getMessageResources(), res));
                        }
                    });
    }
}
