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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IActivitySensor;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;

/**
 * A {@link IActivitySensor} that senses changes in copy operations to a directory.
 * 
 * @author Bernd Rinn
 */
public final class RemoteDirectoryCopyActivitySensor extends AbstractCopyActivitySensor
{
    private final File target;

    public RemoteDirectoryCopyActivitySensor(File target)
    {
        super();
        this.target = target;
    }

    public RemoteDirectoryCopyActivitySensor(File target, int maxErrorsToIgnore)
    {
        super(maxErrorsToIgnore);
        this.target = target;
    }

    @Override
    protected StatusWithResult<Long> getTargetTimeOfLastActivityMoreRecentThan(long thresholdMillis)
    {
        try
        {
            final long lastChanged =
                    FileUtilities.lastChangedRelative(target, true, thresholdMillis);
            return StatusWithResult.create(lastChanged);
        } catch (UnknownLastChangedException ex)
        {
            return StatusWithResult.<Long>createError("Cannot determine time of last change of "
                    + getTargetDescription());
        }
    }

    @Override
    protected String getTargetDescription()
    {
        return String.format("target '%s'", target);
    }

}
