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

package ch.systemsx.cisd.datamover;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.helper.FileSystemHelper;

/**
 * A {@link DirectoryScanningTimerTask.IPathHandler} that moves paths out of the way within one file system by calling
 * {@link File#renameTo(File)}..
 * 
 * @author Bernd Rinn
 */
public class IntraFSPathMover implements DirectoryScanningTimerTask.IPathHandler
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, IntraFSPathMover.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, IntraFSPathMover.class);

    private final File destinationDirectory;

    private final String prefixTemplate;

    /**
     * Creates a <var>PathMover</var>.
     * 
     * @param destinationDirectory The directory to move paths to.
     */
    public IntraFSPathMover(File destinationDirectory, String prefixTemplate)
    {
        assert destinationDirectory != null;
        assert FileUtilities.checkDirectoryFullyAccessible(destinationDirectory, "destination") == null : FileUtilities
                .checkDirectoryFullyAccessible(destinationDirectory, "destination");
        assert prefixTemplate != null;

        this.destinationDirectory = destinationDirectory;
        this.prefixTemplate = prefixTemplate;
    }

    public boolean handle(File path)
    {
        assert path != null;
        assert destinationDirectory != null;
        assert prefixTemplate != null;

        final String destinationPath =
                FileSystemHelper.createDestinationPath(path, null, destinationDirectory, prefixTemplate);
        if (operationLog.isInfoEnabled())
        {
            operationLog
                    .info(String.format("Moving path '%s' to '%s'", path.getPath(), destinationPath));
        }
        boolean movedOK = path.renameTo(new File(destinationPath));
        if (movedOK == false)
        {
            notificationLog.error(String.format("Moving path '%s' to directory '%s' failed.", path,
                    destinationDirectory));
        }
        return movedOK;
    }

}
