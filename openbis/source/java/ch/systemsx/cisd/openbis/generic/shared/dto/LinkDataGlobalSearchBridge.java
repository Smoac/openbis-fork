package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Map;

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
}
