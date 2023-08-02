package ch.ethz.sis.openbis.generic.server.transaction;

public interface ISessionTokenProvider
{
    boolean isValid(String sessionToken);
}
