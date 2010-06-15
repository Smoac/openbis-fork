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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GenericTableBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SerializableComparableIDDecorator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadataStaticColumns;

/**
 * Allows to create a table containing metadata of selected plate.
 * 
 * @author Izabela Adamczyk
 */
class PlateMetadataBrowser extends GenericTableBrowserGrid
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "plate_metadata_browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<IScreeningClientServiceAsync> viewContext, TechId sampleId)
    {
        return new PlateMetadataBrowser(viewContext, sampleId).asDisposableWithoutToolbar();
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final TechId sampleId;

    public PlateMetadataBrowser(IViewContext<IScreeningClientServiceAsync> viewContext,
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
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem, boolean keyPressed)
                        {
                            showEntityViewer(rowItem, false, keyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMetadataStaticColumns.CONTENT.getColumnId(),
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem, boolean keyPressed)
                        {
                            showMaterialViewer(rowItem, false, keyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMetadataStaticColumns.INHIBITED_GENE.getColumnId(),
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem, boolean keyPressed)
                        {
                            showGeneViewer(rowItem, false, keyPressed);
                        }
                    });
        registerLinkClickListenerFor(PlateMetadataStaticColumns.GENE_DETAILS.getColumnId(),
                new ICellListener<GenericTableRow>()
                    {

                        public void handle(GenericTableRow rowItem, boolean keyPressed)
                        {
                            String geneCode =
                                    getColumnAsString(rowItem,
                                            PlateMetadataStaticColumns.INHIBITED_GENE);
                            // NOTE: If we want to include the gene library url in
                            // exported data we must configure it outside the dictionary
                            // (PlateMetadataProvider).
                            WindowUtils.openWindow(screeningViewContext.getMessage(
                                    Dict.GENE_LIBRARY_URL, geneCode));
                        }
                    });
    }

    @Override
    protected void listTableRows(IResultSetConfig<String, GenericTableRow> resultSetConfig,
            AsyncCallback<GenericTableResultSet> callback)
    {
        screeningViewContext.getService().listPlateMetadata(resultSetConfig, sampleId, callback);

    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<GenericTableRow> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportPlateMetadata(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    private void showEntityInformationHolderViewer(final GenericTableRow entity,
            final PlateMetadataStaticColumns column, final String typeCode,
            final EntityKind entityKind, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(createEntityInformationHolder(entity, column, typeCode,
                entityKind), editMode, active);
    }

    @Override
    protected void showEntityViewer(final GenericTableRow entity, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(entity, PlateMetadataStaticColumns.WELL, "UNDEFINED",
                EntityKind.SAMPLE, editMode, active);
    }

    protected void showMaterialViewer(final GenericTableRow entity, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(entity, PlateMetadataStaticColumns.CONTENT, "UNDEFINED",
                EntityKind.MATERIAL, editMode, active);
    }

    protected void showGeneViewer(final GenericTableRow entity, boolean editMode, boolean active)
    {
        showEntityInformationHolderViewer(entity, PlateMetadataStaticColumns.INHIBITED_GENE,
                "GENE", EntityKind.MATERIAL, editMode, active);
    }

    private static ISerializableComparable getColumn(final GenericTableRow entity,
            PlateMetadataStaticColumns column)
    {
        return entity.tryToGetValue(column.ordinal());
    }

    private static String getColumnAsString(GenericTableRow entity,
            PlateMetadataStaticColumns column)
    {
        return getColumn(entity, column).toString();
    }

    private static Long extractTechIdFromColumn(final GenericTableRow entity,
            PlateMetadataStaticColumns column)
    {
        return ((SerializableComparableIDDecorator) getColumn(entity, column)).getID();
    }

    private static BasicEntityType createEntityType(String typeCode)
    {
        BasicEntityType type = new BasicEntityType();
        type.setCode(typeCode);
        return type;
    }

    private static IEntityInformationHolder createEntityInformationHolder(
            final GenericTableRow entity, final PlateMetadataStaticColumns column,
            final String typeCode, final EntityKind entityKind)
    {
        return new IEntityInformationHolder()
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
            };
    }
}