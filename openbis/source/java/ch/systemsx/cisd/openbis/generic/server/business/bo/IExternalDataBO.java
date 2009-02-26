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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IExternalDataBO extends IBusinessObject
{
    /**
     * Returns the external data item which has been created by
     * {@link #define(ExternalData, ProcedurePE, SamplePE, SourceType)}.
     */
    public ExternalDataPE getExternalData();
    
    /**
     * Defines a new external data item. After invocation of this method {@link IExperimentBO#save()} should
     * be invoked to store the new external data item in the Data Access Layer.
     */
    public void define(ExternalData externalData, ProcedurePE procedure, SamplePE sample,
            SourceType sourceType);
    
    /**
     * Loads the external data item with specified code.
     */
    public void loadByCode(String dataSetCode);

    /**
     * Enrich external data with parents, procedure, and experiment.
     */
    public void enrichWithParentsAndProcedure();

}
