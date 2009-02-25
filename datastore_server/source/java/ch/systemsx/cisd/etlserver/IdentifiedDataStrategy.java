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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * This <code>IDataStoreStrategy</code> implementation if for data set that has been <i>identified</i>,
 * meaning that kind of connection to this data set could be found in the database (through the
 * derived <i>Master Plate</i> or through the experiment specified).
 * 
 * @author Christian Ribeaud
 */
public final class IdentifiedDataStrategy implements IDataStoreStrategy
{
    static final String DATA_SET_TYPE_PREFIX = "DataSetType_";

    static final String SAMPLE_PREFIX = "Sample_";

    static final String EXPERIMENT_PREFIX = "Experiment_";

    static final String PROJECT_PREFIX = "Project_";

    static final String GROUP_PREFIX = "Group_";

    static final String INSTANCE_PREFIX = "Instance_";

    public static final String DATASET_PREFIX = "Dataset_";

    static final String UNEXPECTED_PATHS_MSG_FORMAT =
            "There are unexpected paths '%s' in data store '%s'. I'll proceed anyway.";

    static final String STORAGE_LAYOUT_ERROR_MSG_PREFIX = "Serious error in data store layout: ";

    IdentifiedDataStrategy()
    {

    }

    private static String createInstanceDirectory(final DataSetInformation dataSetInfo)
    {
        final String instanceUUID = dataSetInfo.getInstanceUUID();
        assert instanceUUID != null : "Instance UUID can not be null.";
        return INSTANCE_PREFIX + instanceUUID;
    }

    private static String createGroupDirectory(final DataSetInformation dataSetInfo)
    {
        final ExperimentIdentifier identifier = dataSetInfo.getExperimentIdentifier();
        assert identifier != null : "Identifier can not be null.";
        final String groupCode = identifier.getGroupCode();
        assert groupCode != null : "Group code can not be null.";
        return GROUP_PREFIX + groupCode;
    }

    private static String createProjectDirectory(final DataSetInformation dataSetInfo)
    {
        final ExperimentIdentifier identifier = dataSetInfo.getExperimentIdentifier();
        assert identifier != null : "Identifier can not be null.";
        final String projectCode = identifier.getProjectCode();
        assert projectCode != null : "Project code can not be null.";
        return PROJECT_PREFIX + projectCode;
    }

    private static String createExperimentDirectory(final DataSetInformation dataSetInfo)
    {
        final ExperimentIdentifier identifier = dataSetInfo.getExperimentIdentifier();
        assert identifier != null : "Identifier can not be null.";
        final String experimentCode = identifier.getExperimentCode();
        assert experimentCode != null : "Experiment code can not be null.";
        return EXPERIMENT_PREFIX + experimentCode;
    }

    private final static String createSampleDirectory(final DataSetInformation dataSetInfo)
    {
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        assert sampleIdentifier != null : "Sample identifier can not be null.";
        return SAMPLE_PREFIX + sampleIdentifier.getSampleCode();
    }

    private static String createDatasetDirectory(final DataSetInformation dataSetInfo)
    {
        final String dataSetCode = dataSetInfo.getDataSetCode();
        assert dataSetCode != null : "Dataset code con not be null.";
        return DATASET_PREFIX + dataSetCode;
    }

    final static String createDataSetTypeDirectory(final DataSetType dataSetType)
    {
        final String dataSetTypeCode = dataSetType.getCode();
        assert dataSetTypeCode != null : "Data set type code can not be null.";
        return DATA_SET_TYPE_PREFIX + dataSetTypeCode;
    }

    /**
     * Computes the base directory with given <var>baseDir</var> and given <var>dataSetInfo</var>
     * and returns it as <code>File</code>.
     * <p>
     * Note that this method does not call {@link File#mkdirs()} on returned <code>File</code>.
     * </p>
     */
    private final static File createBaseDirectory(final File baseDir,
            final DataSetInformation dataSetInfo, final DataSetType dataSetType)
    {
        final File instanceDir = new File(baseDir, createInstanceDirectory(dataSetInfo));
        final File groupDir = new File(instanceDir, createGroupDirectory(dataSetInfo));
        final File projectDir = new File(groupDir, createProjectDirectory(dataSetInfo));
        final File experimentDir = new File(projectDir, createExperimentDirectory(dataSetInfo));
        final File dataSetTypeDir =
                new File(experimentDir, createDataSetTypeDirectory(dataSetType));
        final File sampleDir = new File(dataSetTypeDir, createSampleDirectory(dataSetInfo));
        final File datasetDir = new File(sampleDir, createDatasetDirectory(dataSetInfo));
        return datasetDir;

    }

    //
    // IDataStoreStrategy
    //

    public final DataStoreStrategyKey getKey()
    {
        return DataStoreStrategyKey.IDENTIFIED;
    }

    public final File getBaseDirectory(final File storeRoot, final DataSetInformation dataSetInfo,
            final DataSetType dataSetType)
    {
        assert storeRoot != null : "Store root can not be null";
        assert dataSetInfo != null : "Data set information can not be null";
        assert dataSetType != null : "Data set type can not be null";
        final File baseDirectory = createBaseDirectory(storeRoot, dataSetInfo, dataSetType);
        if (baseDirectory.exists())
        {
            throw EnvironmentFailureException.fromTemplate(STORAGE_LAYOUT_ERROR_MSG_PREFIX
                    + "Data set directory '%s' exists but has been designed to be unique.",
                    baseDirectory.getPath());
        }
        if (baseDirectory.isFile())
        {
            throw EnvironmentFailureException.fromTemplate(STORAGE_LAYOUT_ERROR_MSG_PREFIX
                    + "Base directory '%s' is a file.", baseDirectory);
        }
        return baseDirectory;
    }

    public final File getTargetPath(final File baseDirectory, final File incomingDataSetPath)
            throws IllegalStateException
    {
        assert baseDirectory != null : "Base directory can not be null";
        assert incomingDataSetPath != null : "Incoming data set can not be null";
        final File targetPath = new File(baseDirectory, incomingDataSetPath.getName());
        if (targetPath.exists())
        {
            throw new IllegalStateException(String.format(
                    "Target path '%s' of identified incoming data set already exists "
                            + "(which it shouldn't), bailing out.", targetPath.getAbsolutePath()));
        }
        return targetPath;
    }
}
