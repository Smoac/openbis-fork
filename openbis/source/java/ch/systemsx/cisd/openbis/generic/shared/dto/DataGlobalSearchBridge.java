package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class DataGlobalSearchBridge extends GlobalSearchBridge<DataPE>
{

    @Override
    public Map<String, String> collect(DataPE data)
    {
        Map<String, String> values = new HashMap<>();

        values.put("Perm ID", data.getPermId());
        values.put("Registration date", dateFormat.format(data.getRegistrationDate()));
        values.put("Modification date", dateFormat.format(data.getModificationDate()));
        values.put("Access date", dateFormat.format(data.getAccessDate()));
        values.put("DataSet type", data.getDataSetType().getCode());
        addProperties(values, data.getProperties());
        addPerson(values, "registrator", data.getRegistrator());
        addPerson(values, "modifier", data.getModifier());
        return values;
    }
}
