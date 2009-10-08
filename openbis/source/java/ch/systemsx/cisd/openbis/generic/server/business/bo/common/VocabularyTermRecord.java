package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

/**
 * A record object for a vocabulary term.
 */
public class VocabularyTermRecord extends BaseEntityPropertyRecord
{
    public long id;

    public long covo_id;

    public String code;

    public String label;

    public long ordinal;
}
