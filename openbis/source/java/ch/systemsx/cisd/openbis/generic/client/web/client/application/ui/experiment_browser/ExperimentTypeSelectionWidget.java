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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
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
public class ExperimentTypeSelectionWidget extends ComboBox<ExperimentTypeModel>
{

    public final class ListExperimentTypesCallback extends
            AbstractAsyncCallback<List<ExperimentType>>
    {
        private final boolean allowEmptyCall;

        ListExperimentTypesCallback(final IViewContext<?> viewContext, final boolean allowEmpty)
        {
            super(viewContext);
            allowEmptyCall = allowEmpty;
        }

        @Override
        protected void process(final List<ExperimentType> result)
        {
            experimentTypeStore.removeAll();
            experimentTypeStore.add(convert(result));
            if (experimentTypeStore.getCount() > 0)
            {
                setEnabled(true);
                if (allowEmptyCall == false)
                {
                    setValue(experimentTypeStore.getAt(0));
                }
            }
        }

        List<ExperimentTypeModel> convert(final List<ExperimentType> experimentTypes)
        {
            final List<ExperimentTypeModel> result = new ArrayList<ExperimentTypeModel>();
            for (final ExperimentType st : experimentTypes)
            {
                result.add(new ExperimentTypeModel(st));
            }
            return result;
        }
    }

    private static final String PREFIX = "experiment-type-select-";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ListStore<ExperimentTypeModel> experimentTypeStore;

    private final boolean allowEmpty;

    public ExperimentTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        this(viewContext, false, idSuffix);
    }

    public ExperimentTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final boolean allowEmpty, final String idSuffix)
    {
        this.viewContext = viewContext;
        this.allowEmpty = allowEmpty;
        setId(ID + idSuffix);
        setEmptyText(allowEmpty ? "Choose experiment type..." : "- No experiment types found -");
        setEnabled(false);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setWidth(150);
        setFieldLabel("Experiment type");
        experimentTypeStore = new ListStore<ExperimentTypeModel>();
        setStore(experimentTypeStore);
        addListener(Events.OnClick, new Listener<BaseEvent>()
            {

                public void handleEvent(final BaseEvent be)
                {
                    expand();
                }
            });
    }

    public ExperimentType tryGetSelected()
    {

        final List<ExperimentTypeModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(ModelDataPropertyNames.OBJECT);
        } else
        {
            return null;
        }
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        viewContext.getService().listExperimentTypes(
                new ListExperimentTypesCallback(viewContext, allowEmpty));
    }
}