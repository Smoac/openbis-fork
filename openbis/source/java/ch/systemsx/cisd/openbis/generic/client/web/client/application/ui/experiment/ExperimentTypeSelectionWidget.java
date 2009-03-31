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

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of experiment types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentTypeSelectionWidget extends
        DropDownList<ExperimentTypeModel, ExperimentType>
{
    public static final String SUFFIX = "experiment-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.EXPERIMENT_TYPE, ModelDataPropertyNames.CODE,
                "experiment type", "experiment types");
        this.viewContext = viewContext;
    }

    /**
     * Returns the {@link ExperimentType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final ExperimentType tryGetSelectedExperimentType()
    {
        return super.tryGetSelected();
    }

    @Override
    protected List<ExperimentTypeModel> convertItems(List<ExperimentType> result)
    {
        return ExperimentTypeModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<ExperimentType>> callback)
    {
        viewContext.getService().listExperimentTypes(callback);
    }
    
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.EXPERIMENT_TYPE);
    }
}