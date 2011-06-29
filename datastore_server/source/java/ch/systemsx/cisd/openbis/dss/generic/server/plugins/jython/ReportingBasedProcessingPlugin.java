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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IEmailBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin that sends email with a TSV representation of a table created by
 * {@link JythonBasedReportingPlugin}. There are two options to use the plugin. There may be a
 * separate table/email generated for every processed data set or a single table/email for all.
 * 
 * @author Piotr Buczek
 */
public class ReportingBasedProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonBasedProcessingPlugin.class);

    // keys of properties

    private static final String SCRIPT_PATH = "script-path"; // the only mandatory property

    private static final String EMAIL_SUBJECT = "email-subject";

    private static final String EMAIL_BODY = "email-body";

    private static final String ATTACHMENT_NAME = "attachment-name";

    private static final String SINGLE_REPORT_FOR_ALL = "single-report-for-all";

    private static final String DEFAULT_EMAIL_SUBJECT = MailService.DEFAULT_SUBJECT;

    private static final String DEFAULT_EMAIL_BODY = MailService.DEFAULT_BODY_TEXT;

    private static final String DEFAULT_ATTACHMENT_NAME = "report.txt";

    private static final boolean DEFAULT_SINGLE_REPORT_FOR_ALL = false;

    private final String scriptPath;

    private final String emailSubject;

    private final String emailBody;

    private final String attachmentName;

    private final boolean singleReportForAll;

    public ReportingBasedProcessingPlugin(Properties properties, File storeRoot)
    {
        this.scriptPath = PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH);
        this.singleReportForAll =
                PropertyUtils.getBoolean(properties, SINGLE_REPORT_FOR_ALL,
                        DEFAULT_SINGLE_REPORT_FOR_ALL);
        this.emailSubject =
                PropertyUtils.getProperty(properties, EMAIL_SUBJECT, DEFAULT_EMAIL_SUBJECT);
        this.emailBody = PropertyUtils.getProperty(properties, EMAIL_BODY, DEFAULT_EMAIL_BODY);
        this.attachmentName =
                PropertyUtils.getProperty(properties, ATTACHMENT_NAME, DEFAULT_ATTACHMENT_NAME);
    }

    public ProcessingStatus process(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        IMailService mailService =
                new MailService(context.getMailClient(), context.getUserEmailOrNull(),
                        emailSubject, emailBody);
        ProcessingStatus status = new ProcessingStatus();
        if (singleReportForAll)
        {
            try
            {
                createAndSendReport(dataSets, context, mailService);
                status.addDatasetStatuses(dataSets, Status.OK);
            } catch (Exception ex)
            {
                status.addDatasetStatuses(dataSets, Status.createError("Exception occured: " + ex));
                operationLog.error("Exception occured while processing " + dataSets, ex);
            }
        } else
        {
            for (DatasetDescription dataSet : dataSets)
            {
                try
                {
                    createAndSendReport(Collections.singletonList(dataSet), context, mailService);
                    status.addDatasetStatus(dataSet, Status.OK);
                } catch (Exception ex)
                {
                    status.addDatasetStatus(dataSet, Status.createError("Exception occured: " + ex));
                    operationLog.error("Exception occured while processing " + dataSet, ex);
                }
            }
        }
        return null;
    }

    private void createAndSendReport(List<DatasetDescription> dataSets,
            DataSetProcessingContext context, IMailService mailService) throws IOException
    {
        TableModel table = createTableModel(dataSets, context);
        String tableAsString = AbstractTableModelReportingPlugin.convertTableToCsvString(table);
        IEmailBuilder emailBuilder =
                mailService.createEmailBuilder().withAttachedText(tableAsString, attachmentName);
        mailService.sendEmail(emailBuilder);
    }

    public TableModel createTableModel(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        return JythonBasedReportingPlugin.createReport(dataSets, context, scriptPath);
    }

}
