/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation;

/**
 * @author pkupczyk
 */
public enum OperationExecutionAvailability
{

    AVAILABLE, DELETE_PENDING(AVAILABLE), DELETED(AVAILABLE, DELETE_PENDING), TIME_OUT_PENDING(AVAILABLE), TIMED_OUT(AVAILABLE, TIME_OUT_PENDING);

    private OperationExecutionAvailability[] previous;

    OperationExecutionAvailability(OperationExecutionAvailability... previous)
    {
        this.previous = previous;
    }

    public boolean hasPrevious(OperationExecutionAvailability availability)
    {
        if (previous != null)
        {
            for (OperationExecutionAvailability aPrevious : previous)
            {
                if (availability.equals(aPrevious))
                {
                    return true;
                }
            }
        }

        return false;
    }

}
