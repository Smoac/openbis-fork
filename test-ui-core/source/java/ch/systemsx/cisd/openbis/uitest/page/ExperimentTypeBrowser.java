/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.page;

import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DeletionConfirmationBox;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.SettingsDialog;

public class ExperimentTypeBrowser extends Browser
{

    @Locate("add-entity-type-EXPERIMENT")
    private Button add;

    @SuppressWarnings("unused")
    @Locate("edit-entity-type-EXPERIMENT")
    private Button edit;

    @Locate("delete-entity-type-EXPERIMENT")
    private Button delete;

    @Locate("openbis_experiment-type-browser-grid")
    private Grid grid;

    @Locate("openbis_experiment-type-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_experiment-type-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    @Lazy
    @Locate("deletion-confirmation-dialog")
    private DeletionConfirmationBox confimDeletion;

    @Lazy
    @Locate("openbis_tab-paneltype-browser-grid-EXPERIMENT")
    private SettingsDialog settings;

    public void add()
    {
        add.click();
    }

    @Override
    public void delete()
    {
        delete.click();
        confimDeletion.confirm();
    }

    @Override
    public Grid getGrid()
    {
        return grid;
    }

    @Override
    public PagingToolBar getPaging()
    {
        return paging;
    }

    @Override
    public FilterToolBar getFilters()
    {
        return filters;
    }

    @Override
    public SettingsDialog getSettings()
    {
        return settings;
    }
}