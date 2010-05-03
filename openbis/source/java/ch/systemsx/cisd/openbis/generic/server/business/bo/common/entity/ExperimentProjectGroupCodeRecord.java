package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import ch.rinn.restrictions.Private;

/**
 * A class representing an experiment, project and group code. It contains also code of experiment
 * type and database instance id.
 */
@Private
public class ExperimentProjectGroupCodeRecord
{
    public String e_code;

    public String e_permid;

    public String et_code;

    public String p_code;

    public String g_code;

    public Long p_id;

    public Long dbin_id;
}
