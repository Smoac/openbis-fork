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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.IExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.IHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.IMaterialPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.IPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.ISampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.ITagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class DataSetTranslator extends AbstractCachingTranslator<DataPE, DataSet, DataSetFetchOptions> implements IDataSetTranslator
{

    @Autowired
    private IExperimentTranslator experimentTranslator;

    @Autowired
    private ISampleTranslator sampleTranslator;

    @Autowired
    private IPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IDataSetTypeTranslator typeTranslator;

    @Autowired
    private ITagTranslator tagTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IExternalDataTranslator externalDataTranslator;

    @Autowired
    private IHistoryTranslator historyTranslator;

    @Override
    protected boolean shouldTranslate(TranslationContext context, DataPE input, DataSetFetchOptions fetchOptions)
    {
        return new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected DataSet createObject(TranslationContext context, DataPE dataPe, DataSetFetchOptions fetchOptions)
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
    protected void updateObject(TranslationContext context, DataPE dataPe, DataSet result, Relations relations, DataSetFetchOptions fetchOptions)
    {
        if (fetchOptions.hasChildren())
        {
            Map<DataPE, DataSet> children = translate(context, dataPe.getChildren(), fetchOptions.withChildren());
            result.setChildren(new ArrayList<DataSet>(children.values()));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasParents())
        {
            Map<DataPE, DataSet> parents = translate(context, dataPe.getParents(), fetchOptions.withParents());
            result.setParents(new ArrayList<DataSet>(parents.values()));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasContained())
        {
            Map<DataPE, DataSet> contained = translate(context, dataPe.getContainedDataSets(), fetchOptions.withContained());
            result.setContained(new ArrayList<DataSet>(contained.values()));
            result.getFetchOptions().withContainedUsing(fetchOptions.withContained());
        }

        if (fetchOptions.hasContainers())
        {
            Map<DataPE, DataSet> containers = translate(context, dataPe.getContainers(), fetchOptions.withContainers());
            result.setContainers(new ArrayList<DataSet>(containers.values()));
            result.getFetchOptions().withContainersUsing(fetchOptions.withContainers());
        }

        if (fetchOptions.hasExperiment())
        {
            if (dataPe.getExperiment() != null)
            {
                result.setExperiment(experimentTranslator.translate(context, dataPe.getExperiment(), fetchOptions.withExperiment()));
            }
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasSample())
        {
            if (dataPe.tryGetSample() != null)
            {
                result.setSample(sampleTranslator.translate(context, dataPe.tryGetSample(), fetchOptions.withSample()));
            }
            result.getFetchOptions().withSampleUsing(fetchOptions.withSample());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(propertyTranslator.translate(context, dataPe, fetchOptions.withProperties()));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(materialPropertyTranslator.translate(context, dataPe, fetchOptions.withMaterialProperties()));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasType())
        {
            result.setType(typeTranslator.translate(context, dataPe.getDataSetType(), fetchOptions.withType()));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasTags())
        {
            Map<MetaprojectPE, Tag> tags = tagTranslator.translate(context, dataPe.getMetaprojects(), fetchOptions.withTags());
            result.setTags(new HashSet<Tag>(tags.values()));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, dataPe.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(personTranslator.translate(context, dataPe.getModifier(), fetchOptions.withModifier()));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasExternalData())
        {
            if (dataPe instanceof ExternalDataPE)
            {
                result.setExternalData(externalDataTranslator.translate(context, (ExternalDataPE) dataPe, fetchOptions.withExternalData()));
            }
            result.getFetchOptions().withExternalDataUsing(fetchOptions.withExternalData());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(historyTranslator.translate(context, dataPe, fetchOptions.withHistory()));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }
    }

}
