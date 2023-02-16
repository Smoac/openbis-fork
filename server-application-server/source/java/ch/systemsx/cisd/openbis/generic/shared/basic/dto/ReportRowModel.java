/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
 * Model of a report row.
 * 
 * @author Piotr Buczek
 */
public class ReportRowModel implements Serializable
{

    private static final long serialVersionUID = 1L;

    private int rowNumber; // index (starting from 0) of the row in the full report table

    public ReportRowModel(int rowNumber)
    {
        this.rowNumber = rowNumber;
    }

    public int getRowNumber()
    {
        return rowNumber;
    }

    // for serialization
    @SuppressWarnings("unused")
    private ReportRowModel()
    {
    }

}
