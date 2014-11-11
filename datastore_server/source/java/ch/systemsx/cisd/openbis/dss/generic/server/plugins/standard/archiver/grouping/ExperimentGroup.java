package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

public class ExperimentGroup extends Grouper<Experiment, DataSetTypeGroup>
{
    private static final long serialVersionUID = -5529811835382975484L;

    public ExperimentGroup()
    {
        super(DataSetTypeGroup.class);
    }

}