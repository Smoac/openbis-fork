package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

public class LinkDataGlobalSearchBridge extends GlobalSearchBridge<LinkDataPE>
{

    @Override
    public Map<String, IndexedValue> collect(LinkDataPE data)
    {
        DataGlobalSearchBridge<LinkDataPE> db = new DataGlobalSearchBridge<>();
        Map<String, IndexedValue> values = db.collect(data);
        put(values, "External code", data.getExternalCode());
        if (data.getExternalDataManagementSystem() != null)
        {
            put(values, "External dms", data.getExternalDataManagementSystem().getCode());
        }
        return values;
    }

    @Override
    protected boolean shouldIndex(String name, Object value, Document document, LuceneOptions luceneOptions)
    {
        return true;
    }
}
