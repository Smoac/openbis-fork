/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * The toolbar of experiment browser with a project selected with an external tree.
 * 
 * @author Piotr Buczek
 */
class ExperimentBrowserToolbarWithTree extends AbstractExperimentBrowserToolbar
{
    public static final String ID = AbstractExperimentBrowserToolbar.ABSTRACT_ID;

    /** @see AbstractExperimentBrowserToolbar */
    public ExperimentBrowserToolbarWithTree(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            ProjectSelectionTreeWidget tree)
    {
        super(viewContext, createProjectSelectionWidgetWrapper(tree));
    }

    private static final ProjectSelectionWidgetWrapper createProjectSelectionWidgetWrapper(
            final ProjectSelectionTreeWidget widget)
    {
        return new ProjectSelectionWidgetWrapper()
            {

                public Widget getWidget()
                {
                    return widget;
                }

                public Project tryGetSelectedProject()
                {
                    return widget.tryGetSelectedProject();
                }

                public void addSelectionChangedListener(SelectionChangedListener<?> listener)
                {
                    widget.setSelectionChangedListener(listener);
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return widget.getRelevantModifications();
                }

                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    widget.update(observedModifications);
                }

            };
    }

}