package ch.systemsx.cisd.etlserver.registrator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * A container for a jython dropbox that can be used to store information during the registration
 * process. This is the suggested way for the users of dropboxes to share some data beetween the
 * dropbox scripts and and the functions like post_registration, post_storage etc.
 * 
 * @author jakubs
 */
public class DataSetRegistrationPersistentMap implements Serializable
{
    public static interface IHolder
    {
        DataSetRegistrationPersistentMap getPersistentMap();
    }
    
    private static final long serialVersionUID = 1L;
    
    private HashMap<String, Serializable> persistentMap;
    
    public DataSetRegistrationPersistentMap()
    {
        persistentMap  = new HashMap<String, Serializable>();
    }

    public int size()
    {
        return persistentMap.size();
    }

    public boolean isEmpty()
    {
        return persistentMap.isEmpty();
    }

    public Serializable get(Object key)
    {
        return persistentMap.get(key);
    }

    public boolean containsKey(Object key)
    {
        return persistentMap.containsKey(key);
    }

    public Serializable put(String key, Serializable value)
    {
        return persistentMap.put(key, value);
    }

    public Serializable remove(Object key)
    {
        return persistentMap.remove(key);
    }

    public void clear()
    {
        persistentMap.clear();
    }

    public boolean containsValue(Object value)
    {
        return persistentMap.containsValue(value);
    }

    public Set<String> keySet()
    {
        return persistentMap.keySet();
    }

    public Collection<Serializable> values()
    {
        return persistentMap.values();
    }
    
}
