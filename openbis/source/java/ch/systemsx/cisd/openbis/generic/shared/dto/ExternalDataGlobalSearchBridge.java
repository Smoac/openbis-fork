package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Map;

public class ExternalDataGlobalSearchBridge extends GlobalSearchBridge<ExternalDataPE>
{

    @Override
    public Map<String, String> collect(ExternalDataPE data)
    {
        DataGlobalSearchBridge db = new DataGlobalSearchBridge();
        Map<String, String> values = db.collect(data);
        values.put("Storage confirmed", data.isStorageConfirmation() ? "true" : "false");
        if (data.getFileFormatType() != null)
        {
            values.put("File format type", data.getFileFormatType().getCode());
        }
        return values;
    }
}
