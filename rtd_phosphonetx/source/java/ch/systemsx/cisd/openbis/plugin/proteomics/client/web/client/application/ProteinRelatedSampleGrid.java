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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * A {@link LayoutContainer} which contains the grid where {@link SampleWithPropertiesAndAbundance}s of specified Protein are displayed.
 * 
 * @author Piotr Buczek
 */
public class ProteinRelatedSampleGrid extends TypedTableGrid<ProteinRelatedSample>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "protein_related_sample_grid";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "_edit-button";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "_show-details-button";

    private final IViewContext<IPhosphoNetXClientServiceAsync> phosphoViewContext;

    public static IDisposableComponent createGridForProteinSamples(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final TechId proteinReferenceID, Long experimentIDOrNull, final String gridId)
    {
        final ListSampleAbundanceByProteinCriteria criteria =
                new ListSampleAbundanceByProteinCriteria();
        criteria.setProteinReferenceID(proteinReferenceID);
        criteria.setExperimentID(experimentIDOrNull);
        final ProteinRelatedSampleGrid browserGrid =
                new ProteinRelatedSampleGrid(viewContext, criteria, gridId);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private ListSampleAbundanceByProteinCriteria criteria;

    private ProteinRelatedSampleGrid(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            ListSampleAbundanceByProteinCriteria criteria, String gridId)
    {
        super(viewContext.getCommonViewContext(), gridId, true,
                DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID);
        this.phosphoViewContext = viewContext;
        setId(BROWSER_ID);
        this.criteria = criteria;
        registerListenerAndLinkGenerator(ProteinRelatedSampleGridColumnIDs.SAMPLE_IDENTIFIER,
                new ICellListenerAndLinkGenerator<ProteinRelatedSample>()
                    {

                        @Override
                        public void handle(TableModelRowWithObject<ProteinRelatedSample> rowItem,
                                boolean specialKeyPressed)
                        {
                            showEntityInformationHolderViewer(rowItem.getObjectOrNull(), false,
                                    specialKeyPressed);
                        }

                        @Override
                        public String tryGetLink(ProteinRelatedSample entity,
                                ISerializableComparable comparableValue)
                        {
                            return LinkExtractor.tryExtract(entity);
                        }
                    });
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ProteinRelatedSample>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ProteinRelatedSample>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        phosphoViewContext.getService().listProteinRelatedSamplesByProtein(criteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ProteinRelatedSampleGridColumnIDs.SAMPLE_IDENTIFIER,
                ProteinRelatedSampleGridColumnIDs.ABUNDANCE);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ProteinRelatedSample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        phosphoViewContext.getService().prepareExportProteinRelatedSamples(exportCriteria, callback);
    }

}
