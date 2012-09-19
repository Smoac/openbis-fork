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

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.TreeGrid;

public class ExperimentBrowser extends BrowserPage
{
    @Locate("openbis_select-project")
    private TreeGrid projectTree;

    @Locate("openbis_experiment-browser_delete-button")
    private Button deleteAll;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox deletionDialog;

    public ExperimentBrowser space(String spaceCode)
    {
        projectTree.select(spaceCode);
        return this;
    }

    public void deleteAll()
    {
        deleteAll.click();
        deletionDialog.confirm("webdriver");
    }

    @Override
    protected WebElement getDeleteButton()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<WebElement> getColumns()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<WebElement> getData()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
