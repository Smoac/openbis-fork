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

package ch.systemsx.cisd.common.api.server.json.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */

@SuppressWarnings("rawtypes")
@JsonObject(ObjectWithContainerTypes.TYPE)
public class ObjectWithContainerTypes
{

    public static final String TYPE = "ObjectWithContainerTypes";

    public static final String CLASS = ".LegacyObjectWithContainerTypes";

    public static final String COLLECTION_WITHOUT_TYPE = "collectionWithoutType";

    public static final String COLLECTION_WITH_OBJECT_TYPE = "collectionWithObjectType";

    public static final String COLLECTION_WITH_SPECIFIC_TYPE = "collectionWithSpecificType";

    public static final String LINKED_HASH_SET_WITHOUT_TYPE = "linkedHashSetWithoutType";

    public static final String LINKED_HASH_SET_WITH_OBJECT_TYPE = "linkedHashSetWithObjectType";

    public static final String LINKED_HASH_SET_WITH_SPECIFIC_TYPE = "linkedHashSetWithSpecificType";

    public static final String LIST_WITHOUT_TYPE = "listWithoutType";

    public static final String LIST_WITH_OBJECT_TYPE = "listWithObjectType";

    public static final String LIST_WITH_SPECIFIC_TYPE = "listWithSpecificType";

    public static final String LINKED_LIST_WITHOUT_TYPE = "linkedListWithoutType";

    public static final String LINKED_LIST_WITH_OBJECT_TYPE = "linkedListWithObjectType";

    public static final String LINKED_LIST_WITH_SPECIFIC_TYPE = "linkedListWithSpecificType";

    public static final String MAP_WITHOUT_TYPE = "mapWithoutType";

    public static final String MAP_WITH_OBJECT_TYPE = "mapWithObjectType";

    public static final String MAP_WITH_SPECIFIC_TYPE = "mapWithSpecificType";

    public static final String LINKED_HASH_MAP_WITHOUT_TYPE = "linkedHashMapWithoutType";

    public static final String LINKED_HASH_MAP_WITH_OBJECT_TYPE = "linkedHashMapWithObjectType";

    public static final String LINKED_HASH_MAP_WITH_SPECIFIC_TYPE = "linkedHashMapWithSpecificType";

    public static final String ARRAY_WITH_OBJECT_TYPE = "arrayWithObjectType";

    public static final String ARRAY_WITH_SPECIFIC_TYPE = "arrayWithSpecificType";

    public static final String ITEM_WITH_KNOWN_TYPE = "itemWithKnownType";

    public static final String ITEM_WITH_KNOWN_UNIQUE_CLASS = "itemWithKnownUniqueClass";

    public static final String ITEM_WITH_RENAMED_PROPERTIES = "itemWithRenamedProperties";

    public static final String ITEM_WITH_IGNORED_PROPERTIES = "itemWithIgnoredProperties";

    public static final String ITEM_WITH_DATE_TYPES = "itemWithDateTypes";

    public static final String ITEM_WITH_ENUM_TYPES = "itemWithEnumTypes";

    public static final String ITEM_WITH_NESTED_TYPES = "itemWithNestedTypes";

    public static final String ITEM_WITH_PRIMITIVE_TYPES = "itemWithPrimitiveTypes";

    public static final String ITEM_WITH_PRIVATE_ACCESS = "itemWithPrivateAccess";

    public static final String ITEM_WITH_UNKNOWN_CLASS = "itemWithUnknownClass";

    public static final String ITEM_WITH_UNKNOWN_TYPE = "itemWithUnknownType";

    public static final String ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS =
            "itemWithKnownButNotUniqueClass";

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

    public static ObjectWithContainerTypes createObject()
    {
        LinkedHashMap map = createItemsObjectsMap();
        LinkedHashMap specificMap = createItemsObjectsSpecificMap();
        return createObjectCommon(map, specificMap);
    }

