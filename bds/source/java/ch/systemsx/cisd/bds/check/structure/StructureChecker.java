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

package ch.systemsx.cisd.bds.check.structure;

import java.io.File;

import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentRegistrationTimestamp;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.ToStringBuilder;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * This <i>static</i> class performs the structure checking.
 * 
 * @author Christian Ribeaud
 */
public final class StructureChecker
{

    StructureChecker()
    {
        // This class can not be instantiated.
    }

    /** Checks whether the directory specified is fully accessible. */
    private final static void checkAccessible(final File directory) throws UserFailureException
    {
        final String check = FileUtilities.checkDirectoryFullyAccessible(directory, "base");
        if (check != null)
        {
            throw new UserFailureException(check);
        }
    }

    /**
     * Checks the <i>BDS</i> structure in given <var>baseDir</var>.
     * 
     * @param baseDir the base directory where the <i>BDS</i> is located. Can not be
     *            <code>null</code>.
     */
    public final static StructureReport checkStructure(final File baseDir)
            throws DataStructureException
    {
        assert baseDir != null;
        checkAccessible(baseDir);
        final DataStructureV1_0 structure =
                (DataStructureV1_0) new DataStructureLoader(baseDir.getParentFile()).load(baseDir
                        .getName());
        return new StructureReport(structure);
    }

    //
    // Helper classes
    //

    public final static class StructureReport
    {
        private final DataSet dataSet;

        private final ExperimentIdentifier experimentIdentifier;

        private final ExperimentRegistrationTimestamp experimentRegistrationTimestamp;

        private final ExperimentRegistrator experimentRegistrator;

        private final Sample sample;

        private StructureReport(final DataStructureV1_0 dataStructure)
                throws DataStructureException
        {
            this.dataSet = dataStructure.getDataSet();
            this.experimentIdentifier = dataStructure.getExperimentIdentifier();
            this.experimentRegistrationTimestamp =
                    dataStructure.getExperimentRegistratorTimestamp();
            this.experimentRegistrator = dataStructure.getExperimentRegistrator();
            this.sample = dataStructure.getSample();
        }

        //
        // Object
        //

        @Override
        public final String toString()
        {
            final ToStringBuilder builder = new ToStringBuilder(OSUtilities.LINE_SEPARATOR);
            builder.append(dataSet);
            builder.append(experimentIdentifier);
            builder.append(experimentRegistrationTimestamp);
            builder.append(experimentRegistrator);
            builder.append(sample);
            return builder.toString();
        }
    }
}
