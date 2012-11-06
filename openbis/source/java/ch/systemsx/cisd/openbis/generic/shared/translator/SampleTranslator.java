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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Sample} &lt;---&gt; {@link SamplePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class SampleTranslator
{
    private SampleTranslator()
    {
        // Can not be instantiated.
    }

    public static List<Sample> translate(final List<SamplePE> samples, String baseIndexURL,
            Map<Long, Set<Metaproject>> metaprojects)
    {
        final List<Sample> list = new ArrayList<Sample>(samples.size());
        for (final SamplePE sample : samples)
        {
            list.add(translate(sample, baseIndexURL, metaprojects.get(sample.getId())));
        }
        return list;
    }

    public final static Sample translateWithoutRevealingData(SamplePE samplePE)
    {
        final Sample result = new Sample(true);

        result.setPermId(samplePE.getPermId());
        result.setId(HibernateUtils.getId(samplePE));
        result.setProperties(new ArrayList<IEntityProperty>());

        return result;
    }

    public final static Sample translate(final SamplePE samplePE, String baseIndexURL,
            Collection<Metaproject> metaprojects)
    {
        return translate(samplePE, baseIndexURL, true, false, metaprojects);
    }

    public final static Sample translate(final SamplePE samplePE, String baseIndexURL,
            final boolean withDetails, boolean withContainedSamples,
            Collection<Metaproject> metaprojects)
    {
        if (samplePE == null)
        {
            return null;
        }
        // we want at least one container/parent sample to be translated if it is loaded [LMS-1053]
        final int containerDep =
                getPositiveIntegerValue(samplePE.getSampleType().getContainerHierarchyDepth());
        final int generatedFromDep =
                getPositiveIntegerValue(samplePE.getSampleType().getGeneratedFromHierarchyDepth());
        return translate(samplePE, baseIndexURL, containerDep, generatedFromDep, withDetails,
                withContainedSamples, metaprojects);

    }

    private final static Sample translate(final SamplePE samplePE, String baseIndexURL,
            final int containerDep, final int generatedFromDep, final boolean withDetails,
            final boolean withContainedSamples, Collection<Metaproject> metaprojects)
    {
        final Sample result = new Sample();
        setCodes(result, samplePE);
        result.setPermId(samplePE.getPermId());
        result.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.SAMPLE,
                samplePE.getPermId()));
        result.setSearchlink(SearchlinkUtilities.createSearchlinkURL(baseIndexURL,
                EntityKind.SAMPLE, samplePE.getCode()));
        result.setModificationDate(samplePE.getModificationDate());
        result.setVersion(samplePE.getVersion());
        // NOTE: we should always translate Id in this way
        // because getId() on HibernateProxy object always returns null
        result.setId(HibernateUtils.getId(samplePE));
        result.setIdentifier(samplePE.getIdentifier());
        result.setSampleType(SampleTypeTranslator.translate(samplePE.getSampleType(),
                new HashMap<PropertyTypePE, PropertyType>()));
        if (withDetails)
        {
            result.setSpace(SpaceTranslator.translate(samplePE.getSpace()));
            result.setDatabaseInstance(DatabaseInstanceTranslator.translate(samplePE
                    .getDatabaseInstance()));
            result.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
            result.setModifier(PersonTranslator.translate(samplePE.getModifier()));
            result.setRegistrationDate(samplePE.getRegistrationDate());
            setProperties(result, samplePE);
            result.setExperiment(ExperimentTranslator.translate(samplePE.getExperiment(),
                    baseIndexURL, null, LoadableFields.PROPERTIES));
            List<Attachment> attachments;
            if (samplePE.attachmentsInitialized() == false)
            {
                attachments = DtoConverters.createUnmodifiableEmptyList();
            } else
            {
                attachments =
                        AttachmentTranslator.translate(samplePE.getAttachments(), baseIndexURL);
            }
            result.setAttachments(attachments);
        }
        if (containerDep > 0 && samplePE.getContainer() != null)
        {
            if (HibernateUtils.isInitialized(samplePE.getContainer()))
            {
                result.setContainer(translate(samplePE.getContainer(), baseIndexURL,
                        containerDep - 1, 0, false, false, null));
            }
        }
        if (generatedFromDep > 0 && samplePE.getParentRelationships() != null)
        {
            if (HibernateUtils.isInitialized(samplePE.getParentRelationships()))
            {
                for (SamplePE parent : samplePE.getParents())
                {
                    result.addParent(translate(parent, baseIndexURL, 0, generatedFromDep - 1,
                            false, false, null));
                }
            }
        }

        if (withContainedSamples && samplePE.getContained() != null)
        {
            ArrayList<Sample> containedSamples = new ArrayList<Sample>();
            for (SamplePE containedPE : samplePE.getContained())
            {
                Sample containedSample =
                        translate(containedPE, baseIndexURL, 0, 0, false, false, null);
                containedSamples.add(containedSample);

            }
            result.setContainedSample(containedSamples);
        }
        if (metaprojects != null)
        {
            result.setMetaprojects(metaprojects);
        }
        result.setDeletion(DeletionTranslator.translate(samplePE.getDeletion()));
        return result;
    }

    /** Sets both subcode and "full" code to {@link Sample} translating it from {@link SamplePE}. */
    public static void setCodes(Sample result, SamplePE samplePE)
    {
        result.setSubCode(IdentifierHelper.extractSubCode(samplePE));
        result.setCode(IdentifierHelper.extractCode(samplePE));
    }

    private static void setProperties(final Sample result, final SamplePE samplePE)
    {
        if (samplePE.isPropertiesInitialized())
        {
            result.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties(),
                    new HashMap<PropertyTypePE, PropertyType>()));
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

    public final static SampleParentWithDerived translate(
            final SampleParentWithDerivedDTO sampleGenerationDTO, String baseIndexURL,
            Collection<Metaproject> metaprojects)
    {
        final SampleParentWithDerived sampleGeneration = new SampleParentWithDerived();

        sampleGeneration.setParent(SampleTranslator.translate(sampleGenerationDTO.getParent(),
                baseIndexURL, metaprojects));

        final List<Sample> generated = new ArrayList<Sample>();
        for (SamplePE samplePE : sampleGenerationDTO.getDerived())
        {
            generated.add(SampleTranslator.translate(samplePE, baseIndexURL, false, false, null));
        }
        sampleGeneration.setDerived(generated.toArray(new Sample[generated.size()]));
        return sampleGeneration;
    }

    public final static int getPositiveIntegerValue(int integer)
    {
        return integer == 0 ? 1 : integer;
    }

}
