package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class SortableNumberBridge implements TwoWayFieldBridge {
    @Override
    public void set(
            String name, 
            Object value,
            Document document, 
            LuceneOptions luceneOptions)
    {
    }
    
    @Override
    public String objectToString(Object object) {
        return null;
    }
    
    @Override
    public Object get(String name, Document document)
    {
        return null;
    }
}