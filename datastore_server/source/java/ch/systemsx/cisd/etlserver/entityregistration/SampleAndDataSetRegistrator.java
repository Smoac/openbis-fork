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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.SampleCodeGenerator;

/**
 * Utitlity class for registering one sample/dataset combination
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrator
{
    private final SampleAndDataSetRegistrationGlobalState globalState;

    SampleAndDataSetRegistrator(SampleAndDataSetRegistrationGlobalState globalState)
    {
        this.globalState = globalState;
    }

    public void register()
    {
        // boolean generateCodesAutomatically = overrideSpaceIdentifierOrNull != null;
        // SampleCodeGenerator sampleCodeGeneratorOrNull =
        // tryCreateCodeGenrator(generateCodesAutomatically);
        // NamedInputStream stream = new NamedInputStream(new FileInputStream(file),
        // file.getName());
        // BatchSamplesOperation info =
        // SampleUploadSectionsParser.prepareSamples(sampleType, Arrays.asList(stream),
        // overrideSpaceIdentifierOrNull, sampleCodeGeneratorOrNull, true,
        // BatchOperationKind.REGISTRATION);
        // logSamplesExtracted(file, info);
        // service.registerSamples(info.getSamples(), userOrNull);
        // logSamplesRegistered(file, info);
    }

    @SuppressWarnings("unused")
    private void logSamplesRegistered(File file, BatchSamplesOperation info)
    {
        String message =
                String.format("%s samples extracted from file '%s' and registered",
                        info.getCodes().length, file.getName());
        globalState.getOperationLog().info(message);
    }

    @SuppressWarnings("unused")
    private SampleCodeGenerator tryCreateCodeGenrator(boolean generateCodesAutomatically,
            final String samplePrefix)
    {
        if (generateCodesAutomatically)
        {
            return new SampleCodeGenerator()
                {

                    public List<String> generateCodes(int size)
                    {
                        return globalState.getOpenbisService().generateCodes(samplePrefix, size);
                    }
                };
        } else
        {
            return null;
        }
    }

}
