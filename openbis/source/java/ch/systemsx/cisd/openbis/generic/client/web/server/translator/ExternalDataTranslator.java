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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
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
        SamplePE sample = tryToGetSample(externalDataPE);
        ExternalData externalData = new ExternalData();
        externalData.setId(externalDataPE.getId());
        externalData.setCode(StringEscapeUtils.escapeHtml(externalDataPE.getCode()));
        externalData.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        externalData.setDataProducerCode(StringEscapeUtils.escapeHtml(externalDataPE
                .getDataProducerCode()));
        externalData.setDataSetType(DataSetTypeTranslator
                .translate(externalDataPE.getDataSetType()));
        externalData.setDerived(externalDataPE.getSampleDerivedFrom() != null);
        externalData
                .setFileFormatType(TypeTranslator.translate(externalDataPE.getFileFormatType()));
        externalData.setInvalidation(tryToGetInvalidation(sample));
        externalData.setLocation(StringEscapeUtils.escapeHtml(externalDataPE.getLocation()));
        externalData.setLocatorType(TypeTranslator.translate(externalDataPE.getLocatorType()));
        externalData.setParentCode(StringEscapeUtils
                .escapeHtml(tryToGetCodeOfFirstParent(externalDataPE)));
        externalData.setProductionDate(externalDataPE.getProductionDate());
        externalData.setModificationDate(externalDataPE.getModificationDate());
        externalData.setRegistrationDate(externalDataPE.getRegistrationDate());
        externalData.setSample(sample == null ? null : fill(new Sample(), sample));
        externalData.setSampleIdentifier(sample == null ? null : StringEscapeUtils
                .escapeHtml(sample.getSampleIdentifier().toString()));
        externalData.setSampleType(sample == null ? null : TypeTranslator.translate(sample
                .getSampleType()));
        externalData.setSampleCode(sample == null ? null : StringEscapeUtils.escapeHtml(sample
                .getCode()));
        externalData.setDataStore(DataStoreTranslator.translate(externalDataPE.getDataStore(),
                defaultDataStoreBaseURL));
        externalData.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.DATA_SET, externalData.getIdentifier()));
        if (loadSampleProperties && sample != null)
        {
            externalData.setSampleProperties(SamplePropertyTranslator.translate(sample
                    .getProperties()));
        }
        setProperties(externalDataPE, externalData);
        ExperimentPE experiment = externalDataPE.getExperiment();
        externalData
                .setExperiment(ExperimentTranslator.translate(experiment, withExperimentFields));
        return externalData;
    }

    private static void setProperties(ExternalDataPE externalDataPE, ExternalData externalData)
    {
        if (HibernateUtils.isInitialized(externalDataPE.getProperties()))
        {
            externalData.setDataSetProperties(DataSetPropertyTranslator.translate(externalDataPE
                    .getProperties()));
        } else
        {
            externalData.setDataSetProperties(new ArrayList<DataSetProperty>());
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

    private static String tryToGetCodeOfFirstParent(ExternalDataPE externalDataPE)
    {
        Set<DataPE> parents = externalDataPE.getParents();
        return parents.isEmpty() ? null : parents.iterator().next().getCode();
    }

    private static SamplePE tryToGetSample(ExternalDataPE externalDataPE)
    {
        SamplePE sample = externalDataPE.getSampleAcquiredFrom();
        if (sample != null)
        {
            return sample;
        }
        return externalDataPE.getSampleDerivedFrom();
    }

    private static Sample fill(Sample sample, SamplePE samplePEOrNull)
    {
        if (sample != null)
        {
            sample.setCode(StringEscapeUtils.escapeHtml(samplePEOrNull.getCode()));
            sample.setInvalidation(tryToGetInvalidation(samplePEOrNull));
            sample.setSampleType(TypeTranslator.translate(samplePEOrNull.getSampleType()));
        }
        return sample;
    }

}
