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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Null;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Piotr Buczek
 */
public class ManagedPropertyGrid extends TypedTableGrid<Null>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "ManagedPropertyGrid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference,
            IManagedPropertyGridInformationProvider gridInformation,
            IDelegatedAction onRefreshAction)
    {
        final ManagedPropertyGrid grid =
                new ManagedPropertyGrid(viewContext, tableModelReference, gridInformation,
                        onRefreshAction);
        return grid.asDisposableWithoutToolbar();
    }

    public static String createId(String idSuffix)
    {
        return BROWSER_ID + "_" + idSuffix;
    }

    private final String resultSetKey;

    private final String gridKind;

    private final IDelegatedAction onRefreshAction;

    private ManagedPropertyGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference,
            IManagedPropertyGridInformationProvider gridInformation,
            IDelegatedAction onRefreshAction)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.DATA_SET_REPORTING_GRID);
        this.onRefreshAction = onRefreshAction;
        setId(BROWSER_ID);
        this.resultSetKey = tableModelReference.getResultSetKey();
        this.gridKind = gridInformation.getKey();
        updateDefaultRefreshButton();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(gridKind);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Null>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Null>> callback)
    {
        // In all cases the data should be taken from the cache, and we know the key already.
        // The custom columns should be recomputed.
        resultSetConfig.setCacheConfig(ResultSetFetchConfig
                .createFetchFromCacheAndRecompute(resultSetKey));
        viewContext.getService().listReport(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Null>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportReport(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Info.display("getRelevantModifications", "");
        return new DatabaseModificationKind[]
            {
                    // script changes can cause all sorts of changes to the grid
                    DatabaseModificationKind.edit(ObjectKind.SCRIPT),
                    // different script can be assigned
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)

            };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        Info.display("update", "");
        super.update(observedModifications);
    }

    @Override
    protected IBrowserGridActionInvoker asActionInvoker()
    {
        final IBrowserGridActionInvoker delegate = super.asActionInvoker();
        return new IBrowserGridActionInvoker()
            {

                public void toggleFilters(boolean show)
                {
                    delegate.toggleFilters(show);
                }

                public void refresh()
                {
                    onRefreshAction.execute();
                }

                public void export(boolean allColumns)
                {
                    delegate.export(allColumns);
                }

                public void configure()
                {
                    delegate.configure();
                }
            };
    }
}
