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

import ch.systemsx.cisd.openbis.uitest.infra.Browser;
import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class PropertyTypeBrowser implements Browser<PropertyType>
{

    @Locate("openbis_property-type-browser-grid")
    private Grid grid;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-browser-grid-add-button")
    private Button add;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-browser-grid-delete-button")
    private Button delete;

    @Locate("openbis_property-type-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_property-type-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    @Override
    public Row select(PropertyType type)
    {
        return grid.select("Code", type.getCode());
    }

    @Override
    public Cell cell(PropertyType type, String column)
    {
        return select(type).get(column);
    }

    @Override
    public void filter(PropertyType propertyType)
    {
        paging.filters();
        filters.setCode(propertyType.getCode(), grid);
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
        String s = "PropertyTypeBrowser\n==========\n";
        return s + grid.toString();
    }
}
