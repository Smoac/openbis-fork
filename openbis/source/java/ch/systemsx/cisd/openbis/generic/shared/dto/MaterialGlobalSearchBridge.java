package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class MaterialGlobalSearchBridge extends GlobalSearchBridge<MaterialPE>
{
    @Override
    public Map<String, String> collect(MaterialPE material)
    {
        Map<String, String> values = new HashMap<>();

        values.put("Identifier", material.getIdentifier());
        values.put("Registration date", dateFormat.format(material.getRegistrationDate()));
        values.put("Modification date", dateFormat.format(material.getModificationDate()));

        values.put("Material type", material.getMaterialType().getCode());

        addProperties(values, material.getProperties());
        addPerson(values, "registrator", material.getRegistrator());
        return values;
    }
}
