/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntitySampleRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetSampleExecutor extends AbstractSetEntitySampleRelationExecutor<DataSetCreation, DataPE> implements
        ISetDataSetSampleExecutor
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    private DataSetTypeWithoutExperimentChecker dataSetTypeChecker;

    @PostConstruct
    public void init() throws Exception
    {
        dataSetTypeChecker = new DataSetTypeWithoutExperimentChecker(configurer == null ? new Properties() : configurer.getResolvedProps());
    }

    @Override
    protected ISampleId getRelatedId(DataSetCreation creation)
    {
        return creation.getSampleId();
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, SamplePE related)
    {
        if (related != null)
        {
            assertAllowedSampleForDataSet(entity, related);
            RelationshipUtils.setSampleForDataSet(entity, related, context.getSession());
            RelationshipUtils.setExperimentForDataSet(entity, related.getExperiment(), context.getSession());
        }
    }

    private void assertAllowedSampleForDataSet(DataPE dataSet, SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();

        if (experiment == null)
        {
            if (dataSetTypeChecker.isDataSetTypeWithoutExperiment(dataSet.getDataSetType().getCode()))
            {
                if (sample.getSpace() == null)
                {
                    throw new UserFailureException("Data set can not be registered because sample '"
                            + sample.getSampleIdentifier() + "' is a shared sample.");
                } else if (sample.getDeletion() != null)
                {
                    throw new UserFailureException("Data set can not be registered because sample '"
                            + sample.getSampleIdentifier() + "' is in trash.");
                }
            } else
            {
                throw new UserFailureException("Data set can not be registered because no experiment "
                        + "found for sample '" + sample.getSampleIdentifier() + "'.");
            }
        } else if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }
    }

}
