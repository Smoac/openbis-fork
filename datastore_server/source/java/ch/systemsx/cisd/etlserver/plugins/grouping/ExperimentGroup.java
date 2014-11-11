package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

public class ExperimentGroup extends Grouper<Experiment, DataSetTypeGroup>
{
    private static final long serialVersionUID = -5529811835382975484L;

    public ExperimentGroup()
    {
        super(DataSetTypeGroup.class);
    }

}