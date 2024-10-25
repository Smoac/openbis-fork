package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.common.interfaces.ICodeHolder")
public interface IManagedInternallyHolder {
    Boolean isManagedInternally();
}
