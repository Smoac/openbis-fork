/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.dto.IIdAndCodeHolder;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystem implements IIdAndCodeHolder, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String code;

    private DatabaseInstance databaseInstance;

    private String label;

    private String urlTemplate;

    private boolean openBIS;

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    public boolean isOpenBIS()
    {
        return openBIS;
    }

    public void setOpenBIS(boolean openBIS)
    {
        this.openBIS = openBIS;
    }
}
