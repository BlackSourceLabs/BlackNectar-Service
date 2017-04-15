package tech.blacksource.blacknectar.service.exceptions;

/**
 * Thrown when performing an operation on a {@link tech.blacksource.blacknectar.ebt.balance.State}
 * that is not supported.
 *
 * @author SirWellingt
 */
public final class UnsupportedStateException extends BlackNectarAPIException
{
    private final String state;

    public UnsupportedStateException()
    {
        this.state = "";
    }

    public UnsupportedStateException(String state)
    {
        super(state);
        this.state = state;
    }

    public UnsupportedStateException(String message, String state)
    {
        super(message);
        this.state = state;
    }

    public UnsupportedStateException(String message, Throwable cause, String state)
    {
        super(message, cause);
        this.state = state;
    }

    public UnsupportedStateException(Throwable cause, String state)
    {
        super(cause);
        this.state = state;
    }
}
