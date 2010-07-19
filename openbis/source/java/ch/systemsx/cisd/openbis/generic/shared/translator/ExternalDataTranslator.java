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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
            ExternalData data =
                    translate(externalDataPE, defaultDataStoreBaseURL, baseIndexURL, true,
                            ExperimentTranslator.LoadableFields.PROPERTIES);
            result.add(data);
        }
        return result;
    }

    public static ExternalData translate(ExternalDataPE externalDataPE,
            String defaultDataStoreBaseURL, String baseIndexURL,
            final LoadableFields... withExperimentFields)
    {
        return translate(externalDataPE, defaultDataStoreBaseURL, baseIndexURL, true,
                withExperimentFields);
    }

    public static ExternalData translate(ExternalDataPE externalDataPE,
            String defaultDataStoreBaseURL, String baseIndexURL, boolean loadSampleProperties,
            final LoadableFields... withExperimentFields)
    {
        SamplePE sampleOrNull = externalDataPE.tryGetSample();
        ExperimentPE experiment = externalDataPE.getExperiment();
        ExternalData externalData = new ExternalData();
        externalData.setId(HibernateUtils.getId(externalDataPE));
        externalData.setCode(StringEscapeUtils.escapeHtml(externalDataPE.getCode()));
        externalData.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        externalData.setStatus(externalDataPE.getStatus());
        externalData.setDataProducerCode(StringEscapeUtils.escapeHtml(externalDataPE
                .getDataProducerCode()));
        externalData.setDataSetType(DataSetTypeTranslator.translate(
                externalDataPE.getDataSetType(), new HashMap<PropertyTypePE, PropertyType>()));
        externalData.setDerived(externalDataPE.isDerived());
        externalData
                .setFileFormatType(TypeTranslator.translate(externalDataPE.getFileFormatType()));
        externalData.setInvalidation(tryToGetInvalidation(sampleOrNull, experiment));
        externalData.setLocation(StringEscapeUtils.escapeHtml(externalDataPE.getLocation()));
        externalData.setLocatorType(TypeTranslator.translate(externalDataPE.getLocatorType()));
        final Collection<ExternalData> parents = new HashSet<ExternalData>();
        externalData.setParents(parents);
        for (DataPE parentPE : externalDataPE.getParents())
        {
            parents.add(fillExternalData(new ExternalData(), parentPE));
        }
        setChildren(externalDataPE, externalData);
        externalData.setProductionDate(externalDataPE.getProductionDate());
        externalData.setModificationDate(externalDataPE.getModificationDate());
        externalData.setRegistrationDate(externalDataPE.getRegistrationDate());
        externalData.setSample(sampleOrNull == null ? null : fillSample(new Sample(), sampleOrNull,
                loadSampleProperties));
        externalData.setDataStore(DataStoreTranslator.translate(externalDataPE.getDataStore(),
                defaultDataStoreBaseURL));
        externalData.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.DATA_SET, externalData.getIdentifier()));
        setProperties(externalDataPE, externalData);
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

    private static Invalidation tryToGetInvalidation(SamplePE sampleOrNull, ExperimentPE experiment)
    {
        InvalidationPE invalidationOrNull;
        if (sampleOrNull != null)
        {
            invalidationOrNull = tryToGetInvalidationPE(sampleOrNull);
        } else
        {
            invalidationOrNull = tryToGetInvalidationPE(experiment);
        }
        return translateInvalidation(invalidationOrNull);
    }

    private static InvalidationPE tryToGetInvalidationPE(SamplePE sampleOrNull)
    {
        if (sampleOrNull != null)
        {
            return sampleOrNull.getInvalidation();
        } else
        {
            return null;
        }
    }

    private static InvalidationPE tryToGetInvalidationPE(ExperimentPE experiment)
    {
        if (experiment != null)
        {
            return experiment.getInvalidation();
        } else
        {
            return null;
        }
    }

    private static Invalidation translateInvalidation(InvalidationPE invalidationPE)
    {
        if (invalidationPE == null)
        {
            return null;
        }
        Invalidation result = new Invalidation();
        result.setReason(StringEscapeUtils.escapeHtml(invalidationPE.getReason()));
        result.setRegistrationDate(invalidationPE.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(invalidationPE.getRegistrator()));
        return result;
    }

    private static Sample fillSample(Sample sample, SamplePE samplePE, boolean loadSampleProperties)
    {
        sample.setId(HibernateUtils.getId(samplePE));
        sample.setPermId(StringEscapeUtils.escapeHtml(samplePE.getPermId()));
        SampleTranslator.setCodes(sample, samplePE);
        sample.setInvalidation(translateInvalidation(samplePE.getInvalidation()));
        sample.setSampleType(TypeTranslator.translate(samplePE.getSampleType()));
        sample.setIdentifier(StringEscapeUtils
                .escapeHtml(samplePE.getSampleIdentifier().toString()));
        sample.setRegistrationDate(samplePE.getRegistrationDate());
        sample.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
        sample.setSpace(GroupTranslator.translate(samplePE.getGroup()));
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
