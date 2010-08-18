/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataSetUploaderFactory implements IDataSetUploaderFactory
{
    private final Map<String, IDataSetUploaderFactory> factories =
            new LinkedHashMap<String, IDataSetUploaderFactory>();
    private final IDataSetUploaderFactory defaultFactory;
    private final Pattern pattern;

    DataSetUploaderFactory(IDataSetUploaderFactory defaultFactory, Pattern pattern)
    {
        this.defaultFactory = defaultFactory;
        this.pattern = pattern;
    }

    void register(String dataSetType, IDataSetUploaderFactory uploaderFactory)
    {
        factories.put(dataSetType, uploaderFactory);
    }

    public IDataSetUploader create(DataSetInformation dataSetInformation, DataSource dataSource,
            IEncapsulatedOpenBISService service, TimeSeriesDataSetUploaderParameters parameters)
    {
        IDataSetUploaderFactory factory = getFactory(dataSetInformation);
        return factory.create(dataSetInformation, dataSource, service, parameters);
    }

    public IDataSetUploader create(DataSetInformation dataSetInformation, ITimeSeriesDAO dao,
            IEncapsulatedOpenBISService service, TimeSeriesDataSetUploaderParameters parameters)
    {
        IDataSetUploaderFactory factory = getFactory(dataSetInformation);
        return factory.create(dataSetInformation, dao, service, parameters);
    }
    
    private IDataSetUploaderFactory getFactory(DataSetInformation dataSetInformation)
    {
        String dataSetType = dataSetInformation.getDataSetType().getCode();
        IDataSetUploaderFactory factory = factories.get(dataSetType);
        if (factory != null)
        {
            return factory;
        }
        if (pattern.matcher(dataSetType).matches() == false)
        {
            throw new UserFailureException("Unable to handle data sets of type " + dataSetType);
        }
        return defaultFactory;
    }

}
