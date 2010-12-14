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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;

import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetControlFileProcessor.ControlFileRegistrationProperties;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Utitlity class for registering one sample/dataset combination
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrator extends AbstractSampleAndDataSetProcessor implements
        TransferredDataSetHandler.IDataSetRegistrator
{
    private final ControlFileRegistrationProperties properties;

    private final SampleDataSetPair sampleDataSetPair;

    SampleAndDataSetRegistrator(File folder, ControlFileRegistrationProperties properties,
            SampleDataSetPair sampleDataSetPair)
    {
        super(properties.getGlobalProperties(), folder);
        this.properties = properties;
        this.sampleDataSetPair = sampleDataSetPair;
    }

    public void register()
    {
        File dataSetFile = new File(folder, sampleDataSetPair.getFolderName());
        if (globalState.getDelegator() instanceof IExtensibleDataSetHandler)
        {
            IExtensibleDataSetHandler handler =
                    (IExtensibleDataSetHandler) globalState.getDelegator();
            handler.handleDataSet(dataSetFile, sampleDataSetPair.getDataSetInformation(), this);
            logDataRegistered();
        }
    }

    private void logDataRegistered()
    {
        String message = String.format("Registered sample/data set pair %s", sampleDataSetPair);
        globalState.getOperationLog().info(message);
    }

    public void registerDataSetInApplicationServer(NewExternalData data)
    {
        globalState.getOpenbisService().registerSampleAndDataSet(sampleDataSetPair.getNewSample(),
                data, properties.getUser().getUserId());
    }

}
