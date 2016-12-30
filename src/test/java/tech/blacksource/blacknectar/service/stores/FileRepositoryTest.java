/*
 * Copyright 2016 BlackWholeLabs.
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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class FileRepositoryTest 
{
    
    private Store store;
    
    
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;
    private IDGenerator idGenerator;
    
    private FileRepository instance;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        instance = new FileRepository(aroma, idGenerator);
    }


    private void setupData() throws Exception
    {
        store = one(stores());
    }

    @DontRepeat
    @Test
    public void testGetAllStores()
    {
        List<Store> result = instance.getAllStores();
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        assertThat(result.size(), greaterThanOrEqualTo(3000));
        assertThat(result.size(), lessThanOrEqualTo(FileRepository.MAXIMUM_STORES));
    }

    @DontRepeat
    @Test
    public void testReadCSVFile()
    {
        String file = instance.readCSVFile();
        assertThat(file, not(isEmptyOrNullString()));
    }

    @Test
    public void testSplitFileIntoLines()
    {
        List<String> lines = listOf(alphabeticString());
        
        String file = String.join("\n", lines);
        
        List<String> result = instance.splitFileIntoLines(file);
        assertThat(result, is(lines));
    }

    @Test
    public void testExtractLocationFrom()
    {
        double latitude = one(doubles(-10, 10));
        double longitude = one(doubles(-10, 10));
        
        String latitudeString = String.valueOf(latitude);
        String longitudeString = String.valueOf(longitude);
        
        Location result = instance.extractLocationFrom(latitudeString, longitudeString);
        assertThat(result.getLatitude(), is(latitude));
        assertThat(result.getLongitude(), is(longitude));
    }

    @Test
    public void testExtractZipCode()
    {
        int zipCode = one(integers(0, 99_999));
        
        int result = instance.extractZipCode(String.valueOf(zipCode));
        assertThat(result, is(zipCode));
    }

}