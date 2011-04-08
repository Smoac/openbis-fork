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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.RANK_PREFIX;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar.PagingToolBarButtonKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;

/**
 * A grid showing feature vector summary for an experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class FeatureVectorSummaryGrid extends TypedTableGrid<MaterialFeatureVectorSummary>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "experiment-feature-vector-summary";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    private final IViewContext<IScreeningClientServiceAsync> specificViewContext;

    private final TechId experimentId;


    public static void openTab(final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(screeningViewContext)
                    {
                        @Override
                        protected void process(IEntityInformationHolderWithPermId experiment)
                        {
                            AbstractTabItemFactory factory =
                                    createTabFactory(screeningViewContext, experiment);
                            DispatcherHelper.dispatchNaviEvent(factory);
                        }
                    });
    }

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId experimentId)
    {
        return new FeatureVectorSummaryGrid(viewContext, experimentId).asDisposableWithoutToolbar();
    }

    private static AbstractTabItemFactory createTabFactory(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final IEntityInformationHolderWithPermId experiment)
    {
        return new AbstractTabItemFactory()
        {

            @Override
            public String getId()
            {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.FEATURE_VECTOR_SUMMARY_ACTION
                            + experiment.getCode();
            }

            @Override
            public ITabItem create()
            {
                IDisposableComponent tabComponent =
                        FeatureVectorSummaryGrid.create(viewContext, new TechId(experiment));
                return DefaultTabItem.create(getTabTitle(), tabComponent, viewContext);
            }

            @Override
            public String tryGetLink()
            {
                return ScreeningLinkExtractor.createFeatureVectorSummaryBrowserLink(experiment.getPermId());
            }

            @Override
            public String getTabTitle()
            {
                    return "Feature Vector Summary: " + experiment.getCode();
            }

            @Override
            public HelpPageIdentifier getHelpPageIdentifier()
            {
                return null;
            }

            };
    }

    FeatureVectorSummaryGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId experimentId)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                DisplayTypeIDGenerator.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION);
        this.specificViewContext = viewContext;
        this.experimentId = experimentId;

        // TODO KE: ask Franz-Josef/Tomek for a quick explanation on how export should be
        // implemented
        removeButtons(PagingToolBarButtonKind.EXPORT);
        setBorders(true);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<MaterialFeatureVectorSummary>> callback)
    {
        specificViewContext.getService().listExperimentFeatureVectorSummary(resultSetConfig,
                experimentId, callback);

    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO KE: implement export functionality once I know how it is supposed to work
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        if (columnID.startsWith(RANK_PREFIX))
        {
            return getRankDictionaryKey();
        } else
        {
            return columnID.toLowerCase();
        }
    }
    
    private String getRankDictionaryKey()
    {
        return Dict.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION.toLowerCase() + "_" + RANK_PREFIX;
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }
}
