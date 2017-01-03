/*
 * Copyright 2017 BlackSource, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.blacksource.blacknectar.service.stores;

import com.google.gson.JsonObject;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateURL;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class StoreTest 
{

    private Store instance;
    
    private Store other;
    
    private Store copy;
    
    @GenerateURL
    private URL mainImageURL;
    
    private String mainImage;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }


    private void setupData() throws Exception
    {
        instance = one(stores());
        other = one(stores());
        copy = Store.Builder.fromStore(instance).build();
        
        mainImage = mainImageURL.toString();
    }

    @Test
    public void testAsJSON()
    {
        JsonObject json = instance.asJSON();
        assertThat(json, notNullValue());
        assertThat(json.get(Store.Keys.ADDRESS), is(instance.getAddress().asJSON()));
        assertThat(json.get(Store.Keys.LOCATION), is(instance.getLocation().asJSON()));
        assertThat(json.get(Store.Keys.NAME).getAsString(), is(instance.getName()));
        assertThat(json.get(Store.Keys.STORE_ID).getAsString(), is(instance.getStoreId()));
        
        if (instance.hasMainImage())
        {
            assertThat(json.get(Store.Keys.MAIN_IMAGE).getAsString(), is(instance.getMainImageURL()));
        }
    }
    
    @Test
    public void testThatImageCanBeAddedToStore()
    {
        Store newStore = Store.Builder.fromStore(instance)
            .withMainImageURL(mainImage)
            .build();
        
        assertThat(newStore, notNullValue());
        assertThat(newStore.getMainImageURL(), is(mainImage));
        assertThat(newStore, not(instance));
        assertThat(newStore.getStoreId(), is(instance.getStoreId()));
        assertThat(newStore.getName(), is(instance.getName()));
        assertThat(newStore.getLocation(), is(instance.getLocation()));
        assertThat(newStore.getAddress(), is(instance.getAddress()));
    }
    
    @DontRepeat
    @Test
    public void testBuilderWithNothingSet() throws Exception
    {
        Store.Builder builder = Store.Builder.newInstance();
        
        assertThrows(() -> builder.build());
    }
    
    @DontRepeat
    @Test
    public void testBuilderWithInvalidArguments() throws Exception
    {
        Store.Builder builder = Store.Builder.newInstance();
        
        assertThrows(() -> builder.withAddress(null)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withLocation(null)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withName("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withName(null)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withMainImageURL("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withMainImageURL(null)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> builder.withStoreID(null)).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testEquals()
    {
        assertThat(instance, is(copy));
        assertThat(instance, not(other));
    }

    @Test
    public void testEqualsWhenDifferent()
    {
        other = Store.Builder.fromStore(instance)
            .withMainImageURL(mainImage)
            .build();

        assertThat(other, not(instance));
    }

    @Test
    public void testValidStore()
    {
        AlchemyAssertion<Store> assertion = Store.validStore();
        assertThat(assertion, notNullValue());
        
        assertion.check(instance);
        assertion.check(copy);
        assertion.check(other);
    }

    @Test
    public void testHashCode()
    {
        assertThat(instance.hashCode(), is(copy.hashCode()));
        assertThat(instance.hashCode(), not(other.hashCode()));
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), is(copy.toString()));
        assertThat(instance.toString(), not(other.toString()));
    }
}
