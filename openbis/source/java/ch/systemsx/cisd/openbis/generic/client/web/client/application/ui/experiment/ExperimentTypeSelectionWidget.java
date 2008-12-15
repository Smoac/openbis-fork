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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;

/**
 * {@link ComboBox} containing list of experiment types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentTypeSelectionWidget extends ComboBox<ExperimentTypeModel>
{
    private static final String PREFIX = "experiment-type-select-";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        this.viewContext = viewContext;
        setId(ID + idSuffix);
        setEnabled(false);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setWidth(180);
        setFieldLabel("Experiment type");
        setStore(new ListStore<ExperimentTypeModel>());
    }

    /**
     * Returns the {@link ExperimentType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final ExperimentType tryGetSelectedExperimentType()
    {
        final List<ExperimentTypeModel> selection = getSelection();
        final int size = selection.size();
        if (size > 0)
        {
            assert size == 1 : "Selection is empty.";
            return selection.get(0).get(ModelDataPropertyNames.OBJECT);
        }
        return null;
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        viewContext.getService().listExperimentTypes(new ListExperimentTypesCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListExperimentTypesCallback extends
            AbstractAsyncCallback<List<ExperimentType>>
    {
        ListExperimentTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<ExperimentType> result)
        {
            final ListStore<ExperimentTypeModel> experimentTypeStore = getStore();
            experimentTypeStore.removeAll();
            experimentTypeStore.add(ExperimentTypeModel.convert(result));
            if (experimentTypeStore.getCount() > 0)
            {
                setEnabled(true);
                setEmptyText("Choose experiment type...");
            } else
            {
                setEmptyText("- No sample types found -");
            }
            applyEmptyText();
        }
    }
}