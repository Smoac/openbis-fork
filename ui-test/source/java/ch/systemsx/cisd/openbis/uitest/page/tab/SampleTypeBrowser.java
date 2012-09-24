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
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class SampleTypeBrowser implements Browser<SampleType>
{
    @Locate("add-entity-type-SAMPLE")
    private Button add;

    @Locate("edit-entity-type-SAMPLE")
    private Button edit;

    @Locate("delete-entity-type-SAMPLE")
    private Button delete;

    @Locate("openbis_sample-type-browser-grid")
    private Grid grid;

    @Locate("openbis_sample-type-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_sample-type-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox confimDeletion;

    public void add()
    {
        add.click();
    }

    public void edit()
    {
        edit.click();
    }

    public void delete()
    {
        delete.click();
        confimDeletion.confirm();
    }

    @Override
    public BrowserRow select(SampleType type)
    {
        return grid.select("Code", type.getCode());
    }

    @Override
    public BrowserCell cell(SampleType type, String column)
    {
        return select(type).get(column);
    }

    @Override
    public void filter(SampleType type)
    {
        paging.filters();
        filters.setCode(type.getCode(), grid);
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
        String s = "SampleTypeBrowser\n==========\n";
        return s + grid.toString();
    }

}