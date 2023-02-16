/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author pkupczyk
 */
public class DataSetBatchUpdatesDTO extends DataSetUpdatesDTO implements ICodeHolder
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String datasetCode;

    private DataSetBatchUpdateDetails details;

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public DataSetBatchUpdateDetails getDetails()
    {
        return details;
    }

    public void setDetails(DataSetBatchUpdateDetails details)
    {
        this.details = details;
    }

    @Override
    public String getCode()
    {
        return getDatasetCode();
    }

}
