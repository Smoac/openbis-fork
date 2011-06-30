/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

/**
 * Provides a simplified view of an openBIS backend by allowing the following operations :
 * 
 * <pre>
 *  1. basic listing for the common openBIS entities kinds: projects, experiments, samples, and data sets.
 *  2. uploading datasets to openBIS
 *  3. downloading data sets from openBIS
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public interface ISimpleOpenbisServiceFacade
{
    /**
     * Return all spaces enriched with their projects and role assignments.
     */
    List<SpaceWithProjectsAndRoleAssignments> getSpacesWithProjects();

    /**
     * Return {@link Experiment} objects for a set of given experiment identifiers. If some of the
     * specified experiment identifiers does not exist in openBIS it will be silently ignored.
     */
    List<Experiment> getExperiments(List<String> experimentIdentifiers);

    /**
     * Return all experiments for a given list of project identifiers. If some of the specified
     * project identifiers does not exist in openBIS it will be silently ignored.
     */
    List<Experiment> listExperimentsForProjects(List<String> projectIdentifiers);

    /**
     * Return {@link Sample} objects for a set of given sample identifiers. If some of the specified
     * sample identifiers does not exist in openBIS it will be silently ignored.
     */
    List<Sample> getSamples(List<String> sampleIdentifiers);

    /**
     * Return all samples for a given list of experiments identifiers. If some of the specified
     * experiment identifiers does not exist in openBIS it will be silently ignored.
     */
    List<Sample> listSamplesForExperiments(List<String> experimentIdentifiers);

    /**
     * Return all samples for a given list of project identifiers. If some of the specified project
     * identifiers does not exist in openBIS it will be silently ignored.
     */
    public List<Sample> listSamplesForProjects(List<String> projectIdentifiers);

    /**
     * Return a {@link DataSet} object for for the given code. If some of the specified data set
     * code does not exist in openBIS, null will be returned.
     * 
     * @return The requested data set, or null if it does not exist.
     */
    DataSet getDataSet(String dataSetCodes);

    /**
     * Return {@link DataSet} objects for given a set of codes. If some of the specified data set
     * codes does not exist in openBIS it will be silently ignored.
     */
    List<DataSet> getDataSets(List<String> dataSetCodes);

    /**
     * Return all data sets for a given list of experiments identifiers. If some of the specified
     * experiment identifiers does not exist in openBIS it will be silently ignored.
     */
    List<DataSet> listDataSetsForExperiments(List<String> experimentIdentifiers);

    /**
     * Return all data sets for a given list of sample identifiers. If some of the specified sample
     * identifiers does not exist in openBIS it will be silently ignored.
     */
    List<DataSet> listDataSetsForSamples(List<String> sampleIdentifiers);

    /**
     * Returns all data set types available in openBIS.
     */
    List<DataSetType> listDataSetTypes();

    /**
     * Upload a new data set to the DSS.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A proxy to the newly added data set
     */
    public DataSet putDataSet(NewDataSetDTO newDataset, File dataSetFile);

    /**
     * Validates a data set.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A list of validation errors. The list is empty if there were no validation errors.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Checks whether the session is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    public void checkSession() throws InvalidSessionException;

    /**
     * Logs out from openBIS and frees all associated resources on the server.
     * <p>
     * IMPORTANT NOTE: If clients fail to call this method after finishing interaction with openBIS,
     * then their session will be kept on the server until it expires. It is considered a security
     * risk to leave openBIS sessions open.
     */
    public void logout();
}
