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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * {@link ComboBox} containing list of property type codes loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeSelectionWidget extends
        ComboBox<PropertyTypeSelectionWidget.PropertyTypeComboModel>
{
    private static class PropertyTypeComboModel extends BaseModelData
    {
        private static final long serialVersionUID = 1L;

        public PropertyTypeComboModel(PropertyType entity)
        {
            set(ModelDataPropertyNames.CODE, entity.getCode());
            set(ModelDataPropertyNames.OBJECT, entity);
        }
    }

    private static final String PREFIX = "property-type-select-";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public PropertyTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        this.viewContext = viewContext;
        setId(ID + idSuffix);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setWidth(180);
        setFieldLabel(viewContext.getMessage(Dict.PROPERTY_TYPE));
        setStore(new ListStore<PropertyTypeComboModel>());
    }

    public final String tryGetSelectedPropertyTypeCode()
    {
        final PropertyType propertyType = tryGetSelectedPropertyType();
        return propertyType == null ? null : propertyType.getCode();
    }

    /**
     * Returns the property type currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final PropertyType tryGetSelectedPropertyType()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        viewContext.getService().listPropertyTypes(new ListPropertyTypesCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListPropertyTypesCallback extends AbstractAsyncCallback<List<PropertyType>>
    {
        ListPropertyTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<PropertyType> result)
        {
            final ListStore<PropertyTypeComboModel> propertyTypeStore = getStore();
            propertyTypeStore.removeAll();
            propertyTypeStore.add(convert(result));
            if (propertyTypeStore.getCount() > 0)
            {
                setEnabled(true);
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "property type"));
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "property types"));
            }
            applyEmptyText();
        }
    }

    public final static List<PropertyTypeComboModel> convert(final List<PropertyType> types)
    {
        final List<PropertyTypeComboModel> result = new ArrayList<PropertyTypeComboModel>();
        for (final PropertyType st : types)
        {
            result.add(new PropertyTypeComboModel(st));
        }
        return result;
    }
}