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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;

/**
 * Business object of a procedure. Holds an instance of {@link ProcedurePE}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IProcedureBO extends IBusinessObject
{
    /**
     * Returns the hold procedure.
     * 
     * @return <code>null</code> if undefined.
     */
    public ProcedurePE getProcedure();

    /**
     * Defines a new procedure of specified type for the specified experiment. After invocation of
     * this method {@link IBusinessObject#save()} should be invoked to store the new procedure in
     * the Data Access Layer.
     * 
     * @throws UserFailureException if <code>procedureTypeCode</code> is not known.
     */
    public void define(ExperimentPE experiment, String procedureTypeCode);
}
