/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class DataSetBuilder extends Builder<ExternalData>
{

    private IETLLIMSService etlService;

    private SampleIdentifier sampleIdentifier;

    private String code;

    public DataSetBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer,
            IETLLIMSService etlService)
    {
        super(commonServer, genericServer);
        this.etlService = etlService;
        this.code = UUID.randomUUID().toString();
    }

    public DataSetBuilder inSample(Sample sample)
    {
        this.sampleIdentifier = getSampleIdentifier(sample);
        return this;
    }

    @Override
    public ExternalData create()
    {
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(UUID.randomUUID().toString());
        commonServer.registerDataSetType(systemSession, dataSetType);

        NewExternalData data = new NewExternalData();
        data.setCode(this.code);
        data.setDataSetType(dataSetType);
        data.setFileFormatType(new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE));
        data.setSampleIdentifierOrNull(this.sampleIdentifier);
        data.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
        data.setLocation("location");
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataStoreCode("STANDARD");

        etlService.registerDataSet(systemSession, sampleIdentifier, data);

        return etlService.tryGetDataSet(systemSession, this.code);

    }
}