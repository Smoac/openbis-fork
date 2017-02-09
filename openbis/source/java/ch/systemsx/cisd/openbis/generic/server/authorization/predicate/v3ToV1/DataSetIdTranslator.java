package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

public class DataSetIdTranslator {
	
	public static String translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId v3datasetId) {
		if(v3datasetId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId) {
			
		} else if(v3datasetId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId) {
			return ((ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId) v3datasetId).getPermId();
		}
		
		return null;
	}
}
