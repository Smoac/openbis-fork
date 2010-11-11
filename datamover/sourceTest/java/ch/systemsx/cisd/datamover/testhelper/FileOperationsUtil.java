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

package ch.systemsx.cisd.datamover.testhelper;

import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * @author Tomasz Pylak
 */
public class FileOperationsUtil
{
    public final static IFileSysOperationsFactory createTestFactory()
    {
        return new FileSysOperationsFactory(createDummyFileSysParameters());
    }

    private static IFileSysParameters createDummyFileSysParameters()
    {
        return new IFileSysParameters()
            {
                public String getRsyncExecutable()
                {
                    return null;
                }

                public String getSshExecutable()
                {
                    return null;
                }

                public boolean isRsyncOverwrite()
                {
                    return false;
                }

                public long getIntervalToWaitAfterFailure()
                {
                    return 0;
                }

                public int getMaximalNumberOfRetries()
                {
                    return 0;
                }

                public String getIncomingRsyncExecutable()
                {
                    return null;
                }

                public String getOutgoingRsyncExecutable()
                {
                    return null;
                }

                public String[] getExtraRsyncParameters()
                {
                    return null;
                }
            };
    }
}
