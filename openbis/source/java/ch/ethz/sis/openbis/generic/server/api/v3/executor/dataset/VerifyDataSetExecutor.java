/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class VerifyDataSetExecutor implements IVerifyDataSetExecutor, InitializingBean
{

    @Autowired
    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Autowired
    private IVerifyDataSetContainersExecutor verifyDataSetContainersExecutor;

    @Autowired
    private IVerifyDataSetParentsExecutor verifyDataSetParentsExecutor;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    private DataSetTypeWithoutExperimentChecker dataSetTypeChecker;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        dataSetTypeChecker = new DataSetTypeWithoutExperimentChecker(
                configurer == null ? new Properties() : configurer.getResolvedProps());
    }

    @Override
    public void verify(IOperationContext context, Collection<DataPE> dataSets)
    {
        verifyEntityPropertyExecutor.verify(context, dataSets);
        verifyDataSetContainersExecutor.verify(context, dataSets);
        verifyDataSetParentsExecutor.verify(context, dataSets);
    }

    @Override
    public DataSetTypeWithoutExperimentChecker getDataSetTypeChecker()
    {
        return dataSetTypeChecker;
    }

    @Override
    public void checkDataSetsDoNotNeedAnExperiment(String sampleIdentifier, List<DataPE> dataSets)
    {
        List<String> dataSetsNeedingExperiment = new ArrayList<String>();
        for (DataPE dataSet : dataSets)
        {
            String dataSetTypeCode = dataSet.getDataSetType().getCode();
            if (dataSetTypeChecker.isDataSetTypeWithoutExperiment(dataSetTypeCode) == false)
            {
                dataSetsNeedingExperiment.add(dataSet.getCode());
            }
        }
        if (dataSetsNeedingExperiment.isEmpty() == false)
        {
            throw new UserFailureException("Operation cannot be performed, because the sample "
                    + sampleIdentifier + " has the following datasets which need an experiment: "
                    + CollectionUtils.abbreviate(dataSetsNeedingExperiment, 10));
        }
    }

}
