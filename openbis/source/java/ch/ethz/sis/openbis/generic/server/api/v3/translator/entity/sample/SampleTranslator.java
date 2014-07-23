package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
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
    protected void updateObject(SamplePE samplePe, Sample result)
    {
        if (getFetchOptions().hasExperiment())
        {
            Experiment experiment =
                    new ExperimentTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchExperiment())
                            .translate(samplePe
                                    .getExperiment());
            result.setExperiment(experiment);
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
            List<Sample> parents =
                    new ListTranslator().translate(samplePe.getParents(), new SampleTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchParents()));
            result.setParents(parents);
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

}
