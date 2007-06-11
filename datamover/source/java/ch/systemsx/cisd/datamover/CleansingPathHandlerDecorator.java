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

import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;

/**
 * A class that decorates an {@link IPathHandler} with a selective cleansing task. The files that should be removed in
 * the cleansing step are selected by a {@link FileFilter}. Note: if a <var>path</var> that should be handled itself
 * is accepted by the <code>FileFilter</code>, the complete directory is removed and the decorated handler will never
 * be called on this path.
 * 
 * @author Bernd Rinn
 */
public class CleansingPathHandlerDecorator implements IPathHandler
{

    private final FileFilter filter;

    private final IPathHandler decoratedHandler;

    public CleansingPathHandlerDecorator(FileFilter filter, IPathHandler decoratedHandler)
    {
        assert filter != null;
        assert decoratedHandler != null;

        this.filter = filter;
        this.decoratedHandler = decoratedHandler;
    }

    public boolean handle(File path)
    {
        assert path != null;

        final boolean pathDeleted = FileUtilities.deleteRecursively(path, filter);
        if (pathDeleted == false)
        {
            return decoratedHandler.handle(path);
        } else
        {
            return true;
        }
    }

}
