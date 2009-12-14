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

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * A vocabulary term.
 * 
 * @author Izabela Adamczyk
 */
public class VocabularyTerm extends CodeWithRegistration<Vocabulary> implements
        IVocabularyTermUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String CODE = "code";

    public static final String LABEL = "label";

    public static final String DESCRIPTION = "description";

    private Long id;

    private String description;

    private String label;

    private String url;

    private Long ordinal;

    public VocabularyTerm()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    @BeanProperty(label = CODE, optional = false)
    public void setCode(String code)
    {
        super.setCode(code);
    }

    @BeanProperty(label = LABEL, optional = true)
    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    @BeanProperty(label = DESCRIPTION, optional = true)
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        String code = getCode();
        return label == null ? code : (code == null ? label : label + " [" + code + "]");
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }
}
