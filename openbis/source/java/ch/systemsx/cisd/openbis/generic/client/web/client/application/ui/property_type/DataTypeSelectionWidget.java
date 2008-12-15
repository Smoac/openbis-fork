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

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataType;

/**
 * A {@link ComboBox} extension for selecting a {@link DataType}.
 * 
 * @author Christian Ribeaud
 */
public final class DataTypeSelectionWidget extends ComboBox<DataTypeModel>
{
    private static final String PREFIX = "data-type-select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public DataTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setEmptyText("- No data types found -");
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setEnabled(false);
        setWidth(100);
        setFieldLabel("Data type");
        setStore(new ListStore<DataTypeModel>());
    }

    /**
     * Returns the {@link DataType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final DataType tryGetSelectedDataType()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    private final void loadDataTypes()
    {
        viewContext.getService().listDataTypes(new ListDataTypesCallback(viewContext));
    }

    //
    // ComboBox
    //

    @Override
    protected final void afterRender()
    {
        super.afterRender();
        loadDataTypes();
    }

    //
    // Helper classes
    //

    public final class ListDataTypesCallback extends AbstractAsyncCallback<List<DataType>>
    {
        ListDataTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final List<DataType> result)
        {
            final ListStore<DataTypeModel> dataTypeStore = getStore();
            dataTypeStore.removeAll();
            dataTypeStore.add(DataTypeModel.convert(result));
            if (dataTypeStore.getCount() > 0)
            {
                setEnabled(true);
                setValue(dataTypeStore.getAt(0));
                setEmptyText("Choose data type...");
            }
        }
    }
}