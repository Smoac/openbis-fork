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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.StaticColumns;

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
        PlateMetadataBrowser grid = new PlateMetadataBrowser(viewContext, sampleId);
        return grid.asDisposableWithoutToolbar();
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final TechId sampleId;

    public PlateMetadataBrowser(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId sampleId)
    {

        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, true, true,
                ScreeningDisplayTypeIDGenerator.PLATE_METADATA_GRID);
        screeningViewContext = viewContext;
        this.sampleId = sampleId;
        registerLinkClickListenerFor(StaticColumns.WELL.colId(),
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem)
                        {
                            showEntityViewer(rowItem, false);
                        }
                    });
        registerLinkClickListenerFor(StaticColumns.CONTENT.colId(),
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem)
                        {
                            showMaterialViewer(rowItem, false);
                        }
                    });
        registerLinkClickListenerFor(StaticColumns.INHIBITED_GENE.colId(),
                new ICellListener<GenericTableRow>()
                    {
                        public void handle(GenericTableRow rowItem)
                        {
                            showGeneViewer(rowItem, false);
                        }
                    });
        registerLinkClickListenerFor(StaticColumns.GENE_DETAILS.colId(),
                new ICellListener<GenericTableRow>()
                    {

                        public void handle(GenericTableRow rowItem)
                        {
                            ISerializableComparable gene =
                                    rowItem.tryToGetValue(StaticColumns.INHIBITED_GENE.ordinal());
                            String geneCode = gene.toString();
                            // NOTE: If we want to include the gene library url in
                            // exported data we must configure it outside the dictionary
                            // (PlateMetadataProvider).
                            WindowUtils.openWindow(screeningViewContext.getMessage(
                                    Dict.GENE_LIBRARY_URL, geneCode));
                        }
                    });

        allowMultipleSelection();
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

    @Override
    protected void showEntityViewer(final GenericTableRow entity, boolean editMode)
    {
        showEntityInformationHolderViewer(new IEntityInformationHolder()
            {

                public String getCode()
                {
                    return entity.tryToGetValue(StaticColumns.WELL.ordinal()).toString();
                }

                public Long getId()
                {
                    return ((SerializableComparableIDDecorator) entity
                            .tryToGetValue(StaticColumns.WELL.ordinal())).getID();
                }

                public BasicEntityType getEntityType()
                {
                    BasicEntityType type = new BasicEntityType();
                    type.setCode("UNDEFINED");
                    return type;
                }

                public EntityKind getEntityKind()
                {
                    return EntityKind.SAMPLE;
                }
            }, editMode);
    }

    protected void showMaterialViewer(final GenericTableRow entity, boolean editMode)
    {
        showEntityInformationHolderViewer(new IEntityInformationHolder()
            {

                public String getCode()
                {
                    return entity.tryToGetValue(StaticColumns.CONTENT.ordinal()).toString();
                }

                public Long getId()
                {
                    return ((SerializableComparableIDDecorator) entity
                            .tryToGetValue(StaticColumns.CONTENT.ordinal())).getID();
                }

                public BasicEntityType getEntityType()
                {
                    BasicEntityType type = new BasicEntityType();
                    type.setCode("UNDEFINED");
                    return type;
                }

                public EntityKind getEntityKind()
                {
                    return EntityKind.MATERIAL;
                }
            }, editMode);
    }

    protected void showGeneViewer(final GenericTableRow entity, boolean editMode)
    {
        showEntityInformationHolderViewer(new IEntityInformationHolder()
            {

                public String getCode()
                {
                    return entity.tryToGetValue(StaticColumns.INHIBITED_GENE.ordinal()).toString();
                }

                public Long getId()
                {
                    return ((SerializableComparableIDDecorator) entity
                            .tryToGetValue(StaticColumns.INHIBITED_GENE.ordinal())).getID();
                }

                public BasicEntityType getEntityType()
                {
                    BasicEntityType type = new BasicEntityType();
                    type.setCode("GENE");
                    return type;
                }

                public EntityKind getEntityKind()
                {
                    return EntityKind.MATERIAL;
                }
            }, editMode);
    }

}