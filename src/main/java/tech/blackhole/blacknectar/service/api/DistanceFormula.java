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


package tech.blackhole.blacknectar.service.api;

import com.google.inject.ImplementedBy;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;


/**
 * A {@code DistanceFormula} is responsible for calculating the distance between
 * two {@linkplain Location Points}.
 * 
 * @author SirWellington
 */
@ImplementedBy(DistanceFormula.HarvesineDistance.class)
interface DistanceFormula 
{
    /**
     * Calculates the distance, in meters, between {@code first} and {@code second}.
     * 
     * @param first
     * @param second
     * 
     * @return The distance, in meters.
     */
    double distanceBetween(@Required Location first, @Required Location second);
    
    
    /**
     * This Singleton {@link DistanceFormula} computes the distance between two points
     * using the Harvesine formula.
     */
    static DistanceFormula HARVESINE = new HarvesineDistance();
    
    /**
     * Uses the <a href="https://rosettacode.org/wiki/Haversine_formula">Harvesine Formula</a> to calculate
     * the distance between two points.
     * <p>
     * The Harvesine formula is not completely accurate; This is because the Earth is not a perfect sphere; it is an oblate-spheroid.
     * It is designed to work on perfect spheres.
     * 
     * @see <a href="https://rosettacode.org/wiki/Haversine_formula">https://rosettacode.org/wiki/Haversine_formula</a>
     */
    static class HarvesineDistance implements DistanceFormula
    {

        private static final double RADIUS_OF_EARTH_IN_KILOMETERS = 6372.8;
        
        @Override
        public double distanceBetween(Location first, Location second)
        {
            checkThat(first, second)
                .usingMessage("Location objects cannot be null")
                .are(notNull());
            
            final double latitudeDelta = Math.toRadians(first.getLatitude() - second.getLatitude());
            final double longitudeDelta = Math.toRadians(first.getLongitude() - second.getLongitude());
            
            final double firstLatitude = Math.toRadians(first.getLatitude());
            final double secondLatitude = Math.toRadians(second.getLatitude());
            
            final double harvesineOfLatitude = Math.pow(Math.sin(latitudeDelta / 2), 2);
            final double harvesineOfLongitude = Math.pow(Math.sin(longitudeDelta / 2), 2);
            final double firstCosine = Math.cos(firstLatitude);
            final double secondConsine = Math.cos(secondLatitude);
            
            final double harvesine = harvesineOfLatitude +(firstCosine * secondConsine * harvesineOfLongitude);
            final double inverseHarvesine = Math.asin(Math.sqrt(harvesine));
            
            final double distance = 2 * inverseHarvesine * RADIUS_OF_EARTH_IN_KILOMETERS * 1_000;
            
            return distance;
        }
        
    }
}
