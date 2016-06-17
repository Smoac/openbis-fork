package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

public class DataGlobalSearchBridge<T extends DataPE> extends GlobalSearchBridge<T>
{

    @Override
    public Map<String, IndexedValue> collect(DataPE data)
    {
        Map<String, IndexedValue> values = new HashMap<>();

        put(values, "Perm ID", data.getPermId());
        put(values, "Registration date", dateFormat.format(data.getRegistrationDate()));
        put(values, "Modification date", dateFormat.format(data.getModificationDate()));
        put(values, "DataSet type", data.getDataSetType().getCode());
        addProperties(values, data.getProperties());
        addPerson(values, "registrator", data.getRegistrator());
        addPerson(values, "modifier", data.getModifier());
        return values;
    }

    @Override
    protected boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        if (this.getClass().equals(DataGlobalSearchBridge.class) && ((value instanceof ExternalDataPE) || (value instanceof LinkDataPE)))
        {
            return false;
        } else
        {
            return true;
        }
    }
}
