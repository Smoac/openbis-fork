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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * Displays a list of well content materials matching the search criteria and allows to go to their
 * details.
 * 
 * @author Tomasz Pylak
 */
public class MaterialDisambiguationGrid extends TypedTableGrid<Material>
{
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "material-disambiguation-grid";

    public static void openTab(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final WellSearchCriteria searchCriteria)
    {
        boolean refreshAutomatically = false;
        final MaterialDisambiguationGrid grid =
                new MaterialDisambiguationGrid(viewContext, searchCriteria, refreshAutomatically);
        final AbstractTabItemFactory disambiguationTabFactory =
                createDisambiguationTab(viewContext, searchCriteria, grid);

        grid.refresh(new IDataRefreshCallback()
            {
                private boolean firstCall = true;

                public void postRefresh(boolean wasSuccessful)
                {
                    if (firstCall == false)
                    {
                        return;
                    }
                    firstCall = false;
                    if (grid.getRowNumber() == 0)
                    {
                        AbstractTabItemFactory tabFactory =
                                createNoResultsTab(viewContext, searchCriteria);
                        DispatcherHelper.dispatchNaviEvent(tabFactory);
                    } else if (grid.getRowNumber() == 1)
                    {
                        List<Material> materials = grid.getContainedGridElements();
                        Material material = materials.get(0);
                        grid.openMaterialDetailViewer(material);
                    } else
                    {
                        DispatcherHelper.dispatchNaviEvent(disambiguationTabFactory);
                    }
                }
            }, false);
    }

    private static AbstractTabItemFactory createNoResultsTab(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            WellSearchCriteria searchCriteria)
    {
        boolean searchAllExperiments =
                searchCriteria.getExperimentCriteria().tryGetExperiment() == null;
        String msgDictKey =
                searchAllExperiments ? Dict.WELL_SEARCH_NO_RESULTS_IN_ANY_EXP_FOUND
                        : Dict.WELL_SEARCH_NO_RESULTS_IN_SELECTED_EXP_FOUND;
        Component component = new Text(viewContext.getMessage(msgDictKey));
        return createSimpleTab(viewContext, component, Dict.MATERIAL_DISAMBIGUATION_TITLE,
                "well-search-no-results");
    }

    private static AbstractTabItemFactory createSimpleTab(final IViewContext<?> viewContext,
            final Component component, final String titleDictKey, final String idSuffix)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.createUnaware(getTabTitle(), component, false,
                            viewContext);
                }

                @Override
                public String getId()
                {
                    return GenericConstants.ID_PREFIX + idSuffix;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(titleDictKey);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    private static AbstractTabItemFactory createDisambiguationTab(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final WellSearchCriteria searchCriteria, final MaterialDisambiguationGrid grid)
    {
        return new AbstractTabItemFactory()
            {
                private final String reportDate = DateTimeFormat.getMediumTimeFormat().format(
                        new Date());

                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.create(getTabTitle(), grid.asDisposableWithoutToolbar(),
                            viewContext);
                }

                @Override
                public String getId()
                {

                    return GenericConstants.ID_PREFIX + "-MaterialDisambiguationGrid-" + reportDate;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Material Disambiguation");
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.MATERIAL_DISAMBIGUATION_TITLE);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.tryCreateWellsSearchLink(searchCriteria, false);
                }

            };
    }

    // ----------------

    private final WellSearchCriteria searchCriteria;

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private MaterialDisambiguationGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            WellSearchCriteria searchCriteria, boolean refreshAutomatically)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, refreshAutomatically,
                ScreeningDisplayTypeIDGenerator.MATERIAL_DISAMBIGUATION_GRID);
        this.screeningViewContext = viewContext;
        this.searchCriteria = searchCriteria;

        setHeader(viewContext.getMessage(Dict.MATERIAL_DISAMBIGUATION_GRID_EXPLANATION));
        linkToMaterialDetails();
        setBorders(true);
    }

    private void linkToMaterialDetails()
    {
        ICellListenerAndLinkGenerator<Material> listenerLinkGenerator =
                new ICellListenerAndLinkGenerator<Material>()
                    {
                        public String tryGetLink(Material material, ISerializableComparable value)
                        {
                            return ScreeningLinkExtractor.createMaterialDetailsLink(material,
                                    searchCriteria.getExperimentCriteria());
                        }

                        public void handle(TableModelRowWithObject<Material> row,
                                boolean specialKeyPressed)
                        {
                            Material material = row.getObjectOrNull();
                            openMaterialDetailViewer(material);
                        }
                    };
        registerListenerAndLinkGenerator(MaterialGridColumnIDs.SHOW_DETAILS, listenerLinkGenerator);
        registerListenerAndLinkGenerator(MaterialGridColumnIDs.CODE, listenerLinkGenerator);
    }

    private void openMaterialDetailViewer(Material material)
    {
        ClientPluginFactory.openImagingMaterialViewer(material,
                searchCriteria.getExperimentCriteria(), screeningViewContext);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Material>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Material>> callback)
    {
        screeningViewContext.getService().listMaterials(resultSetConfig, searchCriteria, callback);

    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Material>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(MaterialGridColumnIDs.CODE);
    }

}
