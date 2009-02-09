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

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * The toolbar of sample browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
final class SampleBrowserToolbar extends ToolBar
{
    public static final String ID = "sample-browser-toolbar";

    private static final String PREFIX = ID + "_";

    public static final String INCLUDE_GROUP_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-group-checkbox";

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final GroupSelectionWidget selectGroupCombo;

    private final IViewContext<?> viewContext;

    public SampleBrowserToolbar(final IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext, ID, true);
        selectGroupCombo = new GroupSelectionWidget(viewContext, ID, true);
        display();
    }

    public ListSampleCriteria tryGetCriteria()
    {
        final SampleType selectedType = selectSampleTypeCombo.tryGetSelectedSampleType();
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
        return criteria;
    }

    public void setCriteriaChangedListener(SelectionChangedListener<?> criteriaChangedListener)
    {
        selectGroupCombo.addSelectionChangedListener(criteriaChangedListener);
        selectSampleTypeCombo.addSelectionChangedListener(criteriaChangedListener);
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectSampleTypeCombo));
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.GROUP) + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectGroupCombo));
    }

    //
    // ToolBar
    //

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

}