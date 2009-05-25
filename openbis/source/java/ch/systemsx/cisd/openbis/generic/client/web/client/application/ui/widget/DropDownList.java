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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Izabela Adamczyk
 */
abstract public class DropDownList<M extends ModelData, E> extends ComboBox<M> implements
        IDatabaseModificationObserver
{
    abstract protected void loadData(AbstractAsyncCallback<List<E>> callback);

    abstract protected List<M> convertItems(final List<E> result);

    // ----------

    private static final int DEFAULT_WIDTH = 150;

    private static final String PREFIX = "select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final String valueNotInListMsg;

    private final String chooseMsg;

    private final String emptyMsg;

    private final boolean mandatory;

    private final boolean reloadWhenRendering;

    private final IViewContext<?> viewContextOrNull;

    public DropDownList(final IViewContext<?> viewContext, String idSuffix, String labelDictCode,
            String displayField, String chooseSuffix, String nothingFoundSuffix)
    {
        this(idSuffix, displayField, viewContext.getMessage(labelDictCode), viewContext.getMessage(
                Dict.COMBO_BOX_CHOOSE, chooseSuffix), viewContext.getMessage(Dict.COMBO_BOX_EMPTY,
                nothingFoundSuffix), viewContext
                .getMessage(Dict.COMBO_BOX_EXPECTED_VALUE_FROM_THE_LIST), true, viewContext, true);
    }

    /** if viewContextOrNull is null the combobox is not able to refresh itself */
    public DropDownList(String idSuffix, String displayField, String label, String chooseMsg,
            String emptyMsg, String valueNotInListMsg, boolean mandatory,
            final IViewContext<?> viewContextOrNull, boolean reloadWhenRendering)
    {
        this.chooseMsg = chooseMsg;
        this.emptyMsg = emptyMsg;
        this.valueNotInListMsg = valueNotInListMsg;
        this.mandatory = mandatory;
        this.reloadWhenRendering = reloadWhenRendering;
        this.viewContextOrNull = viewContextOrNull;

        setId(ID + idSuffix);
        setEnabled(true);
        setValidateOnBlur(true);
        setAllowBlank(mandatory == false);
        setWidth(DEFAULT_WIDTH);
        setDisplayField(displayField);
        setFieldLabel(label);
        setStore(new ListStore<M>());
    }

    public DatabaseModificationAwareField<M> asDatabaseModificationAware()
    {
        return new DatabaseModificationAwareField<M>(this, this);
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshStore();
    }

    /**
     * Refreshes the whole store of the combobox. If the previously chosen value is no longer
     * present in the store, it will be changed to empty. Otherwise the previous selection will be
     * preserved.
     */
    public void refreshStore()
    {
        refreshStore(new ListItemsCallback(viewContextOrNull));
    }

    /**
     * Additionally executes a callback after the data refresh is done.
     * 
     * @see DropDownList#refreshStore()
     */
    public void refreshStore(final IDataRefreshCallback dataRefreshCallback)
    {
        if (viewContextOrNull == null)
        {
            return;
        }
        AbstractAsyncCallback<List<E>> callback = mergeWithStandardCallback(dataRefreshCallback);
        refreshStore(callback);
    }

    private AbstractAsyncCallback<List<E>> mergeWithStandardCallback(
            final IDataRefreshCallback dataRefreshCallback)
    {
        return new AbstractAsyncCallback<List<E>>(viewContextOrNull)
            {
                @Override
                protected void process(List<E> result)
                {
                    new ListItemsCallback(viewContextOrNull).process(result);
                    dataRefreshCallback.postRefresh(true);
                }
            };
    }

    private void refreshStore(AbstractAsyncCallback<List<E>> callback)
    {
        if (viewContextOrNull != null)
        {
            loadData(callback);
        }
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
            List<M> convertedItems = convertItems(result);
            updateStore(convertedItems);
        }
    }

    protected final void updateStore(final List<M> models)
    {
        final ListStore<M> termsStore = getStore();
        termsStore.removeAll();
        termsStore.add(models);

        int termsCount = termsStore.getCount();
        if (termsCount == 0)
        {
            setEmptyText(emptyMsg);
            setReadOnly(true);
        } else if (termsCount == 1)
        {
            setSelection(models);
        } else
        {
            setEmptyText(chooseMsg);
            setReadOnly(false);
            if (getValue() != null && getSelection().size() == 0)
            {
                validate(); // maybe the value became a valid selection
            }
            restoreSelection(getSelection());
        }
        applyEmptyText();
    }

    private void restoreSelection(List<M> previousSelection)
    {
        List<M> newSelection = cleanSelection(previousSelection, getStore());
        if (previousSelection.size() != newSelection.size())
        {
            setSelection(newSelection);
        }
    }

    // removes the no longer existing items from the selection
    private static <M extends ModelData> List<M> cleanSelection(List<M> previousSelection,
            Store<M> newStore)
    {
        List<M> newSelection = new ArrayList<M>();
        for (M prevItem : previousSelection)
        {
            if (containsModel(newStore, prevItem))
            {
                newSelection.add(prevItem);
            }
        }
        return newSelection;
    }

    private static <M extends ModelData> boolean containsModel(Store<M> store, M item)
    {
        for (M elem : store.getModels())
        {
            if (equalsModel(elem, item))
            {
                return true;
            }
        }
        return false;
    }

    private static <M extends ModelData> boolean equalsModel(M model1, M model2)
    {
        Collection<String> props1 = model1.getPropertyNames();
        Collection<String> props2 = model1.getPropertyNames();
        if (props1.equals(props2) == false)
        {
            return false;
        }
        for (String propName : props1)
        {
            Object val1 = model1.get(propName);
            Object val2 = model2.get(propName);
            if (val1 == null)
            {
                if (val2 != null)
                {
                    return false;
                } else
                {
                    continue;
                }
            } else
            {
                if (val1.equals(val2) == false)
                {
                    return false;
                }
            }
        }
        return true;
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
        return isEnabled() && getValue() == null && getRawValue() != null
                && getRawValue().equals("") == false
                && getRawValue().equals(getEmptyText()) == false && optionNoneSelected() == false;
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

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        if (reloadWhenRendering)
        {
            refreshStore();
        }
    }
}
