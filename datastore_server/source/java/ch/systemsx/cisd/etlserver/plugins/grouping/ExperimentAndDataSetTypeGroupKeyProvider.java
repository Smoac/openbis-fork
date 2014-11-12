package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class ExperimentAndDataSetTypeGroupKeyProvider implements IGroupKeyProvider
{

    @Override
    public String getGroupKey(AbstractExternalData dataset)
    {
        return dataset.getExperiment().getIdentifier() + "#" + dataset.getDataSetType().getCode();
    }

}
