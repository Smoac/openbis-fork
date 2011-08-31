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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

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
                if (isMatching(ns, es))
                {
                    samplesToUpdate.add(ns);
                }
            }
        }
        return samplesToUpdate;
    }

    private static boolean isMatching(NewSample newSample, Sample existingSample)
    {
        SampleIdentifier newSampleIdentifier = SampleIdentifierFactory.parse(newSample);
        if (isMatchingIgnoringContainer(existingSample, newSampleIdentifier) == false)
        {
            return false;
        }

        // is container matching?
        SampleIdentifier containerIdentifier =
                tryCreateContainerIdentifier(newSample, newSampleIdentifier);
        Sample containerSample = existingSample.getContainer();
        return isMatchingIgnoringContainer(containerSample, containerIdentifier);
    }

    private static SampleIdentifier tryCreateContainerIdentifier(NewSample newSample,
            SampleIdentifier newSampleIdentifier)
    {
        String newSampleContainerCode = newSampleIdentifier.tryGetContainerCode();
        String newSampleContainerSpace = newSample.getDefaultSpaceIdentifier();
        if (newSampleContainerCode == null && newSample.getContainerIdentifier() != null)
        {
            SampleIdentifier newSampleContainerIdentifier =
                    SampleIdentifierFactory.parse(newSample.getContainerIdentifier(),
                            newSample.getDefaultSpaceIdentifier());
            newSampleContainerCode = newSampleContainerIdentifier.getSampleSubCode();
            newSampleContainerSpace = tryGetSpaceCode(newSampleContainerIdentifier);
        }
        if (newSampleContainerCode == null)
        {
            return null;
        } else
        {
            return new SampleIdentifier(new SpaceIdentifier(newSampleContainerSpace),
                    newSampleContainerCode);
        }
    }

    private static boolean isMatchingIgnoringContainer(Sample existingSample,
            SampleIdentifier newSampleIdentifier)
    {
        if (newSampleIdentifier == null || existingSample == null)
        {
            if (newSampleIdentifier != null || existingSample != null)
            {
                return false; // only one is null
            }
        }
        if (existingSample == null)
        {
            assert newSampleIdentifier == null : "newSampleIdentifier is not null";
            return true;
        }
        assert newSampleIdentifier != null : "newSampleIdentifier is null";

        String newSampleSpace = tryGetSpaceCode(newSampleIdentifier);
        String existingSampleSpace = existingSample.getSpace().getCode();
        if (existingSampleSpace.equalsIgnoreCase(newSampleSpace) == false)
        {
            return false;
        }

        String newSampleSubCode = newSampleIdentifier.getSampleSubCode();
        if (existingSample.getSubCode().equalsIgnoreCase(newSampleSubCode) == false)
        {
            return false;
        }
        return true;
    }

    private static String tryGetSpaceCode(SampleIdentifier sampleIdentifier)
    {
        if (sampleIdentifier.isSpaceLevel())
        {
            String space = sampleIdentifier.getSpaceLevel().getSpaceCode();
            if (space.startsWith(INSTANCE_SEPARATOR))
            {
                space = space.substring(1);
            }
            return space;
        } else
        {
            return null;
        }
    }

    /**
     * Creates {@link ListOrSearchSampleCriteria} narrowing listing result to samples given codes.
     */
    static ListOrSearchSampleCriteria createListSamplesByCodeCriteria(List<String> codes)
    {
        String[] codesAsArray = codes.toArray(new String[0]);
        ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(codesAsArray, false);
        return criteria;
    }

    private static String dropDatabaseInstance(String id)
    {
        assert id != null;
        if (id.contains(INSTANCE_SEPARATOR) == false || id.startsWith(INSTANCE_SEPARATOR))
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
