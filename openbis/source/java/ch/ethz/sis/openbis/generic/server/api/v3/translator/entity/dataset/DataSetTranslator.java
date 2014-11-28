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
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Jakub Straszewski
 */
public class DataSetTranslator extends AbstractCachingTranslator<DataPE, DataSet, DataSetFetchOptions>
{
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public DataSetTranslator(TranslationContext translationContext, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
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
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchChildren()));
            result.setChildren(children);
            result.getFetchOptions().fetchChildren(getFetchOptions().fetchChildren());
        }

        if (getFetchOptions().hasParents())
        {
            List<DataSet> parents =
                    new ListTranslator().translate(dataPe.getParents(), new DataSetTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchParents()));
            result.setParents(parents);
            result.getFetchOptions().fetchParents(getFetchOptions().fetchParents());
        }

        if (getFetchOptions().hasContained())
        {
            List<DataSet> contained =
                    new ListTranslator().translate(dataPe.getContainedDataSets(), new DataSetTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchContained()));
            result.setContained(contained);
            result.getFetchOptions().fetchContained(getFetchOptions().fetchContained());
        }

        if (getFetchOptions().hasContainers())
        {
            List<DataSet> containers =
                    new ListTranslator().translate(dataPe.getContainers(), new DataSetTranslator(getTranslationContext(),
                            managedPropertyEvaluatorFactory, getFetchOptions()
                                    .fetchContainers()));
            result.setContainers(containers);
            result.getFetchOptions().fetchContainers(getFetchOptions().fetchContainers());
        }

        if (getFetchOptions().hasExperiment())
        {
            Experiment experiment =
                    new ExperimentTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchExperiment())
                            .translate(dataPe.getExperiment());
            result.setExperiment(experiment);
            result.getFetchOptions().fetchExperiment(getFetchOptions().fetchExperiment());
        }

        if (getFetchOptions().hasProperties())
        {
            result.setProperties(new PropertyTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchProperties())
                    .translate(dataPe));
            result.getFetchOptions().fetchProperties(getFetchOptions().fetchProperties());
        }

        if (getFetchOptions().hasType())
        {
            DataSetType dataSetType =
                    new DataSetTypeTranslator(getTranslationContext(),
                            getFetchOptions().fetchType()).translate(dataPe.getDataSetType());
            result.setType(dataSetType);
            result.getFetchOptions().fetchType(getFetchOptions().fetchType());
        }

        if (getFetchOptions().hasTags())
        {
            List<Tag> tags =
                    new ListTranslator().translate(dataPe.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                            .fetchTags()));
            result.setTags(new HashSet<Tag>(tags));
            result.getFetchOptions().fetchTags(getFetchOptions().fetchTags());
        }

        if (getFetchOptions().hasRegistrator())
        {
            Person registrator =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().fetchRegistrator()).translate(dataPe.getRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().fetchRegistrator(getFetchOptions().fetchRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            Person modifier =
                    new PersonTranslator(getTranslationContext(), getFetchOptions().fetchModifier()).translate(dataPe.getModifier());
            result.setModifier(modifier);
            result.getFetchOptions().fetchModifier(getFetchOptions().fetchModifier());
        }
    }

}
