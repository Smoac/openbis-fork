package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TranslationCache
{

    private Map<String, CacheEntry> entries = new HashMap<String, CacheEntry>();

    public CacheEntry getEntry(String identifier)
    {
        CacheEntry cacheEntry = entries.get(identifier);

        if (cacheEntry == null)
        {
            cacheEntry = new CacheEntry();
            entries.put(identifier, cacheEntry);
        }

        return cacheEntry;
    }

    public static class CacheEntry
    {

        private boolean shouldTranslateSet;

        private Boolean shouldTranslate;

        private boolean translatedObjectSet;

        private Object translatedObject;

        private Set<Object> usedFetchOptions;

        private CacheEntry()
        {
        }

        public boolean isShouldTranslateSet()
        {
            return shouldTranslateSet;
        }

        public Boolean getShouldTranslate()
        {
            return shouldTranslate;
        }

        public void setShouldTranslate(boolean shouldTranslate)
        {
            this.shouldTranslate = shouldTranslate;
            this.shouldTranslateSet = true;
        }

        public boolean isTranslatedObjectSet()
        {
            return translatedObjectSet;
        }

        public Object getTranslatedObject()
        {
            return translatedObject;
        }

        public void setTranslatedObject(Object translatedObject)
        {
            this.translatedObject = translatedObject;
            this.translatedObjectSet = true;
        }

        public boolean isTranslatedWithFetchOptions(Object fetchOptions)
        {
            if (usedFetchOptions == null)
            {
                return false;
            } else
            {
                return usedFetchOptions.contains(fetchOptions);
            }
        }

        public void addTranslatedWithFetchOptions(Object fetchOptions)
        {
            if (usedFetchOptions == null)
            {
                usedFetchOptions = new HashSet<Object>();
            }
            usedFetchOptions.add(fetchOptions);
        }

    }

}
