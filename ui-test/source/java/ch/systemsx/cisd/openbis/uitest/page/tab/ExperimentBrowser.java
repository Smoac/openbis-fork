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
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.Cell;
import ch.systemsx.cisd.openbis.uitest.page.common.Row;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.TreeGrid;

public class ExperimentBrowser implements Browser<Experiment>
{
    @Locate("openbis_select-project")
    private TreeGrid projectTree;

    @Locate("openbis_experiment-browser_delete-button")
    private Button deleteAll;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox deletionDialog;

    public void space(String spaceCode)
    {
        projectTree.select(spaceCode);
    }

    public void deleteAll()
    {
        deleteAll.click();
        deletionDialog.confirm("webdriver");
    }

    @Override
    public Row row(Experiment browsable)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cell cell(Experiment browsable, String column)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
