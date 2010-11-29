/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Read-only table for experiments. Holds a collection of instances of {@link ExperimentPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IExperimentTable
{
    /**
     * Loads all experiments of given type and from given project together with all their
     * properties.
     * 
     * @param experimentTypeCodeOrNull the experiment type code or <code>null</code>.
     * @param projectIdentifier identifier of the project to which we restrict the load.
     */
    public void load(String experimentTypeCodeOrNull, ProjectIdentifier projectIdentifier);

    /** Returns the loaded {@link ExperimentPE}. */
    public List<ExperimentPE> getExperiments();

    /**
     * Defines new experiments of given type.
     */
    public void add(List<NewBasicExperiment> entities, ExperimentTypePE experimentTypePE);

    /**
     * Prepares the given experiments for update and stores them in this table.
     * <p>
     * NOTE: Business rules are checked in this step as well for better performance.
     */
    public void prepareForUpdate(List<ExperimentBatchUpdatesDTO> updates)
            throws UserFailureException;

    /**
     * Saves experiments in the database.
     */
    public void save();

}
