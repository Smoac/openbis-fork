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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * @author Izabela Adamczyk
 */
public class SimpleDropDownList<M extends ModelData, E> extends ComboBox<M>
{
    private static final int DEFAULT_WIDTH = 150;

    private static final String PREFIX = "select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final String valueNotInListMsg;

    private final String chooseMsg;

    private final String emptyMsg;

    private final boolean mandatory;

    public SimpleDropDownList(String idSuffix, String displayField, String label, String chooseMsg,
            String emptyMsg, String valueNotInListMsg, boolean mandatory)
    {
        this.chooseMsg = chooseMsg;
        this.emptyMsg = emptyMsg;
        this.valueNotInListMsg = valueNotInListMsg;
        this.mandatory = mandatory;
        setId(ID + idSuffix);
        setEnabled(true);
        setValidateOnBlur(true);
        setAllowBlank(mandatory == false);
        setWidth(DEFAULT_WIDTH);
        setDisplayField(displayField);
        setFieldLabel(label);
        setStore(new ListStore<M>());
    }

    public void updateStore(final List<M> models)
    {
        final ListStore<M> termsStore = getStore();
        termsStore.removeAll();
        termsStore.add(models);
        if (termsStore.getCount() > 0)
        {
            setEmptyText(chooseMsg);
            setReadOnly(false);
        } else
        {
            setEmptyText(emptyMsg);
            setReadOnly(true);
        }
        applyEmptyText();
    }

    @Override
    public String getRawValue()
    {
        if (mandatory && optionNoneSelected())
        {
            return "";
        }
        return super.getRawValue();
    }

    @Override
    public M getValue()
    {
        final M val = super.getValue();
        if (optionNoneSelected())
        {
            return null;
        }
        return val;
    }

    private boolean optionNoneSelected()
    {
        return super.getRawValue() != null && super.getRawValue().equals(GWTUtils.NONE_LIST_ITEM);
    }

    /**
     * Assumes that M contains field OBJECT of type E.
     */
    public E tryGetSelected()
    {
        if (optionNoneSelected())
        {
            return null;
        }
        return GWTUtils.tryGetSingleSelected(this);
    }

    /**
     * @return true if anything has been selected. <br>
     *         Note that the result can be different from tryGetSelected() != null if there are null
     *         values in the model.
     */
    protected boolean isAnythingSelected()
    {
        if (optionNoneSelected())
        {
            return false;
        }
        return GWTUtils.tryGetSingleSelectedCode(this) != null;
    }

    private void markInvalidIfNotFromList()
    {
        if (valueNotInTheList())
        {
            forceInvalid(valueNotInListMsg);
        }
    }

    private boolean valueNotInTheList()
    {
        return getValue() == null && getRawValue() != null && getRawValue().equals("") == false
                && getRawValue().equals(getEmptyText()) == false && optionNoneSelected() == false;
    }

    @Override
    public boolean isValid()
    {
        clearInvalid();
        markInvalidIfNotFromList();
        return super.isValid() && valueNotInTheList() == false
                ;
    }

    @Override
    public boolean validate()
    {
        clearInvalid();
        markInvalidIfNotFromList();
        return super.validate() && valueNotInTheList() == false
                ;
    }

}
