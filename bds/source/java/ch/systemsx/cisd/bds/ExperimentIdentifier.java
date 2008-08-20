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

package ch.systemsx.cisd.bds;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Identifier of the experiment which corresponds to the data. This is an immutable but extendable
 * value object class. An instance of this class allows unique identification in the database.
 * 
 * @author Franz-Josef Elmer
 */
public class ExperimentIdentifier implements IStorable
{
    static final String FOLDER = "experiment_identifier";

    static final String INSTANCE_CODE = "instance_code";

    static final String GROUP_CODE = "group_code";

    static final String PROJECT_CODE = "project_code";

    static final String EXPERIMENT_CODE = "experiment_code";

    /**
     * Loads the experiment identifier from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    static ExperimentIdentifier loadFrom(final IDirectory directory)
    {
        final IDirectory idFolder = Utilities.getSubDirectory(directory, FOLDER);
        final String instanceCode = Utilities.getTrimmedString(idFolder, INSTANCE_CODE);
        final String groupCode = Utilities.getTrimmedString(idFolder, GROUP_CODE);
        final String projectCode = Utilities.getTrimmedString(idFolder, PROJECT_CODE);
        final String experimentCode = Utilities.getTrimmedString(idFolder, EXPERIMENT_CODE);
        return new ExperimentIdentifier(instanceCode, groupCode, projectCode, experimentCode);
    }

    private final String instanceCode;

    private final String groupCode;

    private final String projectCode;

    private final String experimentCode;

    /**
     * Creates an instance for the specified database instance and codes of group, project and
     * experiment.
     * 
     * @param instanceCode A non-empty string of the instance code.
     * @param groupCode A non-empty string of the group code.
     * @param projectCode A non-empty string of the project code.
     * @param experimentCode A non-empty string of the experiment code.
     */
    public ExperimentIdentifier(final String instanceCode, final String groupCode,
            final String projectCode, final String experimentCode)
    {
        assert StringUtils.isEmpty(instanceCode) == false : "Undefined instance code";
        this.instanceCode = instanceCode;
        assert StringUtils.isEmpty(groupCode) == false : "Undefined group code";
        this.groupCode = groupCode;
        assert StringUtils.isEmpty(projectCode) == false : "Undefined project code";
        this.projectCode = projectCode;
        assert StringUtils.isEmpty(experimentCode) == false : "Undefined experiment code";
        this.experimentCode = experimentCode;
    }

    final ToStringBuilder createToStringBuilder()
    {
        final ToStringBuilder builder = new ToStringBuilder();
        builder.append(INSTANCE_CODE, instanceCode);
        builder.append(GROUP_CODE, groupCode);
        builder.append(PROJECT_CODE, projectCode);
        builder.append(EXPERIMENT_CODE, experimentCode);
        return builder;
    }

    final EqualsBuilder createEqualsBuilder(final ExperimentIdentifier experimentIdentifier)
    {
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(instanceCode, experimentIdentifier.instanceCode);
        builder.append(groupCode, experimentIdentifier.groupCode);
        builder.append(projectCode, experimentIdentifier.projectCode);
        builder.append(experimentCode, experimentIdentifier.experimentCode);
        return builder;
    }

    final HashCodeBuilder createHashCodeBuilder()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(instanceCode);
        builder.append(groupCode);
        builder.append(projectCode);
        builder.append(experimentCode);
        return builder;
    }

    /**
     * Returns the instance code.
     */
    public final String getInstanceCode()
    {
        return instanceCode;
    }

    /**
     * Returns the group code.
     */
    public final String getGroupCode()
    {
        return groupCode;
    }

    /**
     * Returns the project code.
     */
    public final String getProjectCode()
    {
        return projectCode;
    }

    /**
     * Returns the experiment code.
     */
    public final String getExperimentCode()
    {
        return experimentCode;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public void saveTo(final IDirectory directory)
    {
        final IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(INSTANCE_CODE, instanceCode);
        folder.addKeyValuePair(GROUP_CODE, groupCode);
        folder.addKeyValuePair(PROJECT_CODE, projectCode);
        folder.addKeyValuePair(EXPERIMENT_CODE, experimentCode);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentIdentifier == false)
        {
            return false;
        }
        return createEqualsBuilder((ExperimentIdentifier) obj).isEquals();
    }

    @Override
    public final int hashCode()
    {
        return createHashCodeBuilder().toHashCode();
    }

    @Override
    public final String toString()
    {
        return createToStringBuilder().toString();
    }

}
