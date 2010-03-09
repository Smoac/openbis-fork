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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleTypeSelectionWidget extends DropDownList<SampleTypeModel, SampleType>
{
    public static final String SUFFIX = "sample-type";

    private final IViewContext<?> viewContext;

    private final boolean onlyListable;

    private final boolean withAll;

    private final boolean withTypeInFile;

    private final String initialCodeOrNull;

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable, final boolean withAll, final boolean withTypeInFile,
            final String initialCodeOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.SAMPLE_TYPE, ModelDataPropertyNames.CODE,
                "sample type", "sample types");
        this.viewContext = viewContext;
        this.onlyListable = onlyListable;
        this.withAll = withAll;
        this.withTypeInFile = withTypeInFile;
        this.initialCodeOrNull = initialCodeOrNull;
        setAutoSelectFirst(withAll && initialCodeOrNull == null);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
    }

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable, final boolean withAll, final boolean withTypeInFile)
    {
        this(viewContext, idSuffix, onlyListable, withAll, withTypeInFile, null);
    }

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable)
    {
        this(viewContext, idSuffix, onlyListable, false, false);
    }

    /**
     * Returns the {@link SampleType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final SampleType tryGetSelectedSampleType()
    {
        return super.tryGetSelected();
    }

    /**
     * Returns the {@link SampleType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final List<SampleTypePropertyType> tryGetSelectedSamplePropertyTypes()
    {
        final SampleType selectedOrNull = tryGetSelectedSampleType();
        if (selectedOrNull == null)
        {
            return null;
        } else
        {
            return selectedOrNull.getAssignedPropertyTypes();
        }
    }

    @Override
    protected List<SampleTypeModel> convertItems(List<SampleType> result)
    {
        return SampleTypeModel.convert(result, onlyListable, withAll, withTypeInFile);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<SampleType>> callback)
    {
        viewContext.getCommonService().listSampleTypes(new ListSampleTypesCallback(viewContext));
        callback.ignore();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    // 
    // initial value support
    //

    private void selectInitialValue()
    {
        if (initialCodeOrNull != null)
        {
            trySelectByCode(initialCodeOrNull);
            updateOriginalValue();
        }
    }

    private void trySelectByCode(String code)
    {
        try
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, code);
        } catch (IllegalArgumentException ex)
        {
            MessageBox.alert("Error", "Sample Type '" + code + "' doesn't exist.", null);
        }
    }

    private class ListSampleTypesCallback extends SampleTypeSelectionWidget.ListItemsCallback
    {

        protected ListSampleTypesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<SampleType> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }
}
