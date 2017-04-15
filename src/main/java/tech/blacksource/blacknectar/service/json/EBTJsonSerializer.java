package tech.blacksource.blacknectar.service.json;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.sirwellington.alchemy.annotations.arguments.Required;

/**
 * Responsible for serializing and deserializing classes from the
 * EBT Balance library, to and from JSON.
 *
 * @author SirWellington
 */
@ImplementedBy(EBTJsonSerializerImpl.class)
public interface EBTJsonSerializer
{

    /**
     * Serializes a {@link State} into a JSON Object.
     *
     * @param state The {@link State} to serialize. Cannot be null.
     * @return A {@linkplain JsonObject JSON representation} of the {@link State}.
     */
    JsonObject serializeState(@Required State state);

    static EBTJsonSerializer newInstance()
    {
        return new EBTJsonSerializerImpl();
    }

}
