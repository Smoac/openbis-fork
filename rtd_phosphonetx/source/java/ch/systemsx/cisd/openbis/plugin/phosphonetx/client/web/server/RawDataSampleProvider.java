/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.EXPERIMENT;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.PARENT;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.REGISTRATION_DATE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleProvider extends AbstractTableModelProvider<Sample>
{
    private final IProteomicsDataServiceInternal service;

    private final String sessionToken;

    RawDataSampleProvider(IProteomicsDataServiceInternal service, String sessionToken)
    {
        this.service = service;
        this.sessionToken = sessionToken;
    }

    @Override
    public TypedTableModel<Sample> createTableModel(int maxSize)
    {
        List<MsInjectionSample> samples = service.listRawDataSamples(sessionToken);
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(CODE).withDataType(DataTypeCode.VARCHAR);
        builder.addColumn(REGISTRATION_DATE).withDataType(DataTypeCode.TIMESTAMP)
                .withDefaultWidth(190);
        builder.addColumn(PARENT).withDataType(DataTypeCode.VARCHAR);
        builder.addColumn(EXPERIMENT).withDataType(DataTypeCode.VARCHAR);
        for (MsInjectionSample msInjectionSample : samples)
        {
            Sample sample = msInjectionSample.getSample();
            builder.addRow(sample);
            builder.column(CODE).addString(sample.getCode());
            builder.column(REGISTRATION_DATE).addDate(sample.getRegistrationDate());
            Sample parent = sample.getGeneratedFrom();
            builder.column(PARENT).addString(parent.getIdentifier());
            Experiment experiment = parent.getExperiment();
            if (experiment != null)
            {
                builder.column(EXPERIMENT).addString(experiment.getIdentifier());
            }
            builder.columnGroup("MS").addProperties("", sample.getProperties());
            builder.columnGroup("BIO_").addProperties(parent.getProperties());
        }
        return builder.getModel();
    }

}
