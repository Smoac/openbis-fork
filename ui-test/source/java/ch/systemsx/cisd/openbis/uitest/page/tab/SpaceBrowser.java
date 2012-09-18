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

import java.util.List;

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.page.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;

public class SpaceBrowser extends BrowserPage
{

    @Locate("openbis_space-browser-grid")
    private Grid grid;

    @Locate("openbis_space-browser_add-button")
    private Button addSpace;

    @Locate("openbis_space-browser_delete-button")
    private Button delete;

    public AddSpaceDialog addSpace()
    {
        addSpace.click();
        return get(AddSpaceDialog.class);
    }

    @Override
    protected List<WebElement> getColumns()
    {
        return this.grid.getColumns();
    }

    @Override
    protected List<WebElement> getData()
    {
        return this.grid.getCells();
    }

    @Override
    protected WebElement getDeleteButton()
    {
        return this.delete.getContext();
    }
}
