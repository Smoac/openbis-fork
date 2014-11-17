package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Group by data set type and experiment
 * 
 * @author Sascha Fedorenko
 */
public class ExperimentAndDataSetTypeGroupKeyProvider implements IGroupKeyProvider
{

    @Override
    public String getGroupKey(AbstractExternalData dataset)
    {
        return dataset.getExperiment().getIdentifier() + "#" + dataset.getDataSetType().getCode();
    }

}
