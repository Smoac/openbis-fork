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

package ch.systemsx.cisd.cina.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Store the data and send an email to the person who registered the data.
 * <p>
 * For experiments, the email should contain the properties file
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class StorageProcessor extends AbstractDelegatingStorageProcessor
{
    private String defaultEmailAddress = "root@localhost";

    private static final String DEFAULT_EMAIL_ADDRESS_KEY = "default-email";

    public StorageProcessor(Properties properties)
    {
        super(properties);

        if (properties.containsKey(DEFAULT_EMAIL_ADDRESS_KEY))
        {
            defaultEmailAddress = properties.getProperty(DEFAULT_EMAIL_ADDRESS_KEY);
        }
    }

    public StorageProcessor(IStorageProcessor delegatedProcessor)
    {
        super(delegatedProcessor);
    }

    @Override
    public File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        File answer =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);

        String emailAddress = dataSetInformation.tryGetUploadingUserEmail();
        if (emailAddress == null)
        {
            emailAddress = defaultEmailAddress;
        }

        mailClient.sendMessage("[CINA] Registered Experiment",
                "Experment was successfully registered. Use the following properties file : experiment.code="
                        + dataSetInformation.getExperimentIdentifier(), null, null, emailAddress);

        return answer;
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        return super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

}
