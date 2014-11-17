package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Group by experiment
 * 
 * @author Sascha Fedorenko
 */
public class ExperimentGroupKeyProvider implements IGroupKeyProvider
{

    @Override
    public String getGroupKey(AbstractExternalData dataset)
    {
        return dataset.getExperiment().getIdentifier();
    }

}
