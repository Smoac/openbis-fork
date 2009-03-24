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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A <i>Business Object</i> to load a list of {@link ExternalDataPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IExternalDataTable
{
    /**
     * Loads data sets specified by their codes. Data set codes will be ignored if no 
     * {@link ExternalDataPE} could be found.
     */
    void loadByDataSetCodes(List<String> dataSetCodes);
    
    /**
     * Loads the internal {@link ExternalDataPE} for given <var>identifier</var>.
     */
    void loadBySampleIdentifier(final SampleIdentifier sampleIdentifier);

    /**
     * Loads data sets which are directly link to all procedure instances of the specified
     * experiment.
     */
    void loadByExperimentIdentifier(ExperimentIdentifier identifier);
    
    /**
     * Returns the loaded {@link ExternalDataPE}.
     */
    List<ExternalDataPE> getExternalData();
    
    void deleteLoadedDataSets(DataStoreServerSessionManager dssSessionManager, String reason);
    
    void uploadLoadedDataSetsToCIFEX(DataStoreServerSessionManager dssSessionManager, String cifexURL, String password);

}
