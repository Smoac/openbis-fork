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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.IDatabaseInstanceFinder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Interface of providers of data needed for authorization.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthorizationDataProvider extends IDatabaseInstanceFinder
{
    /**
     * Returns a list of all data spaces.
     */
    public List<SpacePE> listSpaces();

    /**
     * Returns the space for the given <var>spaceCode</var> or <code>null</code>, if it does not
     * exist.
     */
    public SpacePE tryGetSpace(DatabaseInstancePE databaseInstance, String spaceCode);

    /**
     * Returns the experiment for the given <var>permId</var> or <code>null</code>, if it does not
     * exist.
     */
    public ExperimentPE tryGetExperimentByPermId(String permId);

    /**
     * Returns the sample for the given <var>permId</var> or <code>null</code>, if it does not
     * exist.
     */
    public SamplePE tryGetSampleByPermId(String permId);

    /**
     * Returns the project of the experiment to which the specified data set belongs.
     * 
     * @return <code>null</code> if no data set found.
     */
    public ProjectPE tryGetProject(String dataSetCode);

    /**
     * Returns the information necessary to determine if a user is allowed to access this data set.
     */
    public DataSetAccessPE tryGetDatasetAccessData(String dataSetCode);

    /**
     * Returns the information necessary to determine if a user is allowed to access the data sets.
     */
    public Set<DataSetAccessPE> getDatasetCollectionAccessData(List<String> dataSetCodes);

    /**
     * Returns the information necessary to determine if a user is allowed to access the samples.
     */
    public Set<SampleAccessPE> getSampleCollectionAccessData(List<TechId> sampleIds);

    /**
     * Returns the data space of an entity with given <var>entityKind</var> and <var>techId</var>
     * 
     * @return <code>null</code> if entity has no group set.
     */
    public SpacePE tryGetSpace(SpaceOwnerKind entityKind, TechId techId);

    /**
     * Returns the sample with given <var>techId</var>.
     */
    public SamplePE getSample(TechId techId);

    /**
     * Returns the sample with given <var>permId</var>.
     */
    public SamplePE getSample(PermId id);

    /**
     * Returns the filter with given <var>techId</var>
     */
    public GridCustomFilterPE getGridCustomFilter(TechId techId);

    /**
     * Returns the grid custom column with given <var>techId</var>
     */
    public GridCustomColumnPE getGridCustomColumn(TechId techId);

    /**
     * Returns the query with specified ID.
     */
    public QueryPE getQuery(TechId techId);

}
