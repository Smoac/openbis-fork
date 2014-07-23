package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class TranslationCache
{

    /**
     * Map storing information if the given fetch options (value) has already been used for the given object (key)
     */
    private Map<Object, Set<Object>> usedFetchOptions = new IdentityHashMap<Object, Set<Object>>();

    /**
     * Map storing already translated object (value) for the given namespace and object id (key)
     */
    private Map<String, Object> translatedObjects = new HashMap<String, Object>();

    public boolean hasTranslatedObject(String namespace, Long objectId)
    {
        String key = namespace + "." + objectId;
        return translatedObjects.containsKey(key);
    }

    public Object getTranslatedObject(String namespace, Long objectId)
    {
        String key = namespace + "." + objectId;
        return translatedObjects.get(key);
    }

    public void putTranslatedObject(String namespace, Long objectId, Object object)
    {
        String key = namespace + "." + objectId;
        translatedObjects.put(key, object);
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
}
