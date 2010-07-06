/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Tomasz Pylak
 */
public class ImgImageDTO extends AbstractHashable
{
    @ResultColumn("id")
    private long id;

    @ResultColumn("PATH")
    private String filePath;

    @ResultColumn("PAGE")
    private Integer pageOrNull;

    @ResultColumn("COLOR")
    private String colorComponentOrNull;

    @SuppressWarnings("unused")
    private ImgImageDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageDTO(long id, String filePath, Integer pageOrNull,
            ColorComponent colorComponentOrNull)
    {
        this.id = id;
        this.filePath = filePath;
        this.pageOrNull = pageOrNull;
        this.colorComponentOrNull =
                colorComponentOrNull == null ? null : colorComponentOrNull.name();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Integer getPage()
    {
        return pageOrNull;
    }

    public void setPage(Integer page)
    {
        this.pageOrNull = page;
    }

    public ColorComponent getColorComponent()
    {
        return colorComponentOrNull == null ? null : ColorComponent.valueOf(colorComponentOrNull);
    }

    public void setColorComponent(ColorComponent colorComponent)
    {
        this.colorComponentOrNull = colorComponent.name();
    }

    public String getColorComponentAsString()
    {
        return colorComponentOrNull;
    }

}
