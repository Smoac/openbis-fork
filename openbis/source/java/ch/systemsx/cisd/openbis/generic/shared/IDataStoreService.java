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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * Service interface of Data Store Server.
 *
 * @author Franz-Josef Elmer
 */
public interface IDataStoreService
{

    /**
     * Every time this interface and related DTO's are changed, we should increment this number.
     */
    public static final int VERSION = 1; // for release S51

    /**
     * Returns the version of this service. 
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @return {@link #VERSION}
     * 
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    public int getVersion(String sessionToken) throws InvalidAuthenticationException;
    
    /**
     * Returns from the specified data set locations those locations known by the Data Store Server.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * 
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    public List<String> getKnownDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException;
    
    /**
     * Deletes the specified data sets.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    public void deleteDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException;
    
    /**
     * Uploads the specified data sets to CIFEX.
     * 
     * @param sessionToken Valid token to identify authorised access.
     * @param context Context data needed for uploading.
     * @throws InvalidAuthenticationException if <code>sessionToken</code> is invalid.
     */
    public void uploadDataSetsToCIFEX(String sessionToken, List<ExternalDataPE> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException;
}
