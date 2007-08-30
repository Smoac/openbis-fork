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

import ch.systemsx.cisd.datamover.intf.IReadPathOperations;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A {@link FileFilter} that picks all entries that haven't been changed for longer than some given quiet period.
 * 
 * @author Bernd Rinn
 */
public class QuietPeriodFileFilter implements FileFilter
{

    private final long quietPeriodMillis;

    private final IReadPathOperations readOperations;

    /**
     * Creates a <var>QuietPeriodFileFilter</var>.
     * 
     * @param timingParameters The timing paramter object to get the quiet period from.
     * @param readOperations Used to check when a pathname was changed.
     */
    public QuietPeriodFileFilter(ITimingParameters timingParameters, IReadPathOperations readOperations)
    {
        assert timingParameters != null;
        assert readOperations != null;

        this.quietPeriodMillis = timingParameters.getQuietPeriodMillis();
        this.readOperations = readOperations;

        assert quietPeriodMillis > 0;
    }

    public boolean accept(File pathname)
    {
        return (System.currentTimeMillis() - readOperations.lastChanged(pathname)) > quietPeriodMillis;
    }

}
