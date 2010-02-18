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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;

/**
 * Implementation of a table browser grid for {@link GenericTableRow} data.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class GenericTableBrowserGrid extends
        AbstractBrowserGrid<GenericTableRow, BaseEntityModel<GenericTableRow>>
{
    private static final int TIMSTAMP_COLUMN_WIDTH = 190;

    private static final String BROWSER_ID_PATTERN = "[a-z0-9_]*";

    private List<GenericTableColumnHeader> headers;

    /**
     * Creates an instane for specified view context, browserID, gridID, and display type ID
     * generator. The browser ID is also used to create dictionary keys for message providing in
     * according to the following schema:
     * 
     * <pre>
     *    &lt;browser ID&gt;_&lt;column code&gt;
     * </pre>
     * 
     * @param browserId Only lower-case letter, digits, and '_' are allowed as browser ID.
     */
    protected GenericTableBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, gridId, refreshAutomatically, displayTypeIDGenerator);
        if (browserId.matches(BROWSER_ID_PATTERN) == false)
        {
            throw new IllegalArgumentException("Invalid browser ID: " + browserId);
        }
        setId(browserId);
    }

    /**
     * Lists table rows. Implementations of this method usually call a server method.
     */
    protected abstract void listTableRows(
            IResultSetConfig<String, GenericTableRow> resultSetConfig,
            AsyncCallback<GenericTableResultSet> callback);

    @Override
    protected ColumnDefsAndConfigs<GenericTableRow> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<GenericTableRow> definitions =
                ColumnDefsAndConfigs.create(createColDefinitions());
        if (headers != null)
        {
            RealNumberRenderer realNumberRenderer =
                    new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                            .getRealNumberFormatingParameters());
            for (GenericTableColumnHeader header : headers)
            {
                if (header.getType() == DataTypeCode.REAL)
                {
                    definitions.setGridCellRendererFor(header.getCode(), realNumberRenderer);
                }
            }
        }
        return definitions;
    }

    @Override
    protected BaseEntityModel<GenericTableRow> createModel(GridRowModel<GenericTableRow> entity)
    {
        return new BaseEntityModel<GenericTableRow>(entity, createColDefinitions());
    }

    private List<IColumnDefinitionUI<GenericTableRow>> createColDefinitions()
    {
        List<IColumnDefinitionUI<GenericTableRow>> list =
                new ArrayList<IColumnDefinitionUI<GenericTableRow>>();
        if (headers != null)
        {
            for (final GenericTableColumnHeader header : headers)
            {
                String title = header.getTitle();
                if (title == null)
                {
                    title = viewContext.getMessage(getId() + "_" + header.getCode());
                }
                GenericTableRowColumnDefinitionUI columnDef;
                if (header.getType() == DataTypeCode.TIMESTAMP)
                {
                    columnDef =
                            new GenericTableRowColumnDefinitionUI(header, title,
                                    TIMSTAMP_COLUMN_WIDTH);
                } else
                {
                    columnDef = new GenericTableRowColumnDefinitionUI(header, title);
                }
                list.add(columnDef);
            }
        }
        return list;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, GenericTableRow> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<GenericTableRow>> callback)
    {
        AbstractAsyncCallback<GenericTableResultSet> extendedCallback =
                new AbstractAsyncCallback<GenericTableResultSet>(viewContext)
                    {
                        @Override
                        protected void process(GenericTableResultSet result)
                        {
                            headers = result.getHeaders();
                            callback.onSuccess(result.getResultSet());
                            refreshColumnsSettingsIfNecessary();
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            callback.finishOnFailure(caught);
                        }
                    };
        listTableRows(resultSetConfig, extendedCallback);
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    protected void refreshColumnsSettingsIfNecessary()
    {
        recreateColumnModelAndRefreshColumnsWithFilters();
    }

    @Override
    protected void refresh()
    {
        refresh(false);
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

    @Override
    protected void showEntityViewer(GenericTableRow entity, boolean editMode)
    {
    }

    @Override
    protected List<IColumnDefinition<GenericTableRow>> getInitialFilters()
    {
        return Collections.emptyList();
    }

}
