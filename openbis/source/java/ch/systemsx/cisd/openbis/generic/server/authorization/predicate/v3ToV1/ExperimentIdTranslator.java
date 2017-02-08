package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;

public class ExperimentIdTranslator {
	
	public static IExperimentId translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId v3ExperimentId) {
		if(v3ExperimentId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId) {
			
		} else if(v3ExperimentId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier) {
			return new ExperimentIdentifierId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier)v3ExperimentId).getIdentifier());
		} else if(v3ExperimentId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId) {
			return new ExperimentPermIdId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId)v3ExperimentId).getPermId());
		}
		
		return null;
	}
}
