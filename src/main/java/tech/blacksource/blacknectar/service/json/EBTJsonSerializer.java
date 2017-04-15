package tech.blacksource.blacknectar.service.json;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.sirwellington.alchemy.annotations.arguments.*;

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
    JsonObject serializeState(@Required State state) throws BlackNectarAPIException;

    /**
     * Serializes a {@link Field} into a JSON Object.
     *
     * @param field The field to serialize
     * @return A {@link JsonObject} representation of the field
     * @throws BlackNectarAPIException
     */
    JsonObject serializeField(@Required Field field) throws BlackNectarAPIException;

    /**
     * Attempts to deserialize the JSON String into a {@link FieldValue}.
     *
     * @param json The json to parse. Cannot be empty.
     * @return The corresponding {@link FieldValue}, or null if it cannot be parsed.
     * @throws BlackNectarAPIException
     */
    @Optional
    FieldValue deserializeFieldValue(@NonEmpty String json) throws BlackNectarAPIException;

    static EBTJsonSerializer newInstance(@Required Aroma aroma)
    {
        return new EBTJsonSerializerImpl();
    }

}
