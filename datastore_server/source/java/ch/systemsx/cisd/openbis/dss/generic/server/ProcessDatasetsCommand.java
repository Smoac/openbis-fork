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

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
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
    private static final long serialVersionUID = 1L;

    private final IProcessingPluginTask task;

    private final List<DatasetDescription> datasets;

    private final String userEmailOrNull;

    private final DatastoreServiceDescription serviceDescription;

    public ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            String userEmailOrNull, DatastoreServiceDescription serviceDescription)
    {
        this.task = task;
        this.datasets = datasets;
        this.userEmailOrNull = userEmailOrNull;
        this.serviceDescription = serviceDescription;
    }

    public void execute(File store)
    {
        String errorMessageOrNull = null;
        try
        {
            task.process(datasets);
        } catch (RuntimeException e)
        {
            // exception message should be readable for users
            errorMessageOrNull = e.getMessage() == null ? "" : e.getMessage();
            throw e;
        } finally
        {
            if (userEmailOrNull == null)
            {
                System.out
                        .println("No receiver email address provided for processing completion notification.");
                return;
            }
            final StringBuilder sb = new StringBuilder();
            final String messageHeader;
            if (errorMessageOrNull != null)
            {
                // error
                messageHeader = getShortDescription("Failed to perform ");
                sb.append(getDescription(messageHeader));
                sb.append("\n\nError message:\n");
                sb.append(errorMessageOrNull);
            } else
            {
                // success
                messageHeader = getShortDescription("Finished ");
                sb.append(getDescription(messageHeader));
            }
            // TODO 2010-01-06, Piotr Buczek: send mail
            System.err.println("email: " + userEmailOrNull);
            System.err.println("messageHeader: " + messageHeader);
            System.err.println(sb.toString());
        }
    }

    private String getShortDescription(String prefix)
    {
        return String.format("%s'%s'", prefix, serviceDescription.getLabel());
    }

    private String getDescription(String prefix)
    {
        return String.format("%s on data set(s): [%s]", prefix, getDataSetCodes());
    }

    public String getDescription()
    {
        return getDescription(getShortDescription(""));
    }

    public String getDataSetCodes()
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
}
