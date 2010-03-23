package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Checks the status.
 * 
 * @author Izabela Adamczyk
 */
public interface IStatusChecker
{
    Status check();
}