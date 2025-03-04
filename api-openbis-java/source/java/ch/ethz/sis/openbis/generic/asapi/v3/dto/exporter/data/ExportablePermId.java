/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
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
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.data.ExportablePermId")
public class ExportablePermId implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExportableKind exportableKind;

    @JsonProperty
    private String permId;

    @SuppressWarnings("unused")
    public ExportablePermId()
    {
    }

    public ExportablePermId(final ExportableKind exportableKind, final String permId)
    {
        this.exportableKind = exportableKind;
        this.permId = permId;
    }

    @JsonIgnore
    public ExportableKind getExportableKind()
    {
        return exportableKind;
    }

    @JsonIgnore
    public void setExportableKind(final ExportableKind exportableKind)
    {
        this.exportableKind = exportableKind;
    }

    @JsonIgnore
    public String getPermId()
    {
        return permId;
    }

    @JsonIgnore
    public void setPermId(final String permId)
    {
        this.permId = permId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("exportableKind", String.valueOf(exportableKind)).append("permId", String.valueOf(permId)).toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ExportablePermId that = (ExportablePermId) o;

        if (exportableKind != that.exportableKind)
        {
            return false;
        }
        return Objects.equals(permId, that.permId);
    }

    @Override
    public int hashCode()
    {
        int result = exportableKind != null ? exportableKind.hashCode() : 0;
        result = 31 * result + (permId != null ? permId.hashCode() : 0);
        return result;
    }

}
