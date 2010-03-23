/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Criteria for archiving <i>data sets</i>.
 * 
 * @author Piotr Buczek
 */
public class ArchiverDataSetCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // number of days before current date
    private final int olderThan;

    private final String dataSetTypeCodeOrNull;

    public ArchiverDataSetCriteria(int olderThan, String dataSetTypeCodeOrNull)
    {
        this.olderThan = olderThan;
        this.dataSetTypeCodeOrNull = dataSetTypeCodeOrNull;
    }

    public int getOlderThan()
    {
        return olderThan;
    }

    public String tryGetDataSetTypeCode()
    {
        return dataSetTypeCodeOrNull;
    }

    @Override
    public String toString()
    {
        return "older than: " + olderThan + "; data set type: " + dataSetTypeCodeOrNull;
    }

}
