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
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
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
    private static final String CODE_SEPARATOR = ":";

    private static final String INSTANCE_SEPARATOR = "/";

    /**
     * Returns a list of samples that already exist and should be updated.
     */
    static List<NewSample> getSamplesToUpdate(List<NewSample> samples, List<Sample> existingSamples)
    {
        List<NewSample> samplesToUpdate = new ArrayList<NewSample>();
        for (NewSample ns : samples)
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
    static ListOrSearchSampleCriteria createListSamplesByCodeCriteria(List<String> codes)
    {
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
            return normalize(i1).equals(normalize(i2));
        } else
        {
            return i1 == i2;
        }
    }

    private static String normalize(String id)
    {
        return dropDatabaseInstance(id).toUpperCase();
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

    private static String extractCode(String id)
    {
        assert id != null;
        if (id.contains(CODE_SEPARATOR))
        {
            return id.substring(0, id.indexOf(CODE_SEPARATOR));
        } else
        {
            return id;
        }
    }

    /**
     * If <var>withContainers</var> is true, containers codes are extracted, otherwise - sample
     * codes.
     */
    public static List<String> extractCodes(List<NewSample> newSamples, boolean withContainers)
    {
        HashSet<String> set = new HashSet<String>();
        for (NewSample s : newSamples)
        {
            String identifierWithoutInstance = dropDatabaseInstance(s.getIdentifier());
            boolean hasContainer = identifierWithoutInstance.contains(CODE_SEPARATOR);
            if (hasContainer == withContainers)
            {
                SampleIdentifier parsed = SampleIdentifierFactory.parse(s.getIdentifier());
                set.add(extractCode(parsed.getSampleCode()));
            }
        }
        return new ArrayList<String>(set);
    }
}
