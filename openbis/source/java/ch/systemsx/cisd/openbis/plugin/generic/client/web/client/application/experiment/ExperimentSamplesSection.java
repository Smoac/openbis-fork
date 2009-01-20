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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.YesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * {@link SectionPanel} containing experiment samples.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentSamplesSection extends AbstractExperimentTableSection<SampleModel>
{
    private static final String PREFIX = "experiment-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public ExperimentSamplesSection(final Experiment experiment, final IViewContext<?> viewContext)
    {
        super(experiment, viewContext, "Samples", ID_PREFIX);
    }

    @Override
    protected ColumnModel createColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        final ColumnConfig codeColumn =
                ColumnConfigFactory.createCodeColumnConfig(viewContext, CommonSampleColDefKind.CODE
                        .id());
        configs.add(codeColumn);

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.SAMPLE_TYPE), ModelDataPropertyNames.SAMPLE_TYPE));
        configs.add(createColumnConfig(Dict.GROUP, CommonSampleColDefKind.GROUP));
        final ColumnConfig isInvalidColumn =
                createColumnConfig(Dict.IS_INVALID, CommonSampleColDefKind.IS_INVALID);
        isInvalidColumn.setRenderer(new YesNoRenderer());
        configs.add(isInvalidColumn);

        configs.add(ColumnConfigFactory.createRegistrationDateColumnConfig(viewContext,
                CommonSampleColDefKind.REGISTRATION_DATE.id()));
        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(viewContext,
                CommonSampleColDefKind.REGISTRATOR.id()));

        return new ColumnModel(configs);
    }

    private ColumnConfig createColumnConfig(String messageKey, CommonSampleColDefKind columnKind)
    {
        return ColumnConfigFactory.createDefaultColumnConfig(viewContext.getMessage(messageKey),
                columnKind.id());
    }

    @Override
    protected RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>> createRpcProxy()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>>()
            {

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
                {
                    final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
                    sampleCriteria.setExperimentIdentifier(experiment.getIdentifier());
                    viewContext.getCommonService().listSamples(sampleCriteria,
                            new ListSamplesCallback(getGenericViewContext(), callback));
                }

                @SuppressWarnings("unchecked")
                private IViewContext<IGenericClientServiceAsync> getGenericViewContext()
                {
                    return (IViewContext<IGenericClientServiceAsync>) viewContext;
                }
            };
    }

    final static class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {
        private final AsyncCallback<BaseListLoadResult<SampleModel>> delegate;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
        }

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final ResultSet<Sample> result)
        {
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result.getList());
            final BaseListLoadResult<SampleModel> baseListLoadResult =
                    new BaseListLoadResult<SampleModel>(sampleModels);
            delegate.onSuccess(baseListLoadResult);
        }
    }

}
