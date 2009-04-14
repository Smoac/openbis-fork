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
 */

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;

/**
 * @author Franz-Josef Elmer
 */
class ExperimentDataSetSection extends SectionPanel
{
    private final IDisposableComponent disposableBrowser;

    ExperimentDataSetSection(Experiment experiment, IViewContext<?> viewContext)
    {
        super("Data Sets");
        setLayout(new RowLayout());
        disposableBrowser = ExperimentDataSetBrowser.create(viewContext, experiment);
        add(disposableBrowser.getComponent(), new RowData(-1, 200));
    }

    public IDatabaseModificationObserver getDatabaseModificationObserver()
    {
        return disposableBrowser;
    }

    @Override
    protected void onDetach()
    {
        disposableBrowser.dispose();
        super.onDetach();
    }

}
