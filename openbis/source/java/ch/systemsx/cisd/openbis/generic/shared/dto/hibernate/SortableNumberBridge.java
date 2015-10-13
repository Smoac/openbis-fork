package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

import ch.systemsx.cisd.openbis.generic.shared.basic.utils.SortableNumberBridgeUtils;

public class SortableNumberBridge implements TwoWayFieldBridge {
    @Override
    public void set(
            String name, 
            Object value,
            Document document, 
            LuceneOptions luceneOptions)
    {
        String fieldPrefix = name;
        String fieldValue = SortableNumberBridgeUtils.getNumberForLucene((Number) value);
        Field field = new Field(fieldPrefix, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex());
        field.setBoost(luceneOptions.getBoost());
        document.add(field);
    }
    
    @Override
    public String objectToString(Object object) {
        String value = null;
        if(object instanceof String) {
            value = SortableNumberBridgeUtils.getNumberForLucene((String) object);
        } else if(object instanceof Number) {
            value = SortableNumberBridgeUtils.getNumberForLucene((Number) object);
        } else {
            value = object.toString();
        }
        return value;
    }
    
    @Override
    public Object get(String name, Document document)
    {
        String number = document.getField(name).stringValue();
        try {
            return Long.parseLong(number);
        } catch(Exception ex) {
            try {
                return Double.parseDouble(number);
            } catch(Exception ex2) {
                return number;
            }
        }
    }
}