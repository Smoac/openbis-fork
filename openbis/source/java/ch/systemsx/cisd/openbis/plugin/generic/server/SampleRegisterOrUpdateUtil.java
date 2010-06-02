/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Utility class for sample update or registration.
 * 
 * @author Izabela Adamczyk
 */
public class SampleRegisterOrUpdateUtil
{
    private static final String INSTANCE_SEPARATOR = "/";

    /**
     * Returns a list of samples that already exist and should be updated.
     */
    static List<NewSample> getSamplesToUpdate(NewSamplesWithTypes samples,
            List<Sample> existingSamples)
    {
        List<NewSample> samplesToUpdate = new ArrayList<NewSample>();
        for (NewSample ns : samples.getNewSamples())
        {
            for (Sample es : existingSamples)
            {
                if (isMatching(ns.getIdentifier(), es.getIdentifier()))
                {
                    samplesToUpdate.add(ns);
                }
            }
        }
        return samplesToUpdate;
    }

    /**
     * Creates {@link ListOrSearchSampleCriteria} narrowing listing result to samples given codes.
     */
    static ListOrSearchSampleCriteria createListSamplesByCodeCriteria(List<NewSample> samples)
    {
        List<String> codes = new ArrayList<String>();
        for (NewSample s : samples)
        {
            codes.add(extractCodeForSampleListingCriteria(s));
        }
        String[] codesAsArray = codes.toArray(new String[0]);
        ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(codesAsArray);
        return criteria;
    }

    /**
     * Checks if given identifiers are matching (db instance is not considered).
     */
    private static boolean isMatching(String i1, String i2)
    {
        if (i1 != null && i2 != null)
        {
            return dropDatabaseInstance(i1).equals(dropDatabaseInstance(i2));
        } else
        {
            return i1 == i2;
        }
    }

    private static String dropDatabaseInstance(String id)
    {
        assert id != null;
        assert id.contains(INSTANCE_SEPARATOR);
        if (id.startsWith(INSTANCE_SEPARATOR))
        {
            return id;
        } else
        {
            return id.substring(id.indexOf(INSTANCE_SEPARATOR));
        }

    }

    private static String extractCodeForSampleListingCriteria(NewSample s)
    {
        SampleIdentifier parsedIdentifier = SampleIdentifierFactory.parse(s.getIdentifier());
        String subcode = parsedIdentifier.getSampleSubCode();
        String code = parsedIdentifier.getSampleCode();
        return StringUtils.isBlank(subcode) ? code : subcode;
    }
}
