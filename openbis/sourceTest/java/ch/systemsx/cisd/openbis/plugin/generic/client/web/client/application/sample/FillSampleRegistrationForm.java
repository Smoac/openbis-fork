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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample.
 * 
 * @author Christian Ribeaud
 */
public final class FillSampleRegistrationForm extends AbstractDefaultTestCommand
{
    private final String code;

    private final String groupNameOrNull;

    private final List<PropertyField> properties;

    private String parent;

    private String container;

    public FillSampleRegistrationForm(final String groupNameOrNull, final String code,
            boolean withVocabulary)
    {
        this.groupNameOrNull = groupNameOrNull;
        this.code = code;
        this.properties = new ArrayList<PropertyField>();
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
        if (withVocabulary)
        {
            addCallbackClass(VocabularyTermSelectionWidget.ListTermsCallback.class);
        }
    }

    public final FillSampleRegistrationForm parent(final String parentFieldValue)
    {
        this.parent = parentFieldValue;
        return this;
    }

    public final FillSampleRegistrationForm container(final String containerFieldValue)
    {
        this.container = containerFieldValue;
        return this;
    }

    public final FillSampleRegistrationForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        GWTTestUtil.setTextFieldValue(GenericSampleRegistrationForm.ID
                + AbstractGenericEntityRegistrationForm.ID_SUFFIX_CODE, code);

        final GroupSelectionWidget groupSelector =
                (GroupSelectionWidget) GWTTestUtil.getWidgetWithID(GroupSelectionWidget.ID
                        + GroupSelectionWidget.SUFFIX + GenericSampleRegistrationForm.ID);
        GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);

        if (StringUtils.isBlank(parent) == false)
        {
            final SampleChooserField parentField =
                    (SampleChooserField) GWTTestUtil
                            .getWidgetWithID(GenericSampleRegistrationForm.ID
                                    + GenericSampleRegistrationForm.ID_SUFFIX_PARENT);
            parentField.setValue(parent);
        }
        if (StringUtils.isBlank(container) == false)
        {
            final SampleChooserField containerField =
                (SampleChooserField) GWTTestUtil
                        .getWidgetWithID(GenericSampleRegistrationForm.ID
                    + GenericSampleRegistrationForm.ID_SUFFIX_CONTAINER);
            containerField.setValue(container);
        }
        for (final PropertyField property : properties)
        {
            final Widget widget = GWTTestUtil.getWidgetWithID(property.getPropertyFieldId());
            if (widget instanceof Field<?>)
            {
                ((Field<?>) widget).setRawValue(property.getPropertyFieldValue());
            } else
            {
                throw new IllegalStateException("Wrong widget type");
            }
        }
        GWTTestUtil.clickButtonWithID(GenericSampleRegistrationForm.ID
                + AbstractRegistrationForm.SAVE_BUTTON);
    }
}
