package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

public class ExperimentGlobalSearchBridge extends GlobalSearchBridge<ExperimentPE>
{
    @Override
    public Map<String, String> collect(ExperimentPE experiment)
    {
        Map<String, String> values = new HashMap<>();

        values.put("Perm ID", experiment.getPermId());
        values.put("Code", experiment.getCode());
        values.put("Identifier", experiment.getIdentifier());
        values.put("Registration date", dateFormat.format(experiment.getRegistrationDate()));
        values.put("Modification date", dateFormat.format(experiment.getModificationDate()));

        values.put("Experiment type", experiment.getExperimentType().getCode());

        addProject(values, experiment.getProject());

        addProperties(values, experiment.getProperties());
        addAttachments(values, experiment.getAttachments());
        addPerson(values, "registrator", experiment.getRegistrator());
        addPerson(values, "modifier", experiment.getModifier());
        return values;
    }
}
