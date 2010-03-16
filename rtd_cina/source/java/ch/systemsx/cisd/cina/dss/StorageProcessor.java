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

import ch.systemsx.cisd.cina.dss.info.EntityRegistrationSuccessEmail;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
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
        // Create the email first because the email looks into the directory to determine its
        // content.
        EntityRegistrationSuccessEmail successEmail =
                new EntityRegistrationSuccessEmail(dataSetInformation, incomingDataSetDirectory);

        File answer =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);

        String emailAddress = dataSetInformation.tryGetUploadingUserEmail();
        if (emailAddress == null)
        {
            emailAddress = defaultEmailAddress;
        }

        // WORKAROUND: The IMailClient interface is used in many places. I added the
        // sendMessageWithAttachment method to the MailClient class, but I don't want to make a
        // large, global change to introduce the method into the interface, so I do an instanceof
        // test here.
        if (mailClient instanceof MailClient)
        {
            ((MailClient) mailClient).sendMessageWithAttachment(successEmail.getSubject(),
                    successEmail.getContentMimeText(), successEmail
                            .getContentMimeAttachmentFileName(), successEmail
                            .getContentMimeAttachmentContent(), null, null, emailAddress);
        } else
        {
            mailClient.sendMessage(successEmail.getSubject(), successEmail.getContentTextOnly(),
                    null, null, emailAddress);
        }

        return answer;
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        return super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

}
