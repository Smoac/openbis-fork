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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Command which processes datasets using the specified plugin instance. This is essentially an
 * adapter to {@link IProcessingPluginTask}.
 * 
 * @author Tomasz Pylak
 */
public class ProcessDatasetsCommand implements IDataSetCommand
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProcessDatasetsCommand.class);

    private static final long serialVersionUID = 1L;

    private final IProcessingPluginTask task;

    private final List<DatasetDescription> datasets;

    private final Map<String, String> parameterBindings;
    
    private final String userEmailOrNull;

    private final DatastoreServiceDescription serviceDescription;

    private final MailClientParameters mailClientParameters;

    public ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userEmailOrNull,
            DatastoreServiceDescription serviceDescription,
            MailClientParameters mailClientParameters)
    {
        this.task = task;
        this.datasets = datasets;
        this.parameterBindings = parameterBindings;
        this.userEmailOrNull = userEmailOrNull;
        this.serviceDescription = serviceDescription;
        this.mailClientParameters = mailClientParameters;
    }

    public void execute(File store)
    {
        String errorMessageOrNull = null;
        ProcessingStatus processingStatusOrNull = null;
        try
        {
            processingStatusOrNull = task.process(datasets, parameterBindings);
        } catch (RuntimeException e)
        {
            // exception message should be readable for users
            errorMessageOrNull = e.getMessage() == null ? "" : e.getMessage();
            throw e;
        } finally
        {
            if (userEmailOrNull == null)
            {
                if (errorMessageOrNull != null)
                {
                    String warning =
                            String
                                    .format(
                                            "Dataset processing '%s' has failed with a message '%s' but"
                                                    + " the person who triggered processing has no email address specified."
                                                    + " No notification has been sent.",
                                            serviceDescription.getKey(), errorMessageOrNull);
                    operationLog.warn(warning);
                }
                return;
            }
            createContentAndSendMessage(errorMessageOrNull, processingStatusOrNull);
        }
    }

    private void createContentAndSendMessage(String errorMessageOrNull,
            ProcessingStatus processingStatusOrNull)
    {
        final StringBuilder contentBuilder = new StringBuilder();
        final String subject;
        if (errorMessageOrNull != null)
        {
            // create error message content
            subject = getShortDescription(" processing failed");
            contentBuilder.append(getDescription(subject));
            contentBuilder.append("\n\nError message:\n");
            contentBuilder.append(errorMessageOrNull);
        } else
        {
            // create success message content
            subject = getShortDescription(" processing finished");
            if (processingStatusOrNull != null)
            {
                contentBuilder.append(generateDescription(processingStatusOrNull));
            } else
            {
                contentBuilder.append(getDescription(subject));
            }
        }
        sendMessage(subject, contentBuilder.toString(), userEmailOrNull);
    }

    private void sendMessage(String subject, String content, String recipient)
    {
        String from = mailClientParameters.getFrom();
        String smtpHost = mailClientParameters.getSmtpHost();
        String smtpUser = mailClientParameters.getSmtpUser();
        String smtpPassword = mailClientParameters.getSmtpPassword();

        IMailClient mailClient = new MailClient(from, smtpHost, smtpUser, smtpPassword);
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

    private String getShortDescription(String suffix)
    {
        return String.format("'%s'%s", serviceDescription.getLabel(), suffix);
    }

    private String getDescription(String prefix)
    {
        return String.format("%s on data set(s): \n%s", prefix, getDataSetCodes(datasets));
    }

    public String getDescription()
    {
        return getDescription(getShortDescription(""));
    }

    private static String getDataSetCodes(List<DatasetDescription> datasets)
    {
        if (datasets.isEmpty())
        {
            return "";
        } else
        {
            final StringBuilder sb = new StringBuilder();
            for (DatasetDescription dataset : datasets)
            {
                sb.append(dataset.getDatasetCode());
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    private static String generateDescription(ProcessingStatus processingStatus)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Data sets:\n");
        List<DatasetDescription> successfullyProcessed =
                processingStatus.getDatasetsByStatus(Status.OK);
        if (successfullyProcessed != null && successfullyProcessed.isEmpty() == false)
        {
            sb.append("- successfully processed: ");
            sb.append(getDataSetCodes(successfullyProcessed));
            sb.append("\n");
        }
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        for (Status errorStatus : errorStatuses)
        {
            sb.append("- " + errorStatus.tryGetErrorMessage() + ": ");
            sb.append(getDataSetCodes(processingStatus.getDatasetsByStatus(errorStatus)));
            sb.append("\n");
        }
        return sb.toString();
    }

}
