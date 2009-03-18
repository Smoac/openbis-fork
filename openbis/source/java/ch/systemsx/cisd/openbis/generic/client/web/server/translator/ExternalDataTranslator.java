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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProcedureType;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataTranslator
{
    private ExternalDataTranslator()
    {
    }

    public static List<ExternalData> translate(List<ExternalDataPE> list, String dataStoreBaseURL)
    {
        ArrayList<ExternalData> result = new ArrayList<ExternalData>(list.size());
        for (ExternalDataPE externalDataPE : list)
        {
            ExternalData data = translate(externalDataPE, dataStoreBaseURL);
            result.add(data);
        }
        return result;
    }

    public static ExternalData translate(ExternalDataPE externalDataPE, String dataStoreBaseURL,
            final LoadableFields... withExperimentFields)
    {
        return translate(externalDataPE, dataStoreBaseURL, false, withExperimentFields);
    }

    public static ExternalData translate(ExternalDataPE externalDataPE, String dataStoreBaseURL,
            boolean loadSampleProperties, final LoadableFields... withExperimentFields)
    {
        SamplePE sample = tryToGetSample(externalDataPE);
        ExternalData externalData = new ExternalData();
        externalData.setDataStoreBaseURL(dataStoreBaseURL);
        externalData.setCode(StringEscapeUtils.escapeHtml(externalDataPE.getCode()));
        externalData.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        externalData.setDataProducerCode(StringEscapeUtils.escapeHtml(externalDataPE
                .getDataProducerCode()));
        externalData.setDataSetType(fill(new DataSetType(), externalDataPE.getDataSetType()));
        externalData.setDerived(externalDataPE.getSampleDerivedFrom() != null);
        externalData.setFileFormatType(fill(new FileFormatType(), externalDataPE
                .getFileFormatType()));
        externalData.setInvalidation(tryToGetInvalidation(sample));
        externalData.setLocation(StringEscapeUtils.escapeHtml(externalDataPE.getLocation()));
        externalData.setLocatorType(fill(new LocatorType(), externalDataPE.getLocatorType()));
        externalData.setParentCode(StringEscapeUtils
                .escapeHtml(tryToGetCodeOfFirstParent(externalDataPE)));
        externalData.setProcedureType(getProcedureType(externalDataPE));
        externalData.setProductionDate(externalDataPE.getProductionDate());
        externalData.setRegistrationDate(externalDataPE.getRegistrationDate());
        externalData.setSampleIdentifier(sample == null ? null : StringEscapeUtils
                .escapeHtml(sample.getSampleIdentifier().toString()));
        externalData.setSampleType(sample == null ? null : fill(new SampleType(), sample
                .getSampleType()));
        externalData.setSampleCode(sample == null ? null : StringEscapeUtils.escapeHtml(sample
                .getCode()));
        if (loadSampleProperties && sample != null)
        {
            externalData.setSampleProperties(SamplePropertyTranslator.translate(sample
                    .getProperties()));
        }
        externalData.setProcedure(getProcedure(externalDataPE, withExperimentFields));
        return externalData;
    }

    private static ProcedureType getProcedureType(ExternalDataPE externalDataPE)
    {
        ProcedurePE procedure = externalDataPE.getProcedure();
        if (procedure == null)
        {
            return null;
        }
        return fill(new ProcedureType(), procedure.getProcedureType());
    }

    private static Procedure getProcedure(ExternalDataPE externalDataPE,
            final LoadableFields... withExperimentFields)
    {
        final ProcedurePE procedure = externalDataPE.getProcedure();
        if (procedure == null)
        {
            return null;
        }
        return ProcedureTranslator.translate(procedure, withExperimentFields);
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

    private static <T extends AbstractType> T fill(T type, AbstractTypePE typePEOrNull)
    {
        if (typePEOrNull != null)
        {
            type.setCode(typePEOrNull.getCode());
            type.setDescription(StringEscapeUtils.escapeHtml(typePEOrNull.getDescription()));
        }
        return type;
    }

}
