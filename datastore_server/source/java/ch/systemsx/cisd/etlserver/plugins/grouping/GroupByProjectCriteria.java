package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

public class GroupByProjectCriteria implements GroupCriteria
{

    @Override
    public String group(AbstractExternalData dataset)
    {
        return dataset.getExperiment().getProject().getCode();
    }

}
