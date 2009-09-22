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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * The toolbar of experiment browser with a project selected with an external tree.
 * 
 * @author Piotr Buczek
 */
class ExperimentBrowserToolbar extends ToolBar implements
        ICriteriaProvider<ListExperimentsCriteria>
{
    public static final String ID = "experiment-browser-toolbar";

    private final ExperimentTypeSelectionWidget selectExperimentTypeCombo;

    private final ProjectSelectionTreeWidget selectProjectTree;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            ProjectSelectionTreeWidget tree)
    {
        this.viewContext = viewContext;
        selectExperimentTypeCombo = new ExperimentTypeSelectionWidget(viewContext, ID, true);
        selectProjectTree = tree;
        display();
    }

    public void setCriteriaChangedListener(SelectionChangedListener<?> criteriaChangedListener)
    {
        selectExperimentTypeCombo.addSelectionChangedListener(criteriaChangedListener);
        selectProjectTree.setSelectionChangedListener(criteriaChangedListener);
    }

    protected void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.EXPERIMENT_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectExperimentTypeCombo));
    }

    public final ListExperimentsCriteria tryGetCriteria()
    {
        final ExperimentType selectedType =
                selectExperimentTypeCombo.tryGetSelectedExperimentType();
        if (selectedType == null)
        {
            return null;
        }
        final Project selectedProject = selectProjectTree.tryGetSelectedProject();
        if (selectedProject == null)
        {
            return null;
        }
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        criteria.setExperimentType(selectedType);
        criteria.setProjectCode(selectedProject.getCode());
        criteria.setGroupCode(selectedProject.getGroup().getCode());
        return criteria;
    }

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.EXPERIMENT_TYPE), createOrDelete(ObjectKind.PROJECT),
                    edit(ObjectKind.PROJECT), createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback entityTypeRefreshCallback)
    {
        if (observedModifications.contains(createOrDelete(ObjectKind.EXPERIMENT_TYPE))
                || observedModifications
                        .contains(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT))
                || observedModifications.contains(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)))
        {
            selectExperimentTypeCombo.refreshStore(entityTypeRefreshCallback);
        } else
        {
            entityTypeRefreshCallback.postRefresh(true);
        }
        if (observedModifications.contains(createOrDelete(ObjectKind.PROJECT))
                || observedModifications.contains(edit(ObjectKind.PROJECT)))
        {
            selectProjectTree.update(observedModifications);
        }
    }
}
