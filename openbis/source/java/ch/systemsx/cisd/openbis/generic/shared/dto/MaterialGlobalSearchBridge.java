package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class MaterialGlobalSearchBridge extends GlobalSearchBridge<MaterialPE>
{
    @Override
    public Map<String, IndexedValue> collect(MaterialPE material)
    {
        Map<String, IndexedValue> values = new HashMap<>();

        put(values, "Identifier", material.getPermId());
        put(values, "Registration date", dateFormat.format(material.getRegistrationDate()));
        put(values, "Modification date", dateFormat.format(material.getModificationDate()));

        put(values, "Material type", material.getMaterialType().getCode());

        addProperties(values, material.getProperties());
        addPerson(values, "registrator", material.getRegistrator());
        return values;
    }
}
