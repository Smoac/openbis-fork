package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ISessionTokenProvider
{
    boolean isValid(String sessionToken);
}
