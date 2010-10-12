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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GenericTableBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SerializableComparableIDDecorator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadataStaticColumns;

/**
 * Allows to create a table containing metadata of selected plate.
 * 
 * @author Izabela Adamczyk
 */
public class PlateMetadataBrowser extends GenericTableBrowserGrid
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "plate_metadata_browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    /**
     * Fetches information about the plate with the specified plate id and opens plate metadata
     * browser tab for that plate.
     */
    public static void openTab(final String platePermId,
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.SAMPLE,
                platePermId,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(screeningViewContext)
                    {
                        @Override
                        protected void process(IEntityInformationHolderWithPermId plate)
                        {
                            PlateMetadataBrowser.openTab(plate, screeningViewContext);
                        }
                    });
    }

    public static void openTab(final IEntityInformationHolderWithPermId plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        AbstractTabItemFactory factory = createPlateMetadataTabFactory(plate, viewContext);
        DispatcherHelper.dispatchNaviEvent(factory);
    }

    private static AbstractTabItemFactory createPlateMetadataTabFactory(
            final IEntityInformationHolderWithPermId plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.create(getTabTitle(),
                            PlateMetadataBrowser.create(viewContext, new TechId(plate.getId())),
                            viewContext);
                }

                @Override
                public String getId()
                {
                    return GenericConstants.ID_PREFIX + "plate-metadata-" + plate.getId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.VIEW);
                }

                @Override
                public String getTabTitle()
                {
                    return "Plate Report: " + plate.getCode();
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.createPlateMetadataBrowserLink(plate.getPermId());
                }
            };
    }

    private static IDisposableComponent create(
            final IViewContext<IScreeningClientServiceAsync> viewContext, TechId sampleId)
    {
        return new PlateMetadataBrowser(viewContext, sampleId).asDisposableWithoutToolbar();
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final TechId sampleId;

    private PlateMetadataBrowser(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId sampleId)
    {

        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, true,
                ScreeningDisplayTypeIDGenerator.PLATE_METADATA_GRID);
        this.screeningViewContext = viewContext;
        this.sampleId = sampleId;
        registerLinkClickListeners();

        allowMultipleSelection();
    }

    private void registerLinkClickListeners()
    {
        registerLinkClickListenerFor(PlateMetadataStaticColumns.WELL.getColumnId(),
                new ICellListener<TableModelRow>()
                    {
                        public void handle(TableModelRow rowItem, boolean keyPressed)
                        {
                            showEntityViewer(rowItem, false, keyPressed);
                        }
                    });
    }

    @Override
    protected void listTableRows(IResultSetConfig<String, TableModelRow> resultSetConfig,
            AsyncCallback<GenericTableResultSet> callback)
    {
        screeningViewContext.getService().listPlateMetadata(resultSetConfig, sampleId, callback);

    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<TableModelRow> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportPlateMetadata(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    private void showEntityInformationHolderViewer(final TableModelRow entity,
            final PlateMetadataStaticColumns column, final String typeCode,
            final EntityKind entityKind, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(
                createEntityInformationHolder(entity, column, typeCode, entityKind), editMode,
                active);
    }

    @Override
    protected void showEntityViewer(final TableModelRow entity, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(entity, PlateMetadataStaticColumns.WELL, "UNDEFINED",
                EntityKind.SAMPLE, editMode, active);
    }

    private static ISerializableComparable getColumn(final TableModelRow entity,
            PlateMetadataStaticColumns column)
    {
        return entity.getValues().get(column.ordinal());
    }

    private static String getColumnAsString(TableModelRow entity,
            PlateMetadataStaticColumns column)
    {
        return getColumn(entity, column).toString();
    }

    private static Long extractTechIdFromColumn(final TableModelRow entity,
            PlateMetadataStaticColumns column)
    {
        return ((SerializableComparableIDDecorator) getColumn(entity, column)).getID();
    }

    private static BasicEntityType createEntityType(String typeCode)
    {
        return new BasicEntityType(typeCode);
    }

    private static IEntityInformationHolderWithPermId createEntityInformationHolder(
            final TableModelRow entity, final PlateMetadataStaticColumns column,
            final String typeCode, final EntityKind entityKind)
    {
        return new IEntityInformationHolderWithPermId()
            {

                public String getCode()
                {
                    return getColumnAsString(entity, column);
                }

                public Long getId()
                {
                    return extractTechIdFromColumn(entity, column);
                }

                public BasicEntityType getEntityType()
                {
                    return createEntityType(typeCode);
                }

                public EntityKind getEntityKind()
                {
                    return entityKind;
                }

                public String getPermId()
                {
                    return null; // TODO 2010-10-11, Piotr Buczek: needed for permlink
                }
            };
    }
}