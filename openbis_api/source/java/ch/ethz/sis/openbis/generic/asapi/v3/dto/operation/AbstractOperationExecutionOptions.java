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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation;

/**
 * @author pkupczyk
 */
public class AbstractOperationExecutionOptions implements IOperationExecutionOptions
{

    private static final long serialVersionUID = 1L;

    private String description;

    private Integer availabilityTime;

    private Integer summaryAvailabilityTime;

    private Integer detailsAvailabilityTime;

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Integer getAvailabilityTime()
    {
        return availabilityTime;
    }

    public void setAvailabilityTime(Integer availabilityTime)
    {
        this.availabilityTime = availabilityTime;
    }

    @Override
    public Integer getSummaryAvailabilityTime()
    {
        return summaryAvailabilityTime;
    }

    public void setSummaryAvailabilityTime(Integer summaryAvailabilityTime)
    {
        this.summaryAvailabilityTime = summaryAvailabilityTime;
    }

    @Override
    public Integer getDetailsAvailabilityTime()
    {
        return detailsAvailabilityTime;
    }

    public void setDetailsAvailabilityTime(Integer detailsAvailabilityTime)
    {
        this.detailsAvailabilityTime = detailsAvailabilityTime;
    }

}
