/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class PropertyTypeAssignmentBrowser implements Browser<PropertyTypeAssignment>
{
    @Locate("openbis_property-type-assignment-browser-grid")
    private Grid grid;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-assignment-browser-grid-edit")
    private Button edit;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-assignment-browser-grid-release")
    private Button release;

    @Locate("openbis_property-type-assignment-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_property-type-assignment-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    @Override
    public BrowserRow select(PropertyTypeAssignment assignment)
    {
        return grid.select("Property Type Code", assignment.getPropertyType().getCode());
    }

    @Override
    public BrowserCell cell(PropertyTypeAssignment assignment, String column)
    {
        return select(assignment).get(column);
    }

    @Override
    public void filter(PropertyTypeAssignment assignment)
    {
        paging.filters();
        filters.setCode(assignment.getPropertyType().getCode(), paging);
    }

    @Override
    public void resetFilters()
    {
        paging.filters();
        filters.reset();
    }

    @Override
    public String toString()
    {
        String s = "PropertyTypeAssignmentBrowser\n==========\n";
        return s + grid.toString();
    }
}
