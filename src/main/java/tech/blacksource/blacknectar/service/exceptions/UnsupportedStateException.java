package tech.blacksource.blacknectar.service.exceptions;

import tech.blacksource.blacknectar.ebt.balance.State;

/**
 * Thrown when performing an operation on a {@link tech.blacksource.blacknectar.ebt.balance.State}
 * that is not supported.
 *
 * @author SirWellingt
 */
public final class UnsupportedStateException extends BlackNectarAPIException
{
    private final State state;

    public UnsupportedStateException()
    {
        this.state = null;
    }

    public UnsupportedStateException(State state)
    {
        super(state.toString());
        this.state = state;
    }

    public UnsupportedStateException(String message, State state)
    {
        super(message);
        this.state = state;
    }

    public UnsupportedStateException(String message, Throwable cause, State state)
    {
        super(message, cause);
        this.state = state;
    }

    public UnsupportedStateException(Throwable cause, State state)
    {
        super(cause);
        this.state = state;
    }

    public State getState()
    {
        return state;
    }
}
