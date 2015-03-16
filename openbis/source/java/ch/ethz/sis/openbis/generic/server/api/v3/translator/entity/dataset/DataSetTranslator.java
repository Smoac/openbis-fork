/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.ExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.MaterialPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author Jakub Straszewski
 */
public class DataSetTranslator extends AbstractCachingTranslator<DataPE, DataSet, DataSetFetchOptions>
{

    public DataSetTranslator(TranslationContext translationContext, DataSetFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected boolean shouldTranslate(DataPE input)
    {
        return new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected DataSet createObject(DataPE dataPe)
    {
        final DataSet dataSet = new DataSet();
        dataSet.setCode(dataPe.getCode());
        dataSet.setPermId(new DataSetPermId(dataPe.getPermId()));
        dataSet.setAccessDate(dataPe.getAccessDate());
        dataSet.setDerived(dataPe.isDerived());
        dataSet.setPlaceholder(dataPe.isPlaceholder());
        dataSet.setModificationDate(dataPe.getModificationDate());
        dataSet.setRegistrationDate(dataPe.getRegistrationDate());
        dataSet.setFetchOptions(new DataSetFetchOptions());
        return dataSet;
    }

    @Override
    protected void updateObject(DataPE dataPe, DataSet result, Relations relations)
    {
        if (getFetchOptions().hasChildren())
        {
            List<DataSet> children =
                    new ListTranslator().translate(dataPe.getChildren(), new DataSetTranslator(getTranslationContext(),
                            getFetchOptions()
                                    .withChildren()));
            result.setChildren(children);
            result.getFetchOptions().withChildrenUsing(getFetchOptions().withChildren());
        }

        if (getFetchOptions().hasParents())
        {
            List<DataSet> parents =
                    new ListTranslator().translate(dataPe.getParents(), new DataSetTranslator(getTranslationContext(),
                            getFetchOptions()
                                    .withParents()));
            result.setParents(parents);
            result.getFetchOptions().withParentsUsing(getFetchOptions().withParents());
        }

        if (getFetchOptions().hasContained())
        {
            List<DataSet> contained =
                    new ListTranslator().translate(dataPe.getContainedDataSets(), new DataSetTranslator(getTranslationContext(),
                            getFetchOptions()
                                    .withContained()));
            result.setContained(contained);
            result.getFetchOptions().withContainedUsing(getFetchOptions().withContained());
        }

        if (getFetchOptions().hasContainers())
        {
            List<DataSet> containers =
                    new ListTranslator().translate(dataPe.getContainers(), new DataSetTranslator(getTranslationContext(),
                            getFetchOptions()
                                    .withContainers()));
            result.setContainers(containers);
            result.getFetchOptions().withContainersUsing(getFetchOptions().withContainers());
        }

        if (getFetchOptions().hasExperiment() && dataPe.getExperiment() != null)
        {
            Experiment experiment =
                    new ExperimentTranslator(getTranslationContext(), getFetchOptions().withExperiment())
                            .translate(dataPe.getExperiment());
            result.setExperiment(experiment);
            result.getFetchOptions().withExperimentUsing(getFetchOptions().withExperiment());
        }

        if (getFetchOptions().hasSample())
        {
            if (dataPe.tryGetSample() != null)
            {
                Sample sample =
                        new SampleTranslator(getTranslationContext(), getFetchOptions().withSample())
                                .translate(dataPe.tryGetSample());
                result.setSample(sample);
            }
            result.getFetchOptions().withSampleUsing(getFetchOptions().withSample());
        }

        if (getFetchOptions().hasProperties())
        {
            result.setProperties(new PropertyTranslator(getTranslationContext(), getFetchOptions().withProperties())
                    .translate(dataPe));
            result.getFetchOptions().withPropertiesUsing(getFetchOptions().withProperties());
        }

        if (getFetchOptions().hasMaterialProperties())
        {
            result.setMaterialProperties(new MaterialPropertyTranslator(getTranslationContext(), getFetchOptions().withMaterialProperties())
                    .translate(dataPe));
            result.getFetchOptions().withMaterialPropertiesUsing(getFetchOptions().withMaterialProperties());
        }

        if (getFetchOptions().hasType())
        {
            DataSetType dataSetType =
                    new DataSetTypeTranslator(getTranslationContext(),
                            getFetchOptions().withType()).translate(dataPe.getDataSetType());
            result.setType(dataSetType);
            result.getFetchOptions().withTypeUsing(getFetchOptions().withType());
        }

        if (getFetchOptions().hasTags())
        {
            List<Tag> tags =
                    new ListTranslator().translate(dataPe.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                            .withTags()));
            result.setTags(new HashSet<Tag>(tags));
            result.getFetchOptions().withTagsUsing(getFetchOptions().withTags());
        }

        if (getFetchOptions().hasRegistrator())
        {
            Person registrator =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().withRegistrator()).translate(dataPe.getRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().withRegistratorUsing(getFetchOptions().withRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            Person modifier =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().withModifier()).translate(dataPe.getModifier());
            result.setModifier(modifier);
            result.getFetchOptions().withModifierUsing(getFetchOptions().withModifier());
        }

        if (getFetchOptions().hasExternalData())
        {
            if (dataPe instanceof ExternalDataPE)
            {
                ExternalData externalData =
                        new ExternalDataTranslator(getTranslationContext(), getFetchOptions().withExternalData()).translate((ExternalDataPE) dataPe);
                result.setExternalData(externalData);
            }
            result.getFetchOptions().withExternalDataUsing(getFetchOptions().withExternalData());
        }
    }

}
