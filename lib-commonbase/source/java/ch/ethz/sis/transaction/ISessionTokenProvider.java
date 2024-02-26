package ch.ethz.sis.transaction;

public interface ISessionTokenProvider
{
    boolean isValid(String sessionToken);
}
