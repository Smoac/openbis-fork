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
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * {@link ComboBox} which loads data, allows filtering and requires selection.
 * 
 * @author Izabela Adamczyk
 */
public abstract class DropDownList<M extends ModelData, E> extends ComboBox<M>
{
    private static final int DEFAULT_WIDTH = 150;

    private static final String PREFIX = "select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    final private String nothingFoundSuffix;

    private final String chooseSuffix;

    public DropDownList(final IViewContext<?> viewContext, String idSuffix, String labelDictCode,
            String displayField, String chooseSuffix, String nothingFoundSuffix)
    {
        this.viewContext = viewContext;
        this.chooseSuffix = chooseSuffix;
        this.nothingFoundSuffix = nothingFoundSuffix;
        setId(ID + idSuffix);
        setEnabled(true);
        setValidateOnBlur(true);
        setAllowBlank(false);
        setWidth(DEFAULT_WIDTH);
        setDisplayField(displayField);
        setFieldLabel(viewContext.getMessage(labelDictCode));
        setStore(new ListStore<M>());
    }

    abstract protected void loadData(AbstractAsyncCallback<List<E>> callback);

    abstract protected List<M> convertItems(final List<E> result);

    public E tryGetSelected()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    /**
     * @return true if anything has been selected. <br>
     *         Note that the result can be different from tryGetSelected() != null if there are null
     *         values in the model.
     */
    protected boolean isAnythingSelected()
    {
        return GWTUtils.tryGetSingleSelectedCode(this) != null;
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        loadData(new ListItemsCallback(viewContext));
    }

    public class ListItemsCallback extends AbstractAsyncCallback<List<E>>
    {

        protected ListItemsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(final List<E> result)
        {
            final ListStore<M> listStore = getStore();
            listStore.removeAll();
            listStore.add(convertItems(result));
            if (listStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, chooseSuffix));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, nothingFoundSuffix));
                setReadOnly(true);
            }
            applyEmptyText();
        }
    }

    private void markInvalidIfNotFromList()
    {
        if (valueNotInTheList())
        {
            forceInvalid(viewContext.getMessage(Dict.COMBO_BOX_EXPECTED_VALUE_FROM_THE_LIST));
        }
    }

    private boolean valueNotInTheList()
    {
        return getValue() == null && getRawValue() != null && getRawValue().equals("") == false
                && getRawValue().equals(getEmptyText()) == false;
    }

    @Override
    public boolean isValid()
    {
        clearInvalid();
        markInvalidIfNotFromList();
        return super.isValid() && valueNotInTheList() == false;
    }

    @Override
    public boolean validate()
    {
        clearInvalid();
        markInvalidIfNotFromList();
        return super.validate() && valueNotInTheList() == false;
    }
}