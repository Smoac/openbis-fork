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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ListExternalDataCallback;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentDataSection extends AbstractExperimentTableSection<ExternalDataModel>
{
    private static final String PREFIX = "experiment-data-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public ExperimentDataSection(final Experiment experiment, final IViewContext<?> viewContext)
    {
        super(experiment, viewContext, "Data Sets", ID_PREFIX);
    }

    @Override
    protected ColumnModel createColumnModel()
    {
        return ExternalDataModel.createColumnModel(viewContext);
    }

    @Override
    protected RpcProxy<BaseListLoadConfig, BaseListLoadResult<ExternalDataModel>> createRpcProxy()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<ExternalDataModel>>()
        {
            @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<ExternalDataModel>> callback)
                {
                    final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
                    sampleCriteria.setExperimentIdentifier(experiment.getIdentifier());
                    viewContext.getCommonService().listExternalDataForExperiment(experiment.getIdentifier(),
                            new ListExternalDataCallback(getGenericViewContext(), callback));
                }

                @SuppressWarnings("unchecked")
                private IViewContext<IGenericClientServiceAsync> getGenericViewContext()
                {
                    return (IViewContext<IGenericClientServiceAsync>) viewContext;
            }
        };
    }

}
