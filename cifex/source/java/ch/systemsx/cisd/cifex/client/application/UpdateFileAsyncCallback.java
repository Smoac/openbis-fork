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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.shared.basic.dto.File;

/**
 * An {@link AsyncCallback} that updates the list of files after a file has been updated.
 */

class UpdateFileAsyncCallback extends AbstractAsyncCallback
{
    private final ModelBasedGrid modelBasedGrid;

    private final ViewContext viewContext;

    private final boolean adminView;

    UpdateFileAsyncCallback(final ModelBasedGrid modelBasedGrid, final ViewContext viewContext,
            final boolean adminView)
    {
        super(viewContext);
        this.adminView = adminView;
        this.viewContext = viewContext;
        this.modelBasedGrid = modelBasedGrid;
    }

    public final void onSuccess(final Object result)
    {
        final AbstractAsyncCallback callback = new AbstractAsyncCallback(viewContext)
            {
                public final void onSuccess(final Object res)
                {
                    modelBasedGrid.reloadStore((File[]) res);
                }
            };
        if (adminView)
        {
            viewContext.getCifexService().listFiles(callback);
        } else
        {
            viewContext.getCifexService().listUploadedFiles(callback);
        }
    }
}
