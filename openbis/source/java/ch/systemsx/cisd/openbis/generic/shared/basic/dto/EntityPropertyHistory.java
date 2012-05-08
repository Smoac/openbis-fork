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
import java.util.Date;

/**
 * @author Franz-Josef Elmer
 */
public class EntityPropertyHistory implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private PropertyType propertyType;

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    private String value;

    private String vocabularyTerm;

    private String material;

    private Date validFromDate;

    private Date validUntilDate;

    private Person author;

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    public void setVocabularyTerm(String vocabularyTerm)
    {
        this.vocabularyTerm = vocabularyTerm;
    }

    public String getMaterial()
    {
        return material;
    }

    public void setMaterial(String material)
    {
        this.material = material;
    }

    public Date getValidFromDate()
    {
        return validFromDate;
    }

    public void setValidFromDate(Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    public Date getValidUntilDate()
    {
        return validUntilDate;
    }

    public void setValidUntilDate(Date validUntilDate)
    {
        this.validUntilDate = validUntilDate;
    }

    public Person getAuthor()
    {
        return author;
    }

    public void setAuthor(Person author)
    {
        this.author = author;
    }

}
