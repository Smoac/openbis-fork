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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetSearchFieldComboModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchFieldKind;

/**
 * {@link ComboBox} containing list of search fields loaded from the server and extended by manually
 * configured ones.
 * 
 * @author Izabela Adamczyk
 */
public final class DataSetSearchFieldsSelectionWidget extends
        DropDownList<DataSetSearchFieldComboModel, PropertyType>
{
    private static final int WIDTH = 200;

    private static final String EMPTY_RESULT_SUFFIX = "search fields";

    private static final String CHOOSE_SUFFIX = "search field";

    static final String SUFFIX = "data-set-search";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private boolean dataLoaded;

    public DataSetSearchFieldsSelectionWidget(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PROPERTY_TYPE, ModelDataPropertyNames.CODE,
                CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        dataLoaded = false;
        setAllowBlank(true);
    }

    @Override
    /**
     * Always returns null.
     */
    public PropertyType tryGetSelected()
    {
        return null;
    }

    /**
     * Returns {@link DataSetSearchField} connected with selected option, or null - if nothing
     * selected.
     */
    public DataSetSearchField tryGetSelectedField()
    {
        return (DataSetSearchField) GWTUtils.tryGetSingleSelected(this);
    }

    /**
     * Returns code of the selected option, or null - if nothing selected.
     */
    public String tryGetSelectedCode()
    {
        return GWTUtils.tryGetSingleSelectedCode(this);
    }

    public DataSetSearchFieldsSelectionWidget(DataSetSearchFieldsSelectionWidget source,
            final String idSuffix)
    {
        this(source.viewContext, idSuffix);
        this.setEmptyText(source.getEmptyText());
        setWidth(WIDTH);
        setStore(source.getStore());
        dataLoaded = true;
    }

    public final class ListPropertyTypesCallback extends
            AbstractAsyncCallback<ResultSet<PropertyType>>
    {
        ListPropertyTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ResultSet<PropertyType> result)
        {
            final ListStore<DataSetSearchFieldComboModel> propertyTypeStore = getStore();
            propertyTypeStore.removeAll();
            propertyTypeStore.add(convertItems(result.getList()));
            if (propertyTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            applyEmptyText();
            dataLoaded = true;
        }
    }

    @Override
    protected List<DataSetSearchFieldComboModel> convertItems(List<PropertyType> types)
    {
        final List<DataSetSearchFieldComboModel> result =
                new ArrayList<DataSetSearchFieldComboModel>();
        for (DataSetSearchFieldKind field : DataSetSearchFieldKind.values())
        {
            if (field.equals(DataSetSearchFieldKind.EXPERIMENT_PROPERTY)
                    || field.equals(DataSetSearchFieldKind.SAMPLE_PROPERTY))
            {
                continue;
            }
            result.add(new DataSetSearchFieldComboModel(getDisplayName(field), DataSetSearchField
                    .createSimpleField(field)));
        }
        Collections.sort(types);
        for (final PropertyType st : types)
        {
            if (st.getExperimentTypePropertyTypes().size() > 0)
            {
                DataSetSearchField field =
                        DataSetSearchField.createExperimentProperty(st.getCode());
                DataSetSearchFieldComboModel comboModel =
                        createComboModel(st, field, isLabelDuplicated(st, types));
                result.add(comboModel);
            }
        }
        for (final PropertyType st : types)
        {
            if (st.getSampleTypePropertyTypes().size() > 0)
            {
                DataSetSearchField field = DataSetSearchField.createSampleProperty(st.getCode());
                DataSetSearchFieldComboModel comboModel =
                        createComboModel(st, field, isLabelDuplicated(st, types));
                result.add(comboModel);
            }
        }
        return result;
    }

    private static boolean isLabelDuplicated(PropertyType propertyType,
            List<PropertyType> propertyTypes)
    {
        for (PropertyType prop : propertyTypes)
        {
            // NOTE: equality by reference
            if (prop != propertyType && prop.getLabel().equals(propertyType.getLabel()))
            {
                return true;
            }
        }
        return false;
    }

    private DataSetSearchFieldComboModel createComboModel(final PropertyType propertyType,
            DataSetSearchField searchField, boolean useCode)
    {
        String prefix = getDisplayName(searchField.getKind());
        String property = useCode ? propertyType.getCode() : propertyType.getLabel();
        String code = prefix + " \'" + property + "\'";
        return new DataSetSearchFieldComboModel(code, searchField);
    }

    private static String getDisplayName(DataSetSearchFieldKind field)
    {
        return field.description();
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<PropertyType>> callback)
    {
        if (dataLoaded == false)
        {
            DefaultResultSetConfig<String, PropertyType> config =
                    DefaultResultSetConfig.createFetchAll();
            viewContext.getService().listPropertyTypes(config,
                    new ListPropertyTypesCallback(viewContext));
        }
    }
}