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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of search fields loaded from the server (property types) and
 * static ones.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2009-02-13, Tomasz Pylak: fetching of property types is done every time a new widget is
// created, although all of them are identical. It should be done outside of this class and passed
// to it.
public final class DataSetSearchFieldsSelectionWidget extends
        DropDownList<DataSetSearchFieldComboModel, PropertyType>
{
    private static final int WIDTH = 200;

    private static final String EMPTY_RESULT_SUFFIX = "search fields";

    private static final String CHOOSE_SUFFIX = "search field";

    static final String SUFFIX = "data-set-search";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private boolean dataLoaded;

    private List<PropertyType> propertyTypes;

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
    /*
     * * Always returns null.
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
     * Returns code of the selected option, or null - if nothing is selected.
     */
    public String tryGetSelectedCode()
    {
        return GWTUtils.tryGetSingleSelectedCode(this);
    }

    // NOTE: should be removed. See the todo for the whole class.
    public List<PropertyType> getAvailablePropertyTypes()
    {
        return propertyTypes;
    }

    public DataSetSearchFieldsSelectionWidget(DataSetSearchFieldsSelectionWidget source,
            final String idSuffix)
    {
        this(source.viewContext, idSuffix);
        ListStore<DataSetSearchFieldComboModel> sourceStore = source.getStore();
        sourceStore.clearFilters();
        this.setValue(findAnyFieldItem(sourceStore.getModels()));
        setWidth(WIDTH);
        setStore(sourceStore);
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
            propertyTypes = result.getList();

            final ListStore<DataSetSearchFieldComboModel> propertyTypeStore = getStore();
            propertyTypeStore.removeAll();
            List<DataSetSearchFieldComboModel> items = convertItems(propertyTypes);
            propertyTypeStore.add(items);
            if (propertyTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            setValue(findAnyFieldItem(items));

            dataLoaded = true;
        }
    }

    private static DataSetSearchFieldComboModel findAnyFieldItem(
            List<DataSetSearchFieldComboModel> items)
    {
        return findFieldItem(items, DataSetSearchFieldKind.ANY_FIELD);
    }

    private static DataSetSearchFieldComboModel findFieldItem(
            List<DataSetSearchFieldComboModel> items, DataSetSearchFieldKind fieldKind)
    {
        for (DataSetSearchFieldComboModel item : items)
        {
            DataSetSearchField field = item.getField();
            if (field.getKind() == fieldKind)
            {
                return item;
            }

        }
        throw new IllegalStateException("field not found: " + fieldKind);
    }

    @Override
    protected List<DataSetSearchFieldComboModel> convertItems(List<PropertyType> types)
    {
        final List<DataSetSearchFieldComboModel> result =
                new ArrayList<DataSetSearchFieldComboModel>();
        for (DataSetSearchFieldKind field : DataSetSearchFieldKind.getSimpleFields())
        {
            DataSetSearchField simpleField = DataSetSearchField.createSimpleField(field);
            addFieldComboModel(result, simpleField);
        }
        Collections.sort(types);
        List<String> allExpProps = addExperimentPropertyTypes(result, types);
        List<String> allSampleProps = addSamplePropertyTypes(result, types);
        List<String> allDataSetProps = addDataSetPropertyTypes(result, types);

        DataSetSearchField anyExperimentProperty =
                DataSetSearchField.createAnyExperimentProperty(allExpProps);
        addFieldComboModel(result, anyExperimentProperty);

        DataSetSearchField anySampleProperty =
                DataSetSearchField.createAnySampleProperty(allSampleProps);
        addFieldComboModel(result, anySampleProperty);

        DataSetSearchField anyField =
                DataSetSearchField.createAnyField(allExpProps, allSampleProps, allDataSetProps);
        addFieldComboModel(result, anyField);

        return result;
    }

    private void addFieldComboModel(List<DataSetSearchFieldComboModel> result,
            DataSetSearchField field)
    {
        result.add(createComboModel(field));
    }

    private static DataSetSearchFieldComboModel createComboModel(DataSetSearchField simpleField)
    {
        return new DataSetSearchFieldComboModel(getDisplayName(simpleField.getKind()), simpleField);
    }

    private static List<String> addSamplePropertyTypes(
            final List<DataSetSearchFieldComboModel> result, List<PropertyType> propertyTypes)
    {
        List<PropertyType> relevantPropertyTypes =
                PropertyTypesFilterUtil.filterSamplePropertyTypes(propertyTypes);
        return addPropertyTypes(result, relevantPropertyTypes, EntityKind.SAMPLE);
    }

    private static List<String> addDataSetPropertyTypes(
            final List<DataSetSearchFieldComboModel> result, List<PropertyType> propertyTypes)
    {
        List<PropertyType> relevantPropertyTypes =
                PropertyTypesFilterUtil.filterDataSetPropertyTypes(propertyTypes);
        return addPropertyTypes(result, relevantPropertyTypes, EntityKind.DATA_SET);
    }

    private static List<String> addExperimentPropertyTypes(
            final List<DataSetSearchFieldComboModel> result, List<PropertyType> propertyTypes)
    {
        List<PropertyType> relevantPropertyTypes =
                PropertyTypesFilterUtil.filterExperimentPropertyTypes(propertyTypes);
        return addPropertyTypes(result, relevantPropertyTypes, EntityKind.EXPERIMENT);
    }

    // returns codes of added properties
    private static List<String> addPropertyTypes(final List<DataSetSearchFieldComboModel> result,
            List<PropertyType> types, EntityKind kind)
    {
        List<String> allProps = new ArrayList<String>();
        for (final PropertyType st : types)
        {
            String code = st.getCode();
            allProps.add(code);
            DataSetSearchField field;
            switch (kind)
            {
                case EXPERIMENT:
                    field = DataSetSearchField.createExperimentProperty(code);
                    break;
                case SAMPLE:
                    field = DataSetSearchField.createSampleProperty(code);
                    break;
                case DATA_SET:
                    field = DataSetSearchField.createDataSetProperty(code);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported entity kind.");
            }
            DataSetSearchFieldComboModel comboModel = createPropertyComboModel(st, field, types);
            result.add(comboModel);
        }
        return allProps;
    }

    private static DataSetSearchFieldComboModel createPropertyComboModel(
            final PropertyType propertyType, DataSetSearchField searchField,
            List<PropertyType> types)
    {
        String prefix = getDisplayName(searchField.getKind());
        String property = PropertyTypeRenderer.getDisplayName(propertyType, types);
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

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE);
    }
}