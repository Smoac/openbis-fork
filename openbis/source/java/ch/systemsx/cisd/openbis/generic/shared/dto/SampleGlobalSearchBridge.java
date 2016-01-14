package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class SampleGlobalSearchBridge extends GlobalSearchBridge<SamplePE>
{
    @Override
    public Map<String, String> collect(SamplePE sample)
    {
        Map<String, String> values = new HashMap<>();

        values.put("Perm ID", sample.getPermId());
        values.put("Identifier", sample.getIdentifier());
        values.put("Code", sample.getCode());
        values.put("Registration date", dateFormat.format(sample.getRegistrationDate()));
        values.put("Modification date", dateFormat.format(sample.getModificationDate()));

        values.put("Sample type", sample.getSampleType().getCode());

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
