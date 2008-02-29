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

package ch.systemsx.cisd.datamover.filesystem.remote.rsync;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * A class that helps checking an <code>rsync</code> binary for its version.
 * 
 * @author Bernd Rinn
 */
final class RsyncVersionChecker
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RsyncVersionChecker.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RsyncVersionChecker.class);

    /**
     * The class holding the version information about an <code>rsync</code> binary.
     */
    static class RsyncVersion
    {

        /**
         * The version string of the <code>rsync</code> binary.
         */
        private final String rsyncVersion;

        /**
         * The major version of the <code>rsync</code> binary.
         */
        private final int rsyncMajorVersion;

        /**
         * The minor version of the <code>rsync</code> binary.
         */
        private final int rsyncMinorVersion;

        /**
         * The patch version of the <code>rsync</code> binary.
         */
        private final int rsyncPatchVersion;

        /**
         * Returns <code>true</code>, if the version is a pre-release version of <code>rsync</code>.
         */
        private final boolean rsyncPreReleaseVersion;

        private RsyncVersion(String rsyncVersion, int rsyncMajorVersion, int rsyncMinorVersion, int rsyncPatchVersion,
                boolean rsyncPreReleaseVersion)
        {
            this.rsyncVersion = rsyncVersion;
            this.rsyncMajorVersion = rsyncMajorVersion;
            this.rsyncMinorVersion = rsyncMinorVersion;
            this.rsyncPatchVersion = rsyncPatchVersion;
            this.rsyncPreReleaseVersion = rsyncPreReleaseVersion;
        }

        /**
         * @return The version string of the <code>rsync</code> binary.
         */
        public String getVersionString()
        {
            return rsyncVersion;
        }

        /**
         * @return The major version of the <code>rsync</code> binary.
         */
        public int getMajorVersion()
        {
            return rsyncMajorVersion;
        }

        /**
         * @return The minor version of the <code>rsync</code> binary.
         */
        public int getMinorVersion()
        {
            return rsyncMinorVersion;
        }

        /**
         * @return The patch version of the <code>rsync</code> binary.
         */
        public int getPatchVersion()
        {
            return rsyncPatchVersion;
        }
        
        /**
         * @return <code>true</code>, if this version is a pre-release version.
         */
        public boolean isRsyncPreReleaseVersion()
        {
            return rsyncPreReleaseVersion;
        }

        /**
         * @return <code>true</code>, if this version is newer or as new the minimal version specified. 
         */
        public boolean isNewerOrEqual(int minMajorVerson, int minMinorVersion, int minPatchLevel)
        {
            if (rsyncMajorVersion < minMajorVerson)
            {
                return false;
            } else if (rsyncMajorVersion > minMajorVerson)
            {
                return true;
            } else
            {
                if (rsyncMinorVersion < minMinorVersion)
                {
                    return false;
                } else if (rsyncMinorVersion > minMinorVersion)
                {
                    return true;
                } else
                {
                    return (rsyncPatchVersion >= minPatchLevel);
                }
            }
        }

    }

    /**
     * Looks up the version of the <var>rsyncExecutable</var>, and, on its way, checks whether its a good executable at
     * all.
     * 
     * @param rsyncExecutable The executable to find the version for.
     * @return The version information, or <code>null</code>, if the executable doesn't work.
     */
    static RsyncVersion getVersion(String rsyncExecutable)
    {
        assert rsyncExecutable != null;

        String rsyncVersion = tryGetRsyncVersion(rsyncExecutable);
        if (rsyncVersion == null)
        {
            return null;
        }
        String[] rsyncVersionParts = rsyncVersion.split("\\.");
        if (rsyncVersionParts.length != 3)
        {
            return null;
        }
        final int rsyncMajorVersion = Integer.parseInt(rsyncVersionParts[0]);
        final int rsyncMinorVersion = Integer.parseInt(rsyncVersionParts[1]);
        int rsyncPatchVersion;
        boolean preReleaseVersion = false;
        try
        {
            rsyncPatchVersion = Integer.parseInt(rsyncVersionParts[2]);
        } catch (NumberFormatException ex)
        {
            final int preIdx = rsyncVersionParts[2].indexOf("pre");
            if (preIdx >= 0)
            {
                rsyncPatchVersion = Integer.parseInt(rsyncVersionParts[2].substring(0, preIdx));
                preReleaseVersion = true;
            } else
            {
                throw ex;
            }
        }

        return new RsyncVersion(rsyncVersion, rsyncMajorVersion, rsyncMinorVersion, rsyncPatchVersion,
                preReleaseVersion);
    }

    private static String tryGetRsyncVersion(String rsyncExecutableToCheck)
    {
        final long TIME_TO_WAIT_FOR_COMPLETION = 2 * 1000;
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(rsyncExecutableToCheck, "--version"),
                        TIME_TO_WAIT_FOR_COMPLETION, operationLog, machineLog);
        result.log();
        final List<String> processOutput = result.getProcessOutput();
        if (processOutput.size() == 0)
        {
            return null;
        }
        final String versionString = tryExtractRsyncVersion(processOutput.get(0));
        return versionString;
    }

    private static String tryExtractRsyncVersion(String rsyncVersionLine)
    {
        if (rsyncVersionLine.startsWith("rsync  version") == false)
        {
            return null;
        } else
        {
            final String[] versionStringParts = rsyncVersionLine.split("\\s+");
            if (versionStringParts.length < 3)
            {
                return null;
            }
            return versionStringParts[2];
        }
    }

}
