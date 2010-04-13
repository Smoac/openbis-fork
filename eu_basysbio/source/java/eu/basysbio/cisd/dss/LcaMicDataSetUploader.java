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

import java.io.File;

import javax.sql.DataSource;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class LcaMicDataSetUploader extends AbstractDataSetUploader
{
    static final IDataSetUploaderFactory FACTORY = new IDataSetUploaderFactory()
    {
        
        public IDataSetUploader create(DataSetInformation dataSetInformation,
                DataSource dataSource, IEncapsulatedOpenBISService service,
                TimeSeriesDataSetUploaderParameters parameters)
        {
            return new LcaMicDataSetUploader(dataSource, service, parameters);
        }

        public IDataSetUploader create(DataSetInformation dataSetInformation,
                ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
                TimeSeriesDataSetUploaderParameters parameters)
        {
            return new LcaMicDataSetUploader(dao, service, parameters);
        }
    };

    LcaMicDataSetUploader(DataSource dataSource, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dataSource, service, parameters);
    }

    public LcaMicDataSetUploader(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dao, service, parameters);
    }

    @Override
    protected void handleTSVFile(File tsvFile, DataSetInformation dataSetInformation,
            IDropBoxFeeder feeder)
    {
        // TODO Auto-generated method stub

    }

}
