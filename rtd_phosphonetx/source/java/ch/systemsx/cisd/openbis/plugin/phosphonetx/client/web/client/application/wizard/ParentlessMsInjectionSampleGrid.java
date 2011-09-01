/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.wizard;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar.PagingToolBarButtonKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.PhosphoNetXDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ParentlessMsInjectionSampleGridColumnIDs;

/**
 * @author Franz-Josef Elmer
 */
public class ParentlessMsInjectionSampleGrid extends TypedTableGrid<Sample>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "parentless_ms_injection_sample";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    public ParentlessMsInjectionSampleGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.PARENT_LESS_MS_INJECTION_SAMPLE_BROWSER_GRID);
        specificViewContext = viewContext;
        removeButtons(PagingToolBarButtonKind.CONFIG, PagingToolBarButtonKind.EXPORT,
                PagingToolBarButtonKind.FILTERS);
        allowMultipleSelection();
        showFiltersBar();
        setBorders(true);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        specificViewContext.getService()
                .listParentlessMsInjectionSamples(resultSetConfig, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ParentlessMsInjectionSampleGridColumnIDs.IDENTIFIER,
                ParentlessMsInjectionSampleGridColumnIDs.REGISTRATION_DATE);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }

}
