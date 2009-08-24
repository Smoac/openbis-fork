package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.rinn.restrictions.Private;

/**
 * A record object for an entity property of type MATERIAL.
 */
@Private
public class MaterialEntityPropertyRecord extends BaseEntityPropertyRecord
{
    public long id;

    public long maty_id;

    public String code;
}