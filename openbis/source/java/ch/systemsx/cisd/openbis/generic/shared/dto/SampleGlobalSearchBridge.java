package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class SampleGlobalSearchBridge extends GlobalSearchBridge<SamplePE>
{
    @Override
    public Map<String, IndexedValue> collect(SamplePE sample)
    {
        Map<String, IndexedValue> values = new HashMap<>();

        put(values, "Perm ID", sample.getPermId());
        put(values, "Code", sample.getCode());
        put(values, "Identifier", sample.getIdentifier());
        put(values, "Registration date", dateFormat.format(sample.getRegistrationDate()));
        put(values, "Modification date", dateFormat.format(sample.getModificationDate()));
        put(values, "Sample type", sample.getSampleType().getCode());

        if (sample.getExperiment() != null)
        {
            addExperiment(values, sample.getExperiment());
        } else if (sample.getSpace() != null)
        {
            addSpace(values, sample.getSpace());
        }

        addProperties(values, sample.getProperties());
        addAttachments(values, sample.getAttachments());
        addPerson(values, "registrator", sample.getRegistrator());
        addPerson(values, "modifier", sample.getModifier());
        return values;
    }
}
