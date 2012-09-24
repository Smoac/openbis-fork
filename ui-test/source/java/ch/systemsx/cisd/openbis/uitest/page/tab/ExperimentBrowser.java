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

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Action;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WaitForRefreshOf;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.TreeGrid;

public class ExperimentBrowser implements Browser<Experiment>
{
    @Locate("openbis_select-project")
    private TreeGrid projectTree;

    @Locate("openbis_experiment-browser-grid-grid")
    private Grid grid;

    @Locate("openbis_experiment-browser_delete-button")
    private Button deleteAll;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox deletionDialog;

    public boolean selectProject(final Project project)
    {
        return new WaitForRefreshOf<Boolean>(grid).after(new Action<Boolean>()
            {
                @Override
                public Boolean execute()
                {
                    return projectTree.select(project.getCode());
                }

                @Override
                public boolean shouldWait(Boolean result)
                {
                    return result;
                }
            }).withTimeoutOf(10);
    }

    public void deleteAll()
    {
        deleteAll.click();
        deletionDialog.confirm("webdriver");
    }

    @Override
    public BrowserRow select(Experiment browsable)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BrowserCell cell(Experiment browsable, String column)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void filter(Experiment browsable)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetFilters()
    {
        // TODO Auto-generated method stub

    }

}