    @SuppressWarnings(
        { "unchecked" })
    public static ObjectWithContainerTypes createObjectWithIncorrectObjectsAsMaps()
    {
        LinkedHashMap map = createItemsObjectsMap();
        map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                ObjectWithTypeA.createMap(null, ObjectType.CLASS).toMap());
        map.put(ITEM_WITH_UNKNOWN_TYPE, ObjectWithUnknownType.createMap(null, ObjectType.TYPE)
                .toMap());
        map.put(ITEM_WITH_UNKNOWN_CLASS, ObjectWithUnknownType.createMap(null, ObjectType.CLASS)
                .toMap());
        LinkedHashMap specificMap = createItemsObjectsSpecificMap();
        return createObjectCommon(map, specificMap);
    }

    @SuppressWarnings(
        { "unchecked" })
    private static ObjectWithContainerTypes createObjectCommon(Map aMap, Map aSpecificMap)
    {
        Map map = new HashMap(aMap);
        LinkedHashMap linkedHashMap = new LinkedHashMap(aMap);
        Collection collection = new ArrayList(aMap.values());
        LinkedHashSet linkedHashSet = new LinkedHashSet(aMap.values());
        List list = new ArrayList(aMap.values());
        LinkedList linkedList = new LinkedList(aMap.values());
        Object[] array = aMap.values().toArray(new Object[aMap.size()]);

        Map specificMap = new HashMap(aSpecificMap);
        LinkedHashMap specificLinkedHashMap = new LinkedHashMap(aSpecificMap);
        Collection specificCollection = new ArrayList(aSpecificMap.values());
        LinkedHashSet specificLinkedHashSet = new LinkedHashSet(aSpecificMap.values());
        List specificList = new ArrayList(aSpecificMap.values());
        LinkedList specificLinkedList = new LinkedList(aSpecificMap.values());
        ObjectWithType[] specificArray =
                (ObjectWithType[]) aSpecificMap.values().toArray(
                        new ObjectWithType[aSpecificMap.size()]);

        ObjectWithContainerTypes object = new ObjectWithContainerTypes();
        object.collectionWithoutType = collection;
        object.collectionWithObjectType = collection;
        object.collectionWithSpecificType = specificCollection;
        object.linkedHashSetWithoutType = linkedHashSet;
        object.linkedHashSetWithObjectType = linkedHashSet;
        object.linkedHashSetWithSpecificType = specificLinkedHashSet;
        object.listWithoutType = list;
        object.listWithObjectType = list;
        object.listWithSpecificType = specificList;
        object.linkedListWithoutType = linkedList;
        object.linkedListWithObjectType = linkedList;
        object.linkedListWithSpecificType = specificLinkedList;
        object.mapWithoutType = map;
        object.mapWithObjectType = map;
        object.mapWithSpecificType = specificMap;
        object.linkedHashMapWithoutType = linkedHashMap;
        object.linkedHashMapWithObjectType = linkedHashMap;
        object.linkedHashMapWithSpecificType = specificLinkedHashMap;
        object.arrayWithObjectType = array;
        object.arrayWithSpecificType = specificArray;
        return object;
    }

    @SuppressWarnings(
        { "unchecked" })
    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        Map map = createItemsMapsMap(objectCounter, objectType);
        Collection collection = new ArrayList(map.values());
        List list = new ArrayList(map.values());
        Object[] array = map.values().toArray(new Object[map.size()]);

        Map specificMap = createItemsMapsSpecificMap(objectCounter, objectType);
        Collection specificCollection = new ArrayList(specificMap.values());
        List specificList = new ArrayList(specificMap.values());
        Object[] specificArray = specificMap.values().toArray(new Object[specificMap.size()]);

        ObjectMap object = new ObjectMap();
        object.putId(objectCounter);
        object.putType(TYPE, CLASS, objectType);
        object.putField(COLLECTION_WITHOUT_TYPE, collection);
        object.putField(COLLECTION_WITH_OBJECT_TYPE, collection);
        object.putField(COLLECTION_WITH_SPECIFIC_TYPE, specificCollection);

        object.putField(LINKED_HASH_SET_WITHOUT_TYPE, collection);
        object.putField(LINKED_HASH_SET_WITH_OBJECT_TYPE, collection);
        object.putField(LINKED_HASH_SET_WITH_SPECIFIC_TYPE, specificCollection);

        object.putField(MAP_WITHOUT_TYPE, map);
        object.putField(MAP_WITH_OBJECT_TYPE, map);
        object.putField(MAP_WITH_SPECIFIC_TYPE, specificMap);

        object.putField(LINKED_HASH_MAP_WITHOUT_TYPE, map);
        object.putField(LINKED_HASH_MAP_WITH_OBJECT_TYPE, map);
        object.putField(LINKED_HASH_MAP_WITH_SPECIFIC_TYPE, specificMap);

        object.putField(LIST_WITHOUT_TYPE, list);
        object.putField(LIST_WITH_OBJECT_TYPE, list);
        object.putField(LIST_WITH_SPECIFIC_TYPE, specificList);

        object.putField(LINKED_LIST_WITHOUT_TYPE, list);
        object.putField(LINKED_LIST_WITH_OBJECT_TYPE, list);
        object.putField(LINKED_LIST_WITH_SPECIFIC_TYPE, specificList);

        object.putField(ARRAY_WITH_OBJECT_TYPE, array);
        object.putField(ARRAY_WITH_SPECIFIC_TYPE, specificArray);

        return object;
    }

    @SuppressWarnings(
        { "unchecked" })
    private static LinkedHashMap createItemsObjectsMap()
    {
        LinkedHashMap map = new LinkedHashMap();
        map.put(ITEM_WITH_KNOWN_TYPE, ObjectWithTypeA.createObject());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS, ObjectWithTypeAA.createObject());
        map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS, ObjectWithTypeA.createObject());
        map.put(ITEM_WITH_UNKNOWN_TYPE, ObjectWithUnknownType.createObject());
        map.put(ITEM_WITH_UNKNOWN_CLASS, ObjectWithUnknownType.createObject());
        map.put(ITEM_WITH_PRIVATE_ACCESS, ObjectWithPrivateAccess.createObject());
        map.put(ITEM_WITH_PRIMITIVE_TYPES, ObjectWithPrimitiveTypes.createObject());
        map.put(ITEM_WITH_NESTED_TYPES, ObjectWithNestedTypes.createObject());
        map.put(ITEM_WITH_ENUM_TYPES, ObjectWithEnumTypes.createObject());
        map.put(ITEM_WITH_DATE_TYPES, ObjectWithDateTypes.createObject());
        map.put(ITEM_WITH_IGNORED_PROPERTIES,
                ObjectWithIgnoredProperties.createObjectWithIgnoredPropertiesNull());
        map.put(ITEM_WITH_RENAMED_PROPERTIES, ObjectWithRenamedProperties.createObject());
        return map;
    }

    @SuppressWarnings(
        { "unchecked" })
    private static LinkedHashMap createItemsObjectsSpecificMap()
    {
        LinkedHashMap map = new LinkedHashMap();
        map.put(ITEM_WITH_KNOWN_TYPE, ObjectWithTypeA.createObject());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS, ObjectWithTypeAA.createObject());
        return map;
    }

    @SuppressWarnings(
        { "unchecked" })
    private static LinkedHashMap createItemsMapsMap(ObjectCounter objectCounter,
            ObjectType objectType)
    {
        LinkedHashMap map = new LinkedHashMap();
        map.put(ITEM_WITH_KNOWN_TYPE, ObjectWithTypeA.createMap(objectCounter, ObjectType.TYPE)
                .toMap());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS,
                ObjectWithTypeAA.createMap(objectCounter, ObjectType.CLASS).toMap());
        map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                ObjectWithTypeA.createMap(objectCounter, ObjectType.CLASS).toMap());
        map.put(ITEM_WITH_UNKNOWN_TYPE,
                ObjectWithUnknownType.createMap(objectCounter, ObjectType.TYPE).toMap());
        map.put(ITEM_WITH_UNKNOWN_CLASS,
                ObjectWithUnknownType.createMap(objectCounter, ObjectType.CLASS).toMap());
        map.put(ITEM_WITH_PRIVATE_ACCESS,
                ObjectWithPrivateAccess.createMap(objectCounter, objectType).toMap());
        map.put(ITEM_WITH_PRIMITIVE_TYPES,
                ObjectWithPrimitiveTypes.createMap(objectCounter, objectType).toMap());
        map.put(ITEM_WITH_NESTED_TYPES, ObjectWithNestedTypes.createMap(objectCounter, objectType)
                .toMap());
        map.put(ITEM_WITH_ENUM_TYPES, ObjectWithEnumTypes.createMap(objectCounter, objectType)
                .toMap());
        map.put(ITEM_WITH_DATE_TYPES, ObjectWithDateTypes.createMap(objectCounter, objectType)
                .toMap());
        map.put(ITEM_WITH_IGNORED_PROPERTIES,
                ObjectWithIgnoredProperties.createMap(objectCounter, objectType).toMap());
        map.put(ITEM_WITH_RENAMED_PROPERTIES,
                ObjectWithRenamedProperties.createMap(objectCounter, objectType).toMap());
        return map;
    }

    @SuppressWarnings(
        { "unchecked" })
    private static Map createItemsMapsSpecificMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        Map map = new LinkedHashMap();
        map.put(ITEM_WITH_KNOWN_TYPE, ObjectWithTypeA.createMap(objectCounter, ObjectType.TYPE)
                .toMap());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS,
                ObjectWithTypeAA.createMap(objectCounter, ObjectType.CLASS).toMap());
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithContainerTypes casted = (ObjectWithContainerTypes) obj;
        Assert.assertEquals(collectionWithoutType, casted.collectionWithoutType);
        Assert.assertEquals(collectionWithObjectType, casted.collectionWithObjectType);
        Assert.assertEquals(collectionWithSpecificType, casted.collectionWithSpecificType);
        Assert.assertEquals(linkedHashSetWithoutType, casted.linkedHashSetWithoutType);
        Assert.assertEquals(linkedHashSetWithObjectType, casted.linkedHashSetWithObjectType);
        Assert.assertEquals(linkedHashSetWithSpecificType, casted.linkedHashSetWithSpecificType);
        Assert.assertEquals(listWithoutType, casted.listWithoutType);
        Assert.assertEquals(listWithObjectType, casted.listWithObjectType);
        Assert.assertEquals(listWithSpecificType, casted.listWithSpecificType);
        Assert.assertEquals(linkedListWithoutType, casted.linkedListWithoutType);
        Assert.assertEquals(linkedListWithObjectType, casted.linkedListWithObjectType);
        Assert.assertEquals(linkedListWithSpecificType, casted.linkedListWithSpecificType);
        Assert.assertEquals(mapWithoutType, casted.mapWithoutType);
        Assert.assertEquals(mapWithObjectType, casted.mapWithObjectType);
        Assert.assertEquals(mapWithSpecificType, casted.mapWithSpecificType);
        Assert.assertEquals(linkedHashMapWithoutType, casted.linkedHashMapWithoutType);
        Assert.assertEquals(linkedHashMapWithObjectType, casted.linkedHashMapWithObjectType);
        Assert.assertEquals(linkedHashMapWithSpecificType, casted.linkedHashMapWithSpecificType);
        Assert.assertEquals(arrayWithObjectType, casted.arrayWithObjectType);
        Assert.assertEquals(arrayWithSpecificType, casted.arrayWithSpecificType);

        return true;
    }

}
