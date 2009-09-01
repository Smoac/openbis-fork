/*
 * Copyright 2009 ETH Zuerich, CISD
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
 * 
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.DetailedSearchWindow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.IDetailedSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Grid with detailed sample search results.
 * 
 * @author Piotr Buczek
 */
public class SampleSearchHitGrid extends SampleBrowserGrid implements IDetailedSearchHitGrid
{
    // browser consists of the grid and the paging toolbar
    public static final String SEARCH_BROWSER_ID =
            GenericConstants.ID_PREFIX + "sample-search-hit-browser";

    public static final String SEARCH_GRID_ID = SEARCH_BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ISampleCriteriaProvider criteriaProvider =
                new SampleCriteriaProvider(viewContext, ListSampleDisplayCriteria.createForSearch());
        SampleSearchHitGrid grid = new SampleSearchHitGrid(viewContext, criteriaProvider);
        final DetailedSearchWindow searchWindow =
                new DetailedSearchWindow(viewContext, EntityKind.SAMPLE);
        final DetailedSearchToolbar toolbar =
                new DetailedSearchToolbar(grid, viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY),
                        searchWindow);
        searchWindow.setUpdateListener(toolbar);
        return grid.asDisposableWithToolbar(toolbar);
    }

    private SampleSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ISampleCriteriaProvider criteriaProvider)
    {
        super(viewContext, criteriaProvider, SEARCH_GRID_ID, SEARCH_BROWSER_ID, false, false);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.SAMPLE_SEARCH_RESULT_GRID);
        updateCriteriaProviderAndRefresh();
        extendBottomToolbar();
    }

    public void refresh(DetailedSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        ListSampleDisplayCriteria criteriaOrNull = tryGetDisplayCriteria();
        assert criteriaOrNull != null;
        criteriaOrNull.updateSearchCriteria(newCriteria);
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (isAnySearchCriteriaSet())
        {
            super.refresh();
        }
    }

    private boolean isAnySearchCriteriaSet()
    {
        ListSampleDisplayCriteria criteriaOrNull = tryGetDisplayCriteria();
        return criteriaOrNull != null && (criteriaOrNull.getSearchCriteria().isEmpty() == false);
    }

    @Override
    protected String createHeader()
    {
        return null;
    }

    private ListSampleDisplayCriteria tryGetDisplayCriteria()
    {
        return getCriteriaProvider().tryGetCriteria();
    }

}