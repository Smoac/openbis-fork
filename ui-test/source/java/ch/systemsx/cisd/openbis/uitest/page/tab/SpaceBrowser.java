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
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WaitForRefreshOf;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class SpaceBrowser implements Browser<Space>
{

    @Locate("openbis_space-browser-grid")
    private Grid grid;

    @Locate("openbis_space-browser_add-button")
    private Button addSpace;

    @Locate("openbis_space-browser_delete-button")
    private Button delete;

    @Locate("openbis_space-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_space-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox confimDeletion;

    public void addSpace()
    {
        addSpace.click();
    }

    @Override
    public Row select(Space space)
    {
        return grid.select("Code", space.getCode());
    }

    public void delete()
    {
        delete.click();
        confimDeletion.confirm("WebDriver");
    }

    @Override
    public Cell cell(Space space, String column)
    {
        return select(space).get(column);
    }

    @Override
    public void filter(Space space)
    {
        paging.filters();
        filters.setCode(space.getCode());
        new WaitForRefreshOf(grid).withTimeoutOf(10);
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
        String s = "SpaceBrowser\n==========\n";
        return s + grid.toString();
    }
}
