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

package ch.systemsx.cisd.bds.v1_1;

import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * A small {@link ExperimentIdentifier} extension which adds the reading/writing of database
 * instance <i>UUID</i>.
 * <p>
 * Does not override {@link #equals(Object)} resp. {@link #hashCode()} methods as equality for this
 * class is based on the same rules.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ExperimentIdentifierWithUUID extends ExperimentIdentifier
{
    public static final String INSTANCE_UUID = "instance_uuid";

    private final String instanceUUID;

    /**
     * Creates an instance for the specified {@link ExperimentIdentifier} and given database
     * instance <i>UUID</i>.
     * 
     * @param instanceUUID A non-empty string of the instance <i>UUID</i>.
     * @param experimentIdentifier A non-<code>null</code> experiment identifier.
     */
    public ExperimentIdentifierWithUUID(final ExperimentIdentifier experimentIdentifier,
            final String instanceUUID)
    {
        this(experimentIdentifier.getInstanceCode(), instanceUUID, experimentIdentifier
                .getSpaceCode(), experimentIdentifier.getProjectCode(), experimentIdentifier
                .getExperimentCode());
    }

    /**
     * Creates an instance for the specified database instance and codes of space, project and
     * experiment.
     * 
     * @param instanceCode A non-empty string of the instance code.
     * @param instanceUUID A non-empty string of the instance <i>UUID</i>.
     * @param spaceCode A non-empty string of the space code.
     * @param projectCode A non-empty string of the project code.
     * @param experimentCode A non-empty string of the experiment code.
     */
    public ExperimentIdentifierWithUUID(final String instanceCode, final String instanceUUID,
            final String spaceCode, final String projectCode, final String experimentCode)
    {
        super(instanceCode, spaceCode, projectCode, experimentCode);
        assert StringUtils.isEmpty(instanceUUID) == false : "Undefined instance UUID";
        this.instanceUUID = instanceUUID;
    }

    /**
     * Returns the instance <i>UUID</i>.
     */
    public final String getInstanceUUID()
    {
        return instanceUUID;
    }

    //
    // ExperimentIdentifier
    //

    /**
     * Loads the experiment identifier from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    public final static ExperimentIdentifierWithUUID loadFrom(final IDirectory directory)
    {
        final IDirectory idFolder = Utilities.getSubDirectory(directory, FOLDER);
        final String instanceCode = Utilities.getTrimmedString(idFolder, INSTANCE_CODE);
        final String spaceCode = Utilities.getTrimmedString(idFolder, SPACE_CODE);
        final String projectCode = Utilities.getTrimmedString(idFolder, PROJECT_CODE);
        final String experimentCode = Utilities.getTrimmedString(idFolder, EXPERIMENT_CODE);
        final String instanceUUID =
                Utilities.getTrimmedString(idFolder, ExperimentIdentifierWithUUID.INSTANCE_UUID);
        return new ExperimentIdentifierWithUUID(instanceCode, instanceUUID, spaceCode, projectCode,
                experimentCode);

    }

    @Override
    public final void saveTo(final IDirectory directory)
    {
        super.saveTo(directory);
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(ExperimentIdentifierWithUUID.INSTANCE_UUID, instanceUUID);
    }
}
