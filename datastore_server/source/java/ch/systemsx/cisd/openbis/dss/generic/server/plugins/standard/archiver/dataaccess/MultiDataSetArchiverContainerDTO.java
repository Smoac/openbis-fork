/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiverContainerDTO
{
    public static final String LOCATION_STAGE = "STAGE";

    public static final String LOCATION_FINAL = "FINAL";

    private long id;

    private String code;

    private String path;

    private String location;

    public MultiDataSetArchiverContainerDTO()
    {
    }

    public MultiDataSetArchiverContainerDTO(long id, String code, String path, String location, boolean closed)
    {
        this.id = id;
        this.code = code;
        this.path = path;
        this.location = location;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

    public String getPath()
    {
        return path;
    }

    public String getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        return "MultiDataSetArchiverContainerDTO [id=" + id + ", code=" + code + ", path=" + path + ", location=" + location + "]";
    }

}
