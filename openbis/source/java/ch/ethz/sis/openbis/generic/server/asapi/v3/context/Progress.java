/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

/**
 * @author pkupczyk
 */
public class Progress implements IProgress
{

    private static final long serialVersionUID = 1L;

    private String label;

    private String details;

    private Integer numItemsProcessed;

    private Integer totalItemsToProcess;

    public Progress(String label)
    {
        this.label = label;
    }

    public Progress(String label, String details, int numItemsProcessed, int totalItemsToProcess)
    {
        this.label = label;
        this.details = details;
        this.numItemsProcessed = numItemsProcessed;
        this.totalItemsToProcess = totalItemsToProcess;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getDetails()
    {
        return details;
    }

    @Override
    public Integer getTotalItemsToProcess()
    {
        return totalItemsToProcess;
    }

    @Override
    public Integer getNumItemsProcessed()
    {
        return numItemsProcessed;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();

        if (getTotalItemsToProcess() == null && getNumItemsProcessed() == null)
        {
            result.append(getLabel());
        } else
        {
            result.append(getLabel() + " (" + getNumItemsProcessed() + "/" + getTotalItemsToProcess() + ")");
        }

        if (getDetails() != null)
        {
            result.append(" [" + getDetails() + "]");
        }

        return result.toString();
    }

}
