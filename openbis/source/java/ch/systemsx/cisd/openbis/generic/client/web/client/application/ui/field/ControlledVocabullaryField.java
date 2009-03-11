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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * A {@link AdapterField} extension for selecting a vocabulary term from a list.
 * 
 * @author Christian Ribeaud
 */
public final class ControlledVocabullaryField extends AdapterField
{

    private final boolean mandatory;

    public ControlledVocabullaryField(final String labelField, final boolean mandatory,
            final List<VocabularyTerm> terms)
    {
        super(new VocabularyList(terms, mandatory));
        this.mandatory = mandatory;
        setFieldLabel(labelField);
        FieldUtil.setMandatoryFlag(this, mandatory);
        if (mandatory)
        {
            ((VocabularyList) getWidget()).setParent(this);
        }
    }

    public static class VocabularyList extends ListBox
    {
        private AdapterField parent;

        private final boolean validateOnBlurOrChange;

        public VocabularyList(final List<VocabularyTerm> terms, final boolean validateOnBlurOrChange)
        {
            this.validateOnBlurOrChange = validateOnBlurOrChange;
            addItem(GWTUtils.NONE_LIST_ITEM);
            for (final VocabularyTerm term : terms)
            {
                addItem(term.getCode());
            }
            if (validateOnBlurOrChange)
            {
                sinkEvents(Event.ONBLUR);
                sinkEvents(Event.ONCHANGE);
            }
        }

        void setParent(AdapterField parent)
        {
            this.parent = parent;
        }

        @Override
        public void onBrowserEvent(Event event)
        {
            if (event.getTypeInt() == Event.ONBLUR || event.getTypeInt() == Event.ONCHANGE)
            {
                if (parent != null && validateOnBlurOrChange)
                {
                    parent.validate();
                }
            } else
            {
                super.onBrowserEvent(event);
            }
        }
    }

    @Override
    protected boolean validateValue(String val)
    {
        if (mandatory && getValue() == null)
        {
            forceInvalid(GXT.MESSAGES.textField_blankText());
            return false;
        }
        clearInvalid();
        return true;
    }

    //
    // AdapterField
    //

    @Override
    public final Object getValue()
    {
        final String stringValue = super.getValue().toString();
        if (GWTUtils.NONE_LIST_ITEM.equals(stringValue))
        {
            return null;
        }
        return stringValue;
    }

}