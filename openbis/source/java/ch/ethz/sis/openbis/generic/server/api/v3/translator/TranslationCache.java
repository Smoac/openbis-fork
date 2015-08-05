package ch.ethz.sis.openbis.generic.server.api.v3.translator;

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
}
