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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.ITriggerable;

/**
 * The <i>DataMover</i> shutdown hook.
 * 
 * @author Christian Ribeaud
 */
final class DataMoverShutdownHook implements ITriggerable
{
    private static final String DATAMOVER_PID_FILE_NAME = "datamover.pid";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataMoverShutdownHook.class);

    private final ITerminable terminable;

    private final File outgoingTargetLocationFile;

    private final IExitHandler exitHandler;

    DataMoverShutdownHook(final File locationFile, final ITerminable terminable,
            final IExitHandler exitHandler)
    {
        this.terminable = terminable;
        this.exitHandler = exitHandler;
        if (locationFile == null)
        {
            throw new IllegalArgumentException("Unspecified outgoing target location file.");
        }
        this.outgoingTargetLocationFile = locationFile;
    }

    private final static void createMarkerFile(final File markerFile)
    {
        try
        {
            FileUtils.touch(markerFile);
        } catch (final IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Can not create marker file '%s'.",
                    markerFile.getAbsolutePath());
        }
    }

    private final static void deleteFile(final File file, final String description)
    {
        if (file.exists() == false)
        {
            operationLog.warn(String.format(
                    "Can not delete %s file '%s' because it does not exist.", description, file
                            .getAbsolutePath()));
            return;
        }
        final boolean deleted = file.delete();
        if (deleted == false)
        {
            operationLog.warn(String.format("Was not able to delete %s file '%s'.", description,
                    file.getAbsolutePath()));
        }
    }

    //
    // ITriggerable
    //

    public final void trigger()
    {
        final File markerFile = new File(DataMover.SHUTDOWN_PROCESS_MARKER_FILENAME);
        createMarkerFile(markerFile);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Datamover is shutting down.");
        }
        terminable.terminate();
        deleteFile(outgoingTargetLocationFile, "outgoing target location");
        deleteFile(markerFile, "marker");
        deleteFile(new File(DATAMOVER_PID_FILE_NAME), "Datamover pid file");
        exitHandler.exit(0);
    }
}