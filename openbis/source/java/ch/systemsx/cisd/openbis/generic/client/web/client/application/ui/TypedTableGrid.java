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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig.ResultSetFetchMode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Abstract superclass of all grids based on {@link TypedTableModel}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class TypedTableGrid<T extends ISerializable>
        extends
        AbstractBrowserGrid<TableModelRowWithObject<T>, BaseEntityModel<TableModelRowWithObject<T>>>
{
    public static final String GRID_POSTFIX = "-grid";

    /**
     * Do not display more than this amount of columns in the report, web browsers have problem with
     * it
     */
    private static final int MAX_SHOWN_COLUMNS = 200;

    private final class CellListenerAndLinkGenerator implements ICellListenerAndLinkGenerator<T>
    {
        private final EntityKind entityKind;

        private final TableModelColumnHeader header;

        private CellListenerAndLinkGenerator(EntityKind entityKind, TableModelColumnHeader header)
        {
            this.entityKind = entityKind;
            this.header = header;
        }

        public String tryGetLink(T entity, final ISerializableComparable value)
        {
            if (value == null || value.toString().length() == 0)
            {
                return null;
            }
            if (value instanceof MaterialTableCell)
            {
                MaterialTableCell materialTableCell = (MaterialTableCell) value;
                return LinkExtractor.tryExtract(materialTableCell.getMaterialIdentifier());
            }
            return LinkExtractor.createPermlink(entityKind, value.toString());
        }

        public void handle(TableModelRowWithObject<T> rowItem, boolean specialKeyPressed)
        {
            ISerializableComparable cellValue = rowItem.getValues().get(header.getIndex());
            if (cellValue instanceof MaterialTableCell)
            {
                MaterialTableCell materialTableCell = (MaterialTableCell) cellValue;
                MaterialIdentifier materialIdentifier = materialTableCell.getMaterialIdentifier();
                OpenEntityDetailsTabHelper.open(viewContext, materialIdentifier, specialKeyPressed);
            } else
            {
                OpenEntityDetailsTabHelper.open(viewContext, entityKind, cellValue.toString(),
                        specialKeyPressed);
            }
        }
    }

    private final Map<String, ICellListenerAndLinkGenerator<T>> listenerLinkGenerators =
            new HashMap<String, ICellListenerAndLinkGenerator<T>>();

    private List<TableModelColumnHeader> headers;

    private List<IColumnDefinitionUI<TableModelRowWithObject<T>>> columnUIDefinitions;

    private String downloadURL;

    private Map<String, IColumnDefinition<TableModelRowWithObject<T>>> columnDefinitions;

    protected TypedTableGrid(IViewContext<ICommonClientServiceAsync> viewContext, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId + GRID_POSTFIX, refreshAutomatically, displayTypeIDGenerator);
        setId(browserId);
    }

    protected TypedTableGrid(IViewContext<ICommonClientServiceAsync> viewContext, String browserId,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId + GRID_POSTFIX, displayTypeIDGenerator);
        setId(browserId);
    }

    protected void setDownloadURL(String downloadURL)
    {
        this.downloadURL = downloadURL;
    }

    /**
     * Lists table rows. Implementations of this method usually call a server method.
     */
    protected abstract void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<T>> callback);

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<T>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<T>> definitions =
                ColumnDefsAndConfigs.create(createColDefinitions());
        Set<IColumnDefinition<TableModelRowWithObject<T>>> columnDefs = definitions.getColumnDefs();
        columnDefinitions = new HashMap<String, IColumnDefinition<TableModelRowWithObject<T>>>();
        for (IColumnDefinition<TableModelRowWithObject<T>> definition : columnDefs)
        {
            String identifier = definition.getIdentifier();
            columnDefinitions.put(identifier, definition);
        }
        if (headers != null)
        {
            RealNumberRenderer realNumberRenderer =
                    new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                            .getRealNumberFormatingParameters());
            for (TableModelColumnHeader header : headers)
            {
                String id = header.getId();
                if (listenerLinkGenerators.containsKey(id))
                {
                    definitions.setGridCellRendererFor(id, createInternalLinkCellRenderer());
                }
                if (header.getDataType() == DataTypeCode.REAL)
                {
                    definitions.setGridCellRendererFor(id, realNumberRenderer);
                }
            }
        }
        return definitions;
    }

    @Override
    protected BaseEntityModel<TableModelRowWithObject<T>> createModel(
            GridRowModel<TableModelRowWithObject<T>> entity)
    {
        return new BaseEntityModel<TableModelRowWithObject<T>>(entity, createColDefinitions());
    }

    /**
     * Registers for the specified column a cell listener and link generator. This method should be
     * called in the constructor.
     */
    protected void registerListenerAndLinkGenerator(String columnID,
            final ICellListenerAndLinkGenerator<T> listenerLinkGenerator)
    {
        listenerLinkGenerators.put(columnID, listenerLinkGenerator);
        registerLinkClickListenerFor(columnID, listenerLinkGenerator);
    }

    private List<IColumnDefinitionUI<TableModelRowWithObject<T>>> createColDefinitions()
    {
        if (columnUIDefinitions == null)
        {
            List<IColumnDefinitionUI<TableModelRowWithObject<T>>> list =
                    new ArrayList<IColumnDefinitionUI<TableModelRowWithObject<T>>>();
            if (headers != null)
            {
                String sessionID = viewContext.getModel().getSessionContext().getSessionID();
                for (final TableModelColumnHeader header : headers)
                {
                    String title = header.getTitle();
                    if (title == null)
                    {
                        title =
                                viewContext.getMessage(translateColumnIdToDictionaryKey(header
                                        .getId()));
                    }
                    // support for links in queries
                    ICellListenerAndLinkGenerator<T> linkGeneratorOrNull =
                            listenerLinkGenerators.get(header.getId());
                    final EntityKind entityKind = header.tryGetEntityKind();
                    if (linkGeneratorOrNull == null && entityKind != null)
                    {
                        linkGeneratorOrNull = new CellListenerAndLinkGenerator(entityKind, header);
                        registerListenerAndLinkGenerator(header.getId(), linkGeneratorOrNull);
                    }
                    //
                    TypedTableGridColumnDefinitionUI<T> definition =
                            new TypedTableGridColumnDefinitionUI<T>(header, title, downloadURL,
                                    sessionID, linkGeneratorOrNull);
                    definition.setHidden(list.size() > MAX_SHOWN_COLUMNS);
                    list.add(definition);
                }
            }
            columnUIDefinitions = list;
        }
        return columnUIDefinitions;
    }

    /**
     * Translates a column ID to a key used to get title of the column from a dictionary. This
     * method can be overridden by subclasses.
     * 
     * @return <code>getId() + "_" + columnID</code>
     */
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return getId() + "_" + columnID;
    }

    @Override
    protected void listEntities(
            final DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<TableModelRowWithObject<T>>> callback)
    {
        AbstractAsyncCallback<TypedTableResultSet<T>> extendedCallback =
                new AbstractAsyncCallback<TypedTableResultSet<T>>(viewContext)
                    {
                        @Override
                        protected void process(TypedTableResultSet<T> result)
                        {
                            // don't need to recreate columns when paging or filtering
                            if (resultSetConfig.getCacheConfig().getMode() != ResultSetFetchMode.FETCH_FROM_CACHE)
                            {
                                headers = result.getResultSet().getList().getColumnHeaders();
                                columnUIDefinitions = null;
                                recreateColumnModelAndRefreshColumnsWithFilters();
                            }
                            callback.onSuccess(result.getResultSet());
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

    @Override
    protected void refresh()
    {
        refresh(false);
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<T> entity, boolean editMode,
            boolean inBackground)
    {
    }

    @Override
    protected List<IColumnDefinition<TableModelRowWithObject<T>>> getInitialFilters()
    {

        List<IColumnDefinition<TableModelRowWithObject<T>>> definitions =
                new ArrayList<IColumnDefinition<TableModelRowWithObject<T>>>();
        List<String> ids = getColumnIdsOfFilters();
        for (String id : ids)
        {
            IColumnDefinition<TableModelRowWithObject<T>> definition = columnDefinitions.get(id);
            if (definition != null)
            {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    protected List<String> getColumnIdsOfFilters()
    {
        return Collections.<String> emptyList();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }
}
