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

package ch.systemsx.cisd.datamover;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.CompoundTerminable;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * The <i>DataMover</i> specific {@link CompoundTerminable} extension.
 * <p>
 * It try/catches each {@link #terminate(ITerminable)} call.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class DataMoverTerminable extends CompoundTerminable
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataMoverTerminable.class);

    DataMoverTerminable(final DataMoverProcess... dataMoverProcesses)
    {
        super(dataMoverProcesses);
    }

    //
    // CompoundTerminable
    //

    @Override
    protected final boolean terminate(final ITerminable terminable)
    {
        try
        {
            return super.terminate(terminable);
        } catch (final Exception e)
        {
            final DataMoverProcess process = (DataMoverProcess) terminable;
            operationLog.warn(String
                    .format("Terminating Datamover process '%s' threw an exception.", process
                            .getTaskName()), e);
            return false;
        }
    }
}
