package tech.blacksource.blacknectar.service.json;

import com.google.gson.Gson;
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
     * Attempts to deserialize the JSON String into a {@link Field}.
     *
     * @param json The json to parse. Cannot be empty.
     * @return The corresponding {@link Field}, or null if it cannot be parsed.
     * @throws BlackNectarAPIException
     */
    @Optional
    Field deserializeField(@NonEmpty String json) throws BlackNectarAPIException;

    /**
     * Serializes a {@link FieldValue} into a JSON Object.
     *
     * @param fieldValue The fieldValue to serialize
     * @return A {@link JsonObject} representing the field.
     * @throws BlackNectarAPIException
     */
    JsonObject serializeFieldValue(@Required FieldValue fieldValue) throws BlackNectarAPIException;

    /**
     * Attempts to deserialize the JSON String into a {@link FieldValue}.
     *
     * @param json The json to parse. Cannot be empty.
     * @return The corresponding {@link FieldValue}, or null if it cannot be parsed.
     * @throws BlackNectarAPIException
     */
    @Optional
    FieldValue deserializeFieldValue(@NonEmpty String json) throws BlackNectarAPIException;

    //==========================================================
    //| Static Initializers |
    //==========================================================

    /**
     * @return A default {@link EBTJsonSerializer}.
     */
    static EBTJsonSerializer newInstance()
    {
        return newInstance(Aroma.createNoOpInstance(), JSON.GSON);
    }

    /**
     * Allows creating a default instance of {@link EBTJsonSerializer} using a customized {@link Gson}.
     *
     * @param gson The {@link Gson} object to use for serialization. Cannot be null.
     * @return A customized {@link EBTJsonSerializer}.
     */
    static EBTJsonSerializer newInstance(@Required Gson gson)
    {
        return newInstance(Aroma.createNoOpInstance(), gson);
    }

    /**
     * Allows creating a default instance of {@link EBTJsonSerializer} using a customized {@link Aroma}.
     *
     * @param aroma The {@link Aroma} to use. Cannot be null.
     * @return A customized {@link EBTJsonSerializer}.
     */
    static EBTJsonSerializer newInstance(@Required Aroma aroma)
    {
        return newInstance(aroma, JSON.GSON);
    }

    /**
     * Allows creating a customized instance of {@link EBTJsonSerializer} using a custom
     * {@link Aroma} and {@link Gson} instance.
     *
     * @param aroma The custom Aroma instance to use.
     * @param gson  The custom Gson instance ot use.
     * @return A customized instance of {@link EBTJsonSerializer}.
     */
    static EBTJsonSerializer newInstance(@Required Aroma aroma, @Required Gson gson)
    {
        return new EBTJsonSerializerImpl(aroma, gson);
    }

}
