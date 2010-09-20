/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;

/**
 * A helper class for listing entities in the AbstractBrowserGrid.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class ListEntitiesCallback<T, M extends BaseEntityModel<T>> extends
        AbstractAsyncCallback<ResultSet<T>>
{
    private final AbstractBrowserGrid<T, M> browserGrid;

    private final AsyncCallback<PagingLoadResult<M>> delegate;

    // configuration with which the listing was called
    private final DefaultResultSetConfig<String, T> resultSetConfig;

    private final Grid<M> grid;

    private final PendingFetchManager pendingFetchManager;

    private final IDataRefreshCallback refreshCallback;

    private final BrowserGridPagingManager<T, M> pagingManager;

    private final CustomColumnsMetadataProvider customColumnsMetadataProvider;

    private final FilterToolbar<T> filterToolbar;

    private int logID;

    public ListEntitiesCallback(AbstractBrowserGrid<T, M> browserGrid,
            final IViewContext<?> viewContext, final AsyncCallback<PagingLoadResult<M>> delegate,
            final DefaultResultSetConfig<String, T> resultSetConfig, Grid<M> grid,
            PendingFetchManager pendingFetchManager, IDataRefreshCallback refreshCallback,
            BrowserGridPagingManager<T, M> pagingManager,
            CustomColumnsMetadataProvider customColumnsMetadataProvider,
            FilterToolbar<T> filterToolbar)
    {
        super(viewContext);
        this.browserGrid = browserGrid;
        this.delegate = delegate;
        this.resultSetConfig = resultSetConfig;
        this.grid = grid;
        this.pendingFetchManager = pendingFetchManager;
        this.refreshCallback = refreshCallback;
        this.pagingManager = pagingManager;
        this.customColumnsMetadataProvider = customColumnsMetadataProvider;
        this.filterToolbar = filterToolbar;
        logID = browserGrid.log("load data");
    }

    //
    // AbstractAsyncCallback
    //

    @Override
    public final void finishOnFailure(final Throwable caught)
    {
        grid.el().unmask();
        onComplete(false);
        pagingManager.finishOnFailure();
        // no need to show error message - it should be shown by DEFAULT_CALLBACK_LISTENER
        caught.printStackTrace();
        delegate.onFailure(caught);
    }

    @Override
    protected final void process(final ResultSet<T> result)
    {
        viewContext.logStop(logID);
        logID = browserGrid.log("process loaded data");
        // save the key of the result, later we can refer to the result in the cache using this
        // key
        browserGrid.saveCacheKey(result.getResultSetKey());
        GridRowModels<T> rowModels = result.getList();
        List<GridCustomColumnInfo> customColumnMetadata = rowModels.getCustomColumnsMetadata();
        customColumnsMetadataProvider.setCustomColumnsMetadata(customColumnMetadata);
        // convert the result to the model data for the grid control
        final List<M> models = browserGrid.createModels(rowModels);
        final PagingLoadResult<M> loadResult =
                new BasePagingLoadResult<M>(models, resultSetConfig.getOffset(), result
                        .getTotalLength());

        delegate.onSuccess(loadResult);
        pagingManager.process();

        filterToolbar.refreshColumnFiltersDistinctValues(rowModels.getColumnDistinctValues());
        onComplete(true);

        viewContext.logStop(logID);
    }

    // notify that the refresh is done
    private void onComplete(boolean wasSuccessful)
    {
        pendingFetchManager.popPendingFetch();
        refreshCallback.postRefresh(wasSuccessful);
    }

    @Override
    /* Note: we want to differentiate between callbacks in different subclasses of this grid. */
    public String getCallbackId()
    {
        return grid.getId();
    }
}