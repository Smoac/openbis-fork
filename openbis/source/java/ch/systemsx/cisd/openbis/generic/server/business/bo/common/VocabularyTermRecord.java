package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;

/**
 * A record object for a vocabulary term.
 */
@Friend(toClasses=BaseEntityPropertyRecord.class)
@Private
public class VocabularyTermRecord extends BaseEntityPropertyRecord
{
    public long id;

    public long covo_id;

    public String code;

    public String label;
}