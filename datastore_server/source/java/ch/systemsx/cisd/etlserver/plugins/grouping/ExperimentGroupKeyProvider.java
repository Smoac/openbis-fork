package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class ExperimentGroupKeyProvider implements IGroupKeyProvider
{

    @Override
    public String getGroupKey(AbstractExternalData dataset)
    {
        return dataset.getExperiment().getIdentifier();
    }

}
