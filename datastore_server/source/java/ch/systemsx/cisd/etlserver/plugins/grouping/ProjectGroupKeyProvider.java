package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

public class ProjectGroupKeyProvider implements IGroupKeyProvider
{

    @Override
    public String getGroupKey(AbstractExternalData dataset)
    {
        ExperimentIdentifier experimentIdentifier = ExperimentIdentifierFactory.parse(dataset.getExperiment().getIdentifier());
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(experimentIdentifier.getSpaceCode(), experimentIdentifier.getProjectCode());
        return projectIdentifier.toString();
    }

}
