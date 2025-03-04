/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.ImportOperationResult")
public class ImportOperationResult implements Serializable, IOperationResult
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ImportResult importResult;

    public ImportOperationResult()
    {
    }

    public ImportOperationResult(final ImportResult importResult)
    {
        this.importResult = importResult;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @JsonIgnore
    public ImportResult getImportResult()
    {
        return importResult;
    }

    public void setImportResult(final ImportResult importResult)
    {
        this.importResult = importResult;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName();
    }

}
