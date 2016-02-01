package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Map;

public class ExternalDataGlobalSearchBridge extends GlobalSearchBridge<ExternalDataPE>
{

    @Override
    public Map<String, IndexedValue> collect(ExternalDataPE data)
    {
        DataGlobalSearchBridge<ExternalDataPE> db = new DataGlobalSearchBridge<>();
        Map<String, IndexedValue> values = db.collect(data);
        if (data.getFileFormatType() != null)
        {
            put(values, "File format type", data.getFileFormatType().getCode());
        }
        return values;
    }
}
