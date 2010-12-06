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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
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

    private final IMailClient mailClient;

    public ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userEmailOrNull,
            DatastoreServiceDescription serviceDescription,
            MailClientParameters mailClientParameters)
    {
        this(task, datasets, parameterBindings, userEmailOrNull, serviceDescription, new MailClient(mailClientParameters));
    }
    
    ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userEmailOrNull,
            DatastoreServiceDescription serviceDescription,
            IMailClient mailClient)
    {
        this.task = task;
        this.datasets = datasets;
        this.parameterBindings = parameterBindings;
        this.userEmailOrNull = userEmailOrNull;
        this.serviceDescription = serviceDescription;
        this.mailClient = mailClient;
    }
    
    private static final class ProxyMailClient implements IMailClient
    {
        private final IMailClient mailClient;
        
        private boolean mailSent;

        ProxyMailClient(IMailClient mailClient){
            this.mailClient = mailClient;
        }
        
        boolean hasMailSent()
        {
            return mailSent;
        }
        
        public void sendMessage(String subject, String content, String replyToOrNull,
                From fromOrNull, String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessage(subject, content, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        public void sendEmailMessage(String subject, String content, EMailAddress replyToOrNull,
                EMailAddress fromOrNull, EMailAddress... recipients)
                throws EnvironmentFailureException
        {
            mailClient.sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        @SuppressWarnings("deprecation")
        public void sendMessageWithAttachment(String subject, String content, String filename,
                DataHandler attachmentContent, String replyToOrNull, From fromOrNull,
                String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessageWithAttachment(subject, content, filename, attachmentContent,
                    replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        public void sendEmailMessageWithAttachment(String subject, String content, String filename,
                DataHandler attachmentContent, EMailAddress replyToOrNull, EMailAddress fromOrNull,
                EMailAddress... recipients) throws EnvironmentFailureException
        {
            mailClient.sendEmailMessageWithAttachment(subject, content, filename,
                    attachmentContent, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        public void sendTestEmail()
        {
            mailClient.sendTestEmail();
        }

    }

    public void execute(File store)
    {
        String errorMessageOrNull = null;
        ProcessingStatus processingStatusOrNull = null;
        ProxyMailClient proxyMailClient = new ProxyMailClient(mailClient);
        try
        {
            DataSetProcessingContext context =
                    new DataSetProcessingContext(parameterBindings, proxyMailClient,
                            userEmailOrNull);
            processingStatusOrNull = task.process(datasets, context);
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
            if (proxyMailClient.hasMailSent() == false || errorMessageOrNull != null)
            {
                createContentAndSendMessage(errorMessageOrNull, processingStatusOrNull);
            }
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
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

    private String getShortDescription(String suffix)
    {
        return String.format("'%s'%s", serviceDescription.getLabel(), suffix);
    }

    private String getDescription(String prefix)
    {
        return String.format("%s on %d data set(s): \n%s", prefix, datasets.size(),
                getDataSetCodes(asCodes(datasets)));
    }

    private static List<String> asCodes(List<DatasetDescription> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (DatasetDescription dataset : datasets)
        {
            codes.add(dataset.getDatasetCode());
        }
        return codes;
    }

    public String getDescription()
    {
        return getDescription(getShortDescription(""));
    }

    private static String getDataSetCodes(List<String> datasets)
    {
        if (datasets.isEmpty())
        {
            return "";
        } else
        {
            final StringBuilder sb = new StringBuilder();
            for (String dataset : datasets)
            {
                sb.append(dataset);
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    private static String generateDescription(ProcessingStatus processingStatus)
    {
        StringBuilder sb = new StringBuilder();
        sb
                .append("This is an automatically generated report from the completed processing of data sets in openBIS.\n");
        List<String> successfullyProcessed = processingStatus.getDatasetsByStatus(Status.OK);
        if (successfullyProcessed != null && successfullyProcessed.isEmpty() == false)
        {
            sb.append("- number of successfully processed data sets: ");
            sb.append(successfullyProcessed.size());
            sb.append(". Datasets: ");
            sb.append(getDataSetCodes(successfullyProcessed));
            sb.append("\n");
        }
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        for (Status errorStatus : errorStatuses)
        {
            List<String> unsuccessfullyProcessed =
                    processingStatus.getDatasetsByStatus(errorStatus);
            sb.append("- processing of ");
            sb.append(unsuccessfullyProcessed.size());
            sb.append(" data set(s) failed because: ");
            sb.append(" " + errorStatus.tryGetErrorMessage() + ". Datasets: ");
            sb.append(getDataSetCodes(unsuccessfullyProcessed));
            sb.append("\n");
        }
        return sb.toString();
    }

}
