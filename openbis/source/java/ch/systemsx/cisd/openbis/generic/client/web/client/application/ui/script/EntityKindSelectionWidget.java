/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Izabela Adamczyk
 */
class EntityKindSelectionWidget extends SimpleComboBox<String>
{
    public EntityKindSelectionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKindOrNull, boolean enabled, boolean withAll)
    {
        if (withAll)
        {
            add(GenericConstants.ALL_ENTITY_KINDS);
        }
        if (entityKindOrNull != null)
        {
            add(entityKindOrNull.name());
        } else
        {
            for (EntityKind val : EntityKind.values())
            {
                add(val.name());
            }
        }
        setFieldLabel(viewContext.getMessage(Dict.ENTITY_KIND));
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setEditable(false);
        setAllowBlank(false);
        FieldUtil.markAsMandatory(this);
        setEnabled(enabled);
    }

    public boolean isAllEntityKindsSelected()
    {
        return (StringUtils.isBlank(getSimpleValue()) == false)
                && getSimpleValue().equals(GenericConstants.ALL_ENTITY_KINDS);
    }

    public EntityKind tryGetEntityKind()
    {
        String simpleValue = getSimpleValue();
        if (StringUtils.isBlank(simpleValue) || isAllEntityKindsSelected())
        {
            return null;
        } else
        {
            return EntityKind.valueOf(simpleValue);
        }
    }
}