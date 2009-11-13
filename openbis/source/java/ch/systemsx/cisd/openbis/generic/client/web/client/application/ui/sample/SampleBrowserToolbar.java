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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid.ISampleCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * The toolbar of sample browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
final class SampleBrowserToolbar extends ToolBar implements ISampleCriteriaProvider
{
    public static final String ID = "sample-browser-toolbar";

    private static final String PREFIX = ID + "_";

    public static final String INCLUDE_GROUP_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-group-checkbox";

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final GroupSelectionWidget selectGroupCombo;

    private final IViewContext<?> viewContext;

    private final boolean excludeWithoutExperiment;

    public SampleBrowserToolbar(final IViewContext<?> viewContext, final boolean addShared,
            final boolean excludeWithoutExperiment)
    {
        this.viewContext = viewContext;
        this.excludeWithoutExperiment = excludeWithoutExperiment;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext, ID, true, true, false);
        selectGroupCombo = new GroupSelectionWidget(viewContext, ID, addShared);
        display();
    }

    public ListSampleDisplayCriteria tryGetCriteria()
    {
        final SampleType selectedType = tryGetSelectedSampleType();
        if (selectedType == null)
        {
            return null;
        }
        final Group selectedGroup = selectGroupCombo.tryGetSelectedGroup();
        if (selectedGroup == null)
        {
            return null;
        }
        final boolean includeInstance = GroupSelectionWidget.isSharedGroup(selectedGroup);
        final boolean includeGroup = includeInstance == false;

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(selectedType);
        criteria.setGroupCode(selectedGroup.getCode());
        criteria.setIncludeGroup(includeGroup);
        criteria.setIncludeInstance(includeInstance);
        criteria.setExcludeWithoutExperiment(excludeWithoutExperiment);
        return new ListSampleDisplayCriteria(criteria);
    }

    private SampleType tryGetSelectedSampleType()
    {
        return selectSampleTypeCombo.tryGetSelectedSampleType();
    }

    public List<PropertyType> tryGetPropertyTypes()
    {
        final SampleType selectedType = tryGetSelectedSampleType();
        if (selectedType == null)
        {
            return null;
        }
        return PropertyTypesFilterUtil.extractPropertyTypes(selectedType);
    }

    public void setCriteriaChangedListeners(final IDelegatedAction action)
    {
        selectGroupCombo.addSelectionChangedListener(new SelectionChangedListener<GroupModel>()
            {

                @Override
                public void selectionChanged(SelectionChangedEvent<GroupModel> se)
                {
                    action.execute();
                }
            });
        selectSampleTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                    {

                        @Override
                        public void selectionChanged(SelectionChangedEvent<SampleTypeModel> se)
                        {
                            action.execute();
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(selectSampleTypeCombo);
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.GROUP) + GenericConstants.LABEL_SEPARATOR));
        add(selectGroupCombo);
    }

    //
    // ToolBar
    //

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                    createOrDelete(ObjectKind.GROUP),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback entityTypeRefreshCallback)
    {
        if (containsAnyModificationsOf(observedModifications, ObjectKind.SAMPLE_TYPE)
                || containsAnyModificationsOf(observedModifications,
                        ObjectKind.PROPERTY_TYPE_ASSIGNMENT))
        {
            selectSampleTypeCombo.refreshStore(entityTypeRefreshCallback);
        } else
        {
            entityTypeRefreshCallback.postRefresh(true);
        }
        if (observedModifications.contains(createOrDelete(ObjectKind.GROUP)))
        {
            selectGroupCombo.refreshStore();
        }

    }

    private boolean containsAnyModificationsOf(Set<DatabaseModificationKind> observedModifications,
            ObjectKind objectKind)
    {
        return observedModifications.contains(createOrDelete(objectKind))
                || observedModifications.contains(edit(objectKind));
    }

    public void setEntityTypes(Set<SampleType> availableEntityTypes)
    {
        // TODO 2009-08-27, Tomasz Pylak: use this info to narrow properties when displaying "all"
        // types of samples. The method tryGetPropertyTypes would have to be rewritten and an
        // artificial SampleType for displaying all samples should not be used.
    }

}
