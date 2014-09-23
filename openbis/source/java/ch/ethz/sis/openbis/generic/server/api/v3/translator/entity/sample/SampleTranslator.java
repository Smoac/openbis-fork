package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ToManyRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ToOneRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.AttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.ExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.SpaceTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

public class SampleTranslator extends AbstractCachingTranslator<SamplePE, Sample, SampleFetchOptions>
{

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public SampleTranslator(TranslationContext translationContext, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            SampleFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    protected boolean shouldTranslate(SamplePE input)
    {
        return new SampleByIdentiferValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Sample createObject(SamplePE samplePe)
    {
        final Sample sample = new Sample();
        sample.setPermId(new SamplePermId(samplePe.getPermId()));
        sample.setIdentifier(new SampleIdentifier(samplePe.getIdentifier()));
        sample.setCode(samplePe.getCode());
        sample.setModificationDate(samplePe.getModificationDate());
        sample.setRegistrationDate(samplePe.getRegistrationDate());
        sample.setFetchOptions(new SampleFetchOptions());

        return sample;
    }

    @Override
    protected Relations getObjectsRelations(final Collection<SamplePE> samples)
    {
        Collection<Long> sampleIds = new LinkedList<Long>();
        for (SamplePE sample : samples)
        {
            sampleIds.add(sample.getId());
        }

        Relations relations = new Relations();

        if (getFetchOptions().hasExperiment())
        {
            relations.add(new SampleExperimentRelation(samples));
        }

        if (getFetchOptions().hasParents())
        {
            relations.add(new SampleParentsRelation(samples));
        }

        return relations;
    }

    @Override
    protected void updateObject(SamplePE samplePe, Sample result, Relations relations)
    {
        if (getFetchOptions().hasExperiment())
        {
            result.setExperiment(relations.get(SampleExperimentRelation.class).getTranslated(samplePe));
            result.getFetchOptions().fetchExperiment(getFetchOptions().fetchExperiment());
        }

        if (getFetchOptions().hasSpace())
        {
            Space space = new SpaceTranslator(getTranslationContext(), getFetchOptions().fetchSpace()).translate(samplePe
                    .getSpace());
            result.setSpace(space);
            result.getFetchOptions().fetchSpace(getFetchOptions().fetchSpace());
        }

        if (getFetchOptions().hasProperties())
        {
            result.setProperties(new PropertyTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchProperties())
                    .translate(samplePe));
            result.getFetchOptions().fetchProperties(getFetchOptions().fetchProperties());
        }

        if (getFetchOptions().hasParents())
        {
            result.setParents(relations.get(SampleParentsRelation.class).getTranslatedList(samplePe));
            result.getFetchOptions().fetchParents(getFetchOptions().fetchParents());
        }

        if (getFetchOptions().hasChildren())
        {
            List<Sample> children =
                    new ListTranslator().translate(samplePe.getChildren(), new SampleTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchChildren()));
            result.setChildren(children);
            result.getFetchOptions().fetchChildren(getFetchOptions().fetchChildren());
        }

        if (getFetchOptions().hasContainer())
        {
            Sample container =
                    new SampleTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchContainer())
                            .translate(samplePe.getContainer());
            result.setContainer(container);
            result.getFetchOptions().fetchContainer(getFetchOptions().fetchContainer());
        }

        if (getFetchOptions().hasContained())
        {
            List<Sample> contained =
                    new ListTranslator().translate(samplePe.getContained(), new SampleTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchContained()));
            result.setContained(contained);
            result.getFetchOptions().fetchContained(getFetchOptions().fetchContained());
        }

        if (getFetchOptions().hasSampleType())
        {
            SampleType sampleType =
                    new SampleTypeTranslator(getTranslationContext(), getFetchOptions().fetchSampleType()).translate(samplePe.getSampleType());
            result.setSampleType(sampleType);
            result.getFetchOptions().fetchSampleType(getFetchOptions().fetchSampleType());
        }

        if (getFetchOptions().hasTags())
        {
            List<Tag> tags =
                    new ListTranslator().translate(samplePe.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                            .fetchTags()));
            result.setTags(new HashSet<Tag>(tags));
            result.getFetchOptions().fetchTags(getFetchOptions().fetchTags());
        }

        if (getFetchOptions().hasRegistrator())
        {
            Person registrator =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().fetchRegistrator()).translate(samplePe.getRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().fetchRegistrator(getFetchOptions().fetchRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            Person modifier =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().fetchModifier()).translate(samplePe.getModifier());
            result.setModifier(modifier);
            result.getFetchOptions().fetchModifier(getFetchOptions().fetchModifier());
        }

        if (getFetchOptions().hasAttachments())
        {
            ArrayList<Attachment> attachments =
                    AttachmentTranslator.translate(getTranslationContext(), samplePe, getFetchOptions().fetchAttachments());
            result.setAttachments(attachments);
            result.getFetchOptions().fetchAttachments(getFetchOptions().fetchAttachments());
        }
    }

    private class SampleExperimentRelation extends ToOneRelation<SamplePE, String, ExperimentPE, Experiment>
    {

        private Collection<SamplePE> samples;

        public SampleExperimentRelation(Collection<SamplePE> samples)
        {
            this.samples = samples;
        }

        @Override
        protected Map<SamplePE, ExperimentPE> getOriginalMap()
        {
            // TODO replace with one database call
            Map<SamplePE, ExperimentPE> map = new HashMap<SamplePE, ExperimentPE>();
            for (SamplePE sample : samples)
            {
                map.put(sample, sample.getExperiment());
            }
            return map;
        }

        @Override
        protected Collection<Experiment> getTranslatedCollection(Collection<ExperimentPE> originalCollection)
        {
            return new ExperimentTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions()
                    .fetchExperiment()).translate(originalCollection);
        }

        @Override
        protected String getOriginalId(ExperimentPE original)
        {
            return original.getIdentifier();
        }

        @Override
        protected String getTranslatedId(Experiment translated)
        {
            return translated.getIdentifier().getIdentifier();
        }

    }

    private class SampleParentsRelation extends ToManyRelation<SamplePE, String, SamplePE, Sample>
    {

        private Collection<SamplePE> samples;

        public SampleParentsRelation(Collection<SamplePE> samples)
        {
            this.samples = samples;
        }

        @Override
        protected Map<SamplePE, Collection<SamplePE>> getOriginalMap()
        {
            // TODO replace with one database call
            Map<SamplePE, Collection<SamplePE>> map = new HashMap<SamplePE, Collection<SamplePE>>();
            for (SamplePE sample : samples)
            {
                List<SamplePE> parents = new ArrayList<SamplePE>(sample.getParents());
                // return parents in the same order as they were created
                Collections.sort(parents, new Comparator<SamplePE>()
                    {
                        @Override
                        public int compare(SamplePE s1, SamplePE s2)
                        {
                            return s1.getId().compareTo(s2.getId());
                        }
                    });
                map.put(sample, parents);
            }
            return map;
        }

        @Override
        protected Collection<Sample> getTranslatedCollection(Collection<SamplePE> originalCollection)
        {
            return new SampleTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions()
                    .fetchParents()).translate(originalCollection);
        }

        @Override
        protected String getOriginalId(SamplePE original)
        {
            return original.getIdentifier();
        }

        @Override
        protected String getTranslatedId(Sample translated)
        {
            return translated.getIdentifier().getIdentifier();
        }

    }

}
