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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Asynchronous version of {@link ICommonClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonClientServiceAsync extends IClientServiceAsync
{
    /** @see ICommonClientService#listGroups(String) */
    public void listGroups(String databaseInstanceCode, AsyncCallback<List<Group>> callback);

    /** @see ICommonClientService#registerGroup(String, String, String) */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#listPersons() */
    public void listPersons(AsyncCallback<List<Person>> asyncCallback);

    /** @see ICommonClientService#registerPerson(String) */
    public void registerPerson(String code, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listRoles() */
    public void listRoles(AsyncCallback<List<RoleAssignment>> asyncCallback);

    /** @see ICommonClientService#registerGroupRole(String, String, String) */
    public void registerGroupRole(String roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteGroupRole(String, String, String) */
    public void deleteGroupRole(String roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerInstanceRole(String, String) */
    public void registerInstanceRole(String roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteInstanceRole(String, String) */
    public void deleteInstanceRole(String roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listSampleTypes() */
    public void listSampleTypes(AsyncCallback<List<SampleType>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples(ListSampleCriteria)
     */
    public void listSamples(final ListSampleCriteria criteria,
            AsyncCallback<ResultSet<Sample>> asyncCallback);

    /**
     * @see ICommonClientService#getSampleInfo(String)
     */
    public void getSampleInfo(final String sampleIdentifier,
            AsyncCallback<SampleGeneration> asyncCallback);

    /**
     * @see ICommonClientService#listExternalData(String)
     */
    public void listExternalData(final String sampleIdentifier,
            AsyncCallback<List<ExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listSearchableEntities()
     */
    public void listSearchableEntities(final AsyncCallback<List<SearchableEntity>> asyncCallback);

    /**
     * @see ICommonClientService#listMatchingEntities(SearchableEntity, String, IResultSetConfig)
     */
    public void listMatchingEntities(final SearchableEntity searchableEntity,
            final String queryText, final IResultSetConfig<String> resultSetConfig,
            final AsyncCallback<ResultSet<MatchingEntity>> asyncCallback);

    /**
     * @see ICommonClientService#removeResultSet(String)
     */
    public void removeResultSet(final String resultSetKey, final AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#registerSample(SampleToRegister)
     */
    public void registerSample(final SampleToRegister sample,
            final AsyncCallback<Void> asyncCallback) throws UserFailureException;
}
