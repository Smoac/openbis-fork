package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class TranslationCache
{

    /**
     * Map storing information whether an object with the given namespace and object id (key) should be translated (value)
     */
    private Map<String, Boolean> shouldTranslateObjects = new HashMap<String, Boolean>();

    /**
     * Map storing information if the given fetch options (value) has already been used for the given object (key)
     */
    private Map<Object, Set<Object>> usedFetchOptions = new IdentityHashMap<Object, Set<Object>>();

    /**
     * Map storing already translated object (value) for the given namespace and object id (key)
     */
    private Map<String, Object> translatedObjects = new HashMap<String, Object>();

    /**
     * Map storing already translated collections (value) for the given namespace, object ids (key) and fetchOptions
     */
    private Map<String, Map<Object, Object>> translatedCollections = new HashMap<String, Map<Object, Object>>();

    public boolean hasShouldTranslateObject(String namespace, Long objectId)
    {
        return shouldTranslateObjects.containsKey(getObjectKey(namespace, objectId));
    }

    public boolean getShouldTranslateObject(String namespace, Long objectId)
    {
        return shouldTranslateObjects.get(getObjectKey(namespace, objectId));
    }

    public void putShouldTranslateObject(String namespace, Long objectId, boolean shouldTranslate)
    {
        shouldTranslateObjects.put(getObjectKey(namespace, objectId), shouldTranslate);
    }

    public boolean hasTranslatedCollection(String namespace, Collection<Long> objectIds, Object fetchOptions)
    {
        return translatedCollections.containsKey(getObjectsKey(namespace, objectIds, fetchOptions));
    }

    public Map<Object, Object> getTranslatedCollection(String namespace, Collection<Long> objectIds, Object fetchOptions)
    {
        return translatedCollections.get(getObjectsKey(namespace, objectIds, fetchOptions));
    }

    public void putTranslatedCollection(String namespace, Collection<Long> objectIds, Object fetchOptions, Map<Object, Object> objects)
    {
        translatedCollections.put(getObjectsKey(namespace, objectIds, fetchOptions), objects);
    }

    public boolean hasTranslatedObject(String namespace, Long objectId)
    {
        return translatedObjects.containsKey(getObjectKey(namespace, objectId));
    }

    public Object getTranslatedObject(String namespace, Long objectId)
    {
        return translatedObjects.get(getObjectKey(namespace, objectId));
    }

    public void putTranslatedObject(String namespace, Long objectId, Object object)
    {
        translatedObjects.put(getObjectKey(namespace, objectId), object);
    }

    public boolean isFetchedWithOptions(Object object, Object fetchOptions)
    {
        return usedFetchOptions.containsKey(object) && usedFetchOptions.get(object).contains(fetchOptions);
    }

    public void setFetchedWithOptions(Object object, Object fetchOptions)
    {
        if (false == usedFetchOptions.containsKey(object))
        {
            usedFetchOptions.put(object, new HashSet<Object>());
        }
        usedFetchOptions.get(object).add(fetchOptions);
    }

    private String getObjectKey(String namespace, Long objectId)
    {
        return namespace + "." + objectId;
    }

    private String getObjectsKey(String namespace, Collection<Long> objectIds, Object fetchOptions)
    {
        // TODO compare fetch options
        return namespace + "." + objectIds.toString();
    }

}
