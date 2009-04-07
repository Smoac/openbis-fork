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

package ch.systemsx.cisd.etlserver;

import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which transports through Web Service any
 * information we would like to know about a procedure type.
 * 
 * @author Christian Ribeaud
 */
public final class ProcedureType
{
    private String code;
    private boolean dataAcquisition;

    public ProcedureType()
    {
    }

    public final String getCode()
    {
        return code;
    }

    public final void setCode(String code)
    {
        this.code = code;
    }

    public ProcedureType(final String code)
    {
        ProcedureTypeCode procedureTypeCode = ProcedureTypeCode.getProcedureTypeCode(code);
        setCode(procedureTypeCode.getCode());
        setDataAcquisition(procedureTypeCode == ProcedureTypeCode.DATA_ACQUISITION);
    }

    /**
     * Returns <code>true</code> if and only if this procedure type represents a data acquisition
     * (or measurement) rather than a data processing (or derivation) step in a workflow.
     */
    public final boolean isDataAcquisition()
    {
        return dataAcquisition;
    }

    /**
     * Sets the attribute which determines whether this procedure type represents a data acquisition
     * (or measurement) rather than a data processing (or derivation) step in a workflow.
     */
    public final void setDataAcquisition(final boolean dataAcquisition)
    {
        this.dataAcquisition = dataAcquisition;
    }
}
