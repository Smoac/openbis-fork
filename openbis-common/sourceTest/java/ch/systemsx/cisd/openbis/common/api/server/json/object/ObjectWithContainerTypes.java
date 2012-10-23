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

package ch.systemsx.cisd.openbis.common.api.server.json.object;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@SuppressWarnings("rawtypes")
@JsonObject(ObjectWithContainerTypesFactory.TYPE)
public class ObjectWithContainerTypes
{

    public Collection collectionWithoutType;

    public Collection<Object> collectionWithObjectType;

    public Collection<ObjectWithType> collectionWithSpecificType;

    public LinkedHashSet linkedHashSetWithoutType;

    public LinkedHashSet<Object> linkedHashSetWithObjectType;

    public LinkedHashSet<ObjectWithType> linkedHashSetWithSpecificType;

    public List listWithoutType;

    public List<Object> listWithObjectType;

    public List<ObjectWithType> listWithSpecificType;

    public LinkedList linkedListWithoutType;

    public LinkedList<Object> linkedListWithObjectType;

    public LinkedList<ObjectWithType> linkedListWithSpecificType;

    public Map mapWithoutType;

    public Map<String, Object> mapWithObjectType;

    public Map<String, ObjectWithType> mapWithSpecificType;

    public LinkedHashMap linkedHashMapWithoutType;

    public LinkedHashMap<String, Object> linkedHashMapWithObjectType;

    public LinkedHashMap<String, ObjectWithType> linkedHashMapWithSpecificType;

    public Object[] arrayWithObjectType;

    public ObjectWithType[] arrayWithSpecificType;

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }

}
