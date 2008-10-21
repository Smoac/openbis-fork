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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class SampleTranslator
{
    private SampleTranslator()
    {
    }

    public static Sample translate(SamplePE samplePE, SampleType st,
            Map<SampleIdentifier, List<SamplePropertyPE>> samplesProperties)
    {
        if (samplePE == null)
        {
            return null;
        }
        int containerDep = samplePE.getSampleType().getContainerHierarchyDepth();
        int generatedFromDep = samplePE.getSampleType().getGeneratedFromHierarchyDepth();
        return translate(samplePE, containerDep, generatedFromDep, st, true, samplesProperties);

    }

    private static Sample translate(SamplePE samplePE, int containerDep, int generatedFromDep,
            SampleType st, boolean withDetails,
            Map<SampleIdentifier, List<SamplePropertyPE>> samplesProperties)
    {
        final Sample result = new Sample();
        result.setCode(samplePE.getCode());
        result.setIdentifier(samplePE.getSampleIdentifier().toString());
        if (withDetails)
        {
            result.setSampleType(st);
            result.setGroup(GroupTranslator.translate(samplePE.getGroup()));
            result.setDatabaseInstance(DatabaseInstanceTranslator.translate(samplePE
                    .getDatabaseInstance()));
            result.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
            result.setRegistrationDate(samplePE.getRegistrationDate());
            final List<SamplePropertyPE> list =
                    samplesProperties.get(samplePE.getSampleIdentifier());
            result.setProperties(list == null ? new ArrayList<SampleProperty>()
                    : SamplePropertyTranslator.translate(list, st));
            result.setValidProcedure(ProcedureTranslator.translate(samplePE.getValidProcedure()));
        }
        if (containerDep > 0 && samplePE.getContainer() != null)
        {
            result.setContainer(SampleTranslator.translate(samplePE.getContainer(),
                    containerDep - 1, 0, null, false, null));
        }
        if (generatedFromDep > 0 && samplePE.getGeneratedFrom() != null)
        {
            result.setGeneratedFrom(SampleTranslator.translate(samplePE.getGeneratedFrom(), 0,
                    generatedFromDep - 1, null, false, null));
        }
        result.setInvalid(samplePE.getInvalidation() != null);
        return result;
    }

}
