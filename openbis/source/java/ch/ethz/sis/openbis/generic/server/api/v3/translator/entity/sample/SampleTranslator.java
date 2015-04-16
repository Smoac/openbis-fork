package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ToManyRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ToOneRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.IAttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.IDataSetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.IExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.IHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.IMaterialPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.IPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.ISpaceTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.ITagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

@Component
public class SampleTranslator extends AbstractCachingTranslator<SamplePE, Sample, SampleFetchOptions> implements ISampleTranslator
{

    @Autowired
    private ISpaceTranslator spaceTranslator;

    @Autowired
    private IExperimentTranslator experimentTranslator;

    @Autowired
    private IAttachmentTranslator attachmentTranslator;

    @Autowired
    private IPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private ISampleTypeTranslator typeTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private ITagTranslator tagTranslator;

    @Autowired
    private IDataSetTranslator dataSetTranslator;

    @Autowired
    private IHistoryTranslator historyTranslator;

    @Override
    protected boolean shouldTranslate(TranslationContext context, SamplePE input, SampleFetchOptions fetchOptions)
    {
        return new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected Sample createObject(TranslationContext context, SamplePE samplePe, SampleFetchOptions fetchOptions)
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
    protected Relations getObjectsRelations(TranslationContext context, final Collection<SamplePE> samples, SampleFetchOptions fetchOptions)
    {
        Relations relations = new Relations();

        if (fetchOptions.hasExperiment())
        {
            relations.add(new SampleExperimentRelation(context, samples, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasParents())
        {
            relations.add(new SampleParentsRelation(context, samples, fetchOptions.withParents()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, SamplePE samplePe, Sample result, Relations relations, SampleFetchOptions fetchOptions)
    {
        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(SampleExperimentRelation.class).getTranslated(samplePe));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasSpace())
        {
            result.setSpace(spaceTranslator.translate(context, samplePe.getSpace(), fetchOptions.withSpace()));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(propertyTranslator.translate(context, samplePe, fetchOptions.withProperties()));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(materialPropertyTranslator.translate(context, samplePe, fetchOptions.withMaterialProperties()));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasParents())
        {
            result.setParents(relations.get(SampleParentsRelation.class).getTranslatedList(samplePe));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            Map<SamplePE, Sample> children = translate(context, samplePe.getChildren(), fetchOptions.withChildren());
            result.setChildren(new ArrayList<Sample>(children.values()));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasContainer())
        {
            result.setContainer(translate(context, samplePe.getContainer(), fetchOptions.withContainer()));
            result.getFetchOptions().withContainerUsing(fetchOptions.withContainer());
        }

        if (fetchOptions.hasContained())
        {
            Map<SamplePE, Sample> contained = translate(context, samplePe.getContained(), fetchOptions.withContained());
            result.setContained(new ArrayList<Sample>(contained.values()));
            result.getFetchOptions().withContainedUsing(fetchOptions.withContained());
        }

        if (fetchOptions.hasDataSets())
        {
            Map<DataPE, DataSet> dataSets = dataSetTranslator.translate(context, samplePe.getDatasets(), fetchOptions.withDataSets());
            result.setDataSets(new ArrayList<DataSet>(dataSets.values()));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasType())
        {
            result.setType(typeTranslator.translate(context, samplePe.getSampleType(), fetchOptions.withType()));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasTags())
        {
            Map<MetaprojectPE, Tag> tags = tagTranslator.translate(context, samplePe.getMetaprojects(), fetchOptions.withTags());
            result.setTags(new HashSet<Tag>(tags.values()));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, samplePe.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(personTranslator.translate(context, samplePe.getModifier(), fetchOptions.withModifier()));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments(attachmentTranslator.translate(context, samplePe, fetchOptions.withAttachments()));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(historyTranslator.translate(context, samplePe, fetchOptions.withHistory()));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }
    }

    private class SampleExperimentRelation extends ToOneRelation<SamplePE, ExperimentPE, Experiment, ExperimentFetchOptions>
    {

        private Collection<SamplePE> samples;

        public SampleExperimentRelation(TranslationContext context, Collection<SamplePE> samples, ExperimentFetchOptions fetchOptions)
        {
            super(context, fetchOptions);
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
        protected Map<ExperimentPE, Experiment> getTranslatedMap(TranslationContext context, Collection<ExperimentPE> originalCollection,
                ExperimentFetchOptions fetchOptions)
        {
            return experimentTranslator.translate(context, originalCollection, fetchOptions);
        }
    }

    private class SampleParentsRelation extends ToManyRelation<SamplePE, SamplePE, Sample, SampleFetchOptions>
    {

        private Collection<SamplePE> samples;

        public SampleParentsRelation(TranslationContext context, Collection<SamplePE> samples, SampleFetchOptions fetchOptions)
        {
            super(context, fetchOptions);
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
        protected Map<SamplePE, Sample> getTranslatedMap(TranslationContext context, Collection<SamplePE> originalCollection,
                SampleFetchOptions fetchOptions)
        {
            return translate(context, originalCollection, fetchOptions);
        }

    }

}
