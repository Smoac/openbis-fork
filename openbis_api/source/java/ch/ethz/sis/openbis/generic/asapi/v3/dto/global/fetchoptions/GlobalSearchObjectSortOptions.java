package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.global.fetchoptions.GlobalSearchObjectSortOptions")
public class GlobalSearchObjectSortOptions extends SortOptions<GlobalSearchObject>
{

    private static final long serialVersionUID = 1L;

}