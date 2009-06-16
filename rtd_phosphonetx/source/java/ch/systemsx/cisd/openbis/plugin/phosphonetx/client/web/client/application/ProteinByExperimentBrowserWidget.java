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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowserWidget extends QueryWidget
{

    private IDisposableComponent disposableComponent;

    ProteinByExperimentBrowserWidget(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext);
        disposableComponent = ProteinByExperimentBrowserGrid.create(viewContext);
        add(disposableComponent.getComponent());
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return disposableComponent.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        disposableComponent.update(observedModifications);
    }

}
