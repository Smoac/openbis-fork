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
import java.io.FileFilter;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;

/**
 * A class that decorates an {@link IPathHandler} with a selective gate. If a path is picked up by a {@link FileFilter},
 * the first handler is chosen to handle the path, if it doesn't, the second one. The gate relates only to the top-level
 * path (may it be a file or a directory), not to any sub-directories.
 * 
 * @author Bernd Rinn
 */
public class GatePathHandlerDecorator implements IPathHandler
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GatePathHandlerDecorator.class);

    private final FileFilter filter;

    private final IPathHandler defaultHandler;

    private final IPathHandler filteredHandler;

    public GatePathHandlerDecorator(FileFilter filter, IPathHandler defaultHandler, IPathHandler filteredHandler)
    {
        assert filter != null;
        assert defaultHandler != null;
        assert filteredHandler != null;

        this.filter = filter;
        this.defaultHandler = defaultHandler;
        this.filteredHandler = filteredHandler;
    }

    public boolean handle(File path)
    {
        assert path != null;

        final String absolutePath = path.getAbsolutePath();
        final boolean filtered = filter.accept(path);
        if (filtered)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("FILTERED %s [created: %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS]",
                        absolutePath, new Date(path.lastModified())));
            }
            return filteredHandler.handle(path);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("DEFAULT %s [created: %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS]",
                        absolutePath, new Date(path.lastModified())));
            }
            return defaultHandler.handle(path);
        }
    }

}
