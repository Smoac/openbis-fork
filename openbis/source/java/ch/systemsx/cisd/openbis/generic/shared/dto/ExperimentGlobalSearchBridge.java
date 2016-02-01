package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class ExperimentGlobalSearchBridge extends GlobalSearchBridge<ExperimentPE>
{
    @Override
    public Map<String, IndexedValue> collect(ExperimentPE experiment)
    {
        Map<String, IndexedValue> values = new HashMap<>();

        put(values, "Perm ID", experiment.getPermId());
        put(values, "Code", experiment.getCode());
        put(values, "Identifier", experiment.getIdentifier());
        put(values, "Registration date", dateFormat.format(experiment.getRegistrationDate()));
        put(values, "Modification date", dateFormat.format(experiment.getModificationDate()));

        put(values, "Experiment type", experiment.getExperimentType().getCode());

        addProject(values, experiment.getProject());

        addProperties(values, experiment.getProperties());
        addAttachments(values, experiment.getAttachments());
        addPerson(values, "registrator", experiment.getRegistrator());
        addPerson(values, "modifier", experiment.getModifier());
        return values;
    }
}
