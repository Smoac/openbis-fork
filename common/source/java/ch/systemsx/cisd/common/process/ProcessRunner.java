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

package ch.systemsx.cisd.common.process;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.StopException;

/**
 * This class takes cares of running encapsulated {@link IProcess}.
 * 
 * @author Christian Ribeaud
 */
public final class ProcessRunner
{

    public ProcessRunner(final IProcess process)
    {
        runProcess(process);
    }

    final static void runProcess(final IProcess process)
    {
        final int maxRetryOnFailure = process.getMaxRetryOnFailure();
        final long millisToSleepOnFailure = process.getMillisToSleepOnFailure();
        assert millisToSleepOnFailure > -1;
        int counter = 0;
        do
        {
            StopException.check();
            process.run();
            if (counter > 0 && millisToSleepOnFailure > 0)
            {
                try
                {
                    Thread.sleep(millisToSleepOnFailure);
                } catch (InterruptedException ex)
                {
                    throw new CheckedExceptionTunnel(ex);
                }
            }
        } while (++counter < maxRetryOnFailure && process.succeeded() == false);
    }
}
