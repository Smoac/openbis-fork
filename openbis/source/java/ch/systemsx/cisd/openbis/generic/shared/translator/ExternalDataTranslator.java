/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataTranslator
{
    private ExternalDataTranslator()
    {
    }

    public static List<ExternalData> translate(List<ExternalDataPE> list,
            String defaultDataStoreBaseURL, String baseIndexURL)
    {
        ArrayList<ExternalData> result = new ArrayList<ExternalData>(list.size());
        for (ExternalDataPE externalDataPE : list)
        {
            ExternalData data = translate(externalDataPE, defaultDataStoreBaseURL, baseIndexURL);
            result.add(data);
        }
        return result;
    }

    public static ExternalData translate(ExternalDataPE externalDataPE,
            String defaultDataStoreBaseURL, String baseIndexURL,
            final LoadableFields... withExperimentFields)
    {
        return translate(externalDataPE, defaultDataStoreBaseURL, baseIndexURL, false,
                withExperimentFields);
    }

    public static ExternalData translate(ExternalDataPE externalDataPE,
            String defaultDataStoreBaseURL, String baseIndexURL, boolean loadSampleProperties,
            final LoadableFields... withExperimentFields)
    {
        SamplePE sample = externalDataPE.getSample();
        DataPE parent = externalDataPE.tryGetParent();
        ExternalData externalData = new ExternalData();
        externalData.setId(HibernateUtils.getId(externalDataPE));
        externalData.setCode(StringEscapeUtils.escapeHtml(externalDataPE.getCode()));
        externalData.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        externalData.setDataProducerCode(StringEscapeUtils.escapeHtml(externalDataPE
                .getDataProducerCode()));
        externalData.setDataSetType(DataSetTypeTranslator.translate(
                externalDataPE.getDataSetType(), new HashMap<PropertyTypePE, PropertyType>()));
        externalData.setDerived(externalDataPE.isDerived());
        externalData
                .setFileFormatType(TypeTranslator.translate(externalDataPE.getFileFormatType()));
        externalData.setInvalidation(tryToGetInvalidation(sample));
        externalData.setLocation(StringEscapeUtils.escapeHtml(externalDataPE.getLocation()));
        externalData.setLocatorType(TypeTranslator.translate(externalDataPE.getLocatorType()));
        externalData
                .setParent(parent == null ? null : fillExternalData(new ExternalData(), parent));
        setChildren(externalDataPE, externalData);
        externalData.setProductionDate(externalDataPE.getProductionDate());
        externalData.setModificationDate(externalDataPE.getModificationDate());
        externalData.setRegistrationDate(externalDataPE.getRegistrationDate());
        externalData.setSample(sample == null ? null : fillSample(new Sample(), sample,
                loadSampleProperties));
        externalData.setDataStore(DataStoreTranslator.translate(externalDataPE.getDataStore(),
                defaultDataStoreBaseURL));
        externalData.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.DATA_SET, externalData.getIdentifier()));
        setProperties(externalDataPE, externalData);
        ExperimentPE experiment = externalDataPE.getExperiment();
        externalData.setExperiment(ExperimentTranslator.translate(experiment, baseIndexURL,
                withExperimentFields));
        return externalData;
    }

    private static void setProperties(ExternalDataPE externalDataPE, ExternalData externalData)
    {
        if (HibernateUtils.isInitialized(externalDataPE.getProperties()))
        {
            externalData.setDataSetProperties(EntityPropertyTranslator.translate(externalDataPE
                    .getProperties(), new HashMap<PropertyTypePE, PropertyType>()));
        } else
        {
            externalData.setDataSetProperties(new ArrayList<IEntityProperty>());
        }
    }

    private static Invalidation tryToGetInvalidation(SamplePE sample)
    {
        if (sample == null)
        {
            return null;
        }
        InvalidationPE invalidation = sample.getInvalidation();
        if (invalidation == null)
        {
            return null;
        }
        Invalidation result = new Invalidation();
        result.setReason(StringEscapeUtils.escapeHtml(invalidation.getReason()));
        result.setRegistrationDate(invalidation.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(invalidation.getRegistrator()));
        return result;
    }

    private static Sample fillSample(Sample sample, SamplePE samplePE, boolean loadSampleProperties)
    {
        sample.setId(HibernateUtils.getId(samplePE));
        SampleTranslator.setCodes(sample, samplePE);
        sample.setInvalidation(tryToGetInvalidation(samplePE));
        sample.setSampleType(TypeTranslator.translate(samplePE.getSampleType()));
        sample.setIdentifier(StringEscapeUtils
                .escapeHtml(samplePE.getSampleIdentifier().toString()));
        if (loadSampleProperties)
        {
            sample.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties(),
                    new HashMap<PropertyTypePE, PropertyType>()));
        }
        return sample;
    }

    private static void setChildren(ExternalDataPE externalDataPE, ExternalData externalData)
    {
        List<ExternalData> children = new ArrayList<ExternalData>();
        if (HibernateUtils.isInitialized(externalDataPE.getChildren()))
        {
            for (DataPE childPE : externalDataPE.getChildren())
            {
                children.add(fillExternalData(new ExternalData(), childPE));
            }
        }
        externalData.setChildren(children);
    }

    /**
     * Fills <var>externalData</var> from <var>data</vra> with all data needed by
     * {@link IEntityInformationHolder}.
     */
    private static ExternalData fillExternalData(ExternalData externalData, DataPE dataPE)
    {
        externalData.setId(HibernateUtils.getId(dataPE));
        externalData.setCode(StringEscapeUtils.escapeHtml(dataPE.getCode()));
        externalData.setDataSetType(DataSetTypeTranslator.translate(dataPE.getDataSetType(),
                new HashMap<PropertyTypePE, PropertyType>()));
        return externalData;
    }
}
