package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.IdListUpdateValue")
public class IdListUpdateValue<T> extends ListUpdateValue<T, T, T, T>
{
    private static final long serialVersionUID = 1L;

}
