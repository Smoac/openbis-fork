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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProcedureType;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;

/**
 * A {@link Person} &lt;---&gt; {@link PersonPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ProcedureTranslator
{

    private ProcedureTranslator()
    {
        // Can not be instantiated.
    }

    public final static Procedure translate(final ProcedurePE procedure,
            final LoadableFields... withExperimentFields)
    {
        if (procedure == null)
        {
            return null;
        }
        final Procedure result = new Procedure();
        result.setExperiment(ExperimentTranslator.translate(procedure.getExperiment(),
                withExperimentFields));
        result.setProcedureType(translate(procedure.getProcedureType()));
        result.setRegistrationDate(procedure.getRegistrationDate());
        return result;
    }

    private final static ProcedureType translate(final ProcedureTypePE procedureType)
    {
        final ProcedureType result = new ProcedureType();
        result.setCode(StringEscapeUtils.escapeHtml(procedureType.getCode()));
        result.setDataAcquisition(procedureType.isDataAcquisition());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(procedureType
                .getDatabaseInstance()));
        result.setDescription(StringEscapeUtils.escapeHtml(procedureType.getDescription()));
        return result;
    }

}
