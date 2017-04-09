package tech.blacksource.blacknectar.service.exceptions;

/**
 * Thrown when performing an operation on a {@link tech.blacksource.blacknectar.ebt.balance.State}
 * that is not supported.
 *
 * @author SirWellingt
 */
public final class UnsupportedStateException extends BlackNectarAPIException
{
    public UnsupportedStateException()
    {
    }

    public UnsupportedStateException(String message)
    {
        super(message);
    }

    public UnsupportedStateException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnsupportedStateException(Throwable cause)
    {
        super(cause);
    }
}
