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

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static tech.blackhole.blacknectar.service.stores.Location.validLocation;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;


/**
 * A {@code GeoCalculator} is responsible for performing various geodetic calculations, like the distance between two
 * {@linkplain Location Points}.
 *
 * @author SirWellington
 */
@ImplementedBy(GeoCalculator.HarvesineCalculator.class)
public interface GeoCalculator 
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
     * Calculates a Coordinate from the given parameters.
     * 
     * @param origin The starting coordinate to base the calculation off.
     * @param distanceInMeters The distance from the origin, in meters.
     * @param bearing The bearing, in degrees, to calculate the point.
     * 
     * @return The geo-coordinate that is {@code distanceInMeters} meters away from {@code origin} in direction of {@code bearing}.
     */
    Location calculateDestinationFrom(Location origin, double distanceInMeters, double bearing);
    
    /**
     * Given two points, calculates the bearing between them.
     * 
     * @param start The starting point
     * @param end The ending point
     * 
     * @return 
     */
    double calculateBearingFromTo(@Required Location start, @Required Location end);
    
    
    /**
     * This Singleton {@link GeoCalculator} computes the distance between two points
     * using the Harvesine formula.
     */
    static GeoCalculator HARVESINE = new HarvesineCalculator();
    
    /**
     * Uses the <a href="https://rosettacode.org/wiki/Haversine_formula">Harvesine Formula</a> to calculate
     * the distance between two points.
     * <p>
     * The Harvesine formula is not completely accurate; This is because the Earth is not a perfect sphere; it is an oblate-spheroid.
     * It is designed to work on perfect spheres.
     * 
     * @see <a href="https://rosettacode.org/wiki/Haversine_formula">https://rosettacode.org/wiki/Haversine_formula</a>
     */
    static class HarvesineCalculator implements GeoCalculator
    {

        private static final double RADIUS_OF_EARTH_IN_KILOMETERS = 6372.8;
        private static final double RADIUS_OF_EARTH_IN_METERS = RADIUS_OF_EARTH_IN_KILOMETERS * 1_000;
        
        @Override
        public double distanceBetween(Location first, Location second)
        {
            checkThat(first, second)
                .usingMessage("Location objects cannot be null")
                .are(notNull());
            
            final double latitudeDelta = toRadians(first.getLatitude() - second.getLatitude());
            final double longitudeDelta = toRadians(first.getLongitude() - second.getLongitude());
            
            final double firstLatitude = toRadians(first.getLatitude());
            final double secondLatitude = toRadians(second.getLatitude());
            
            final double harvesineOfLatitude = pow(sin(latitudeDelta / 2), 2);
            final double harvesineOfLongitude = pow(sin(longitudeDelta / 2), 2);
            final double firstCosine = cos(firstLatitude);
            final double secondConsine = cos(secondLatitude);
            
            final double harvesine = harvesineOfLatitude +(firstCosine * secondConsine * harvesineOfLongitude);
            final double inverseHarvesine = asin(sqrt(harvesine));
            
            final double distance = 2 * inverseHarvesine * RADIUS_OF_EARTH_IN_KILOMETERS * 1_000;
            
            return distance;
        }

        @Override
        public Location calculateDestinationFrom(Location origin, double distanceInMeters, double bearing)
        {
            checkThat(origin)
                .is(notNull())
                .is(validLocation());
            
            checkThat(distanceInMeters)
                .usingMessage("distance must be > 0")
                .is(greaterThan(0.0));
            
            checkThat(bearing)
                .usingMessage("bearing is out of range of a great circle")
                .is(greaterThanOrEqualTo(-360.0))
                .is(lessThanOrEqualTo(360.0));
            
            //Formula:	φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
            //λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
            
            double lat1 = toRadians(origin.getLatitude());
            double lon1 = toRadians(origin.getLongitude());
            
            double angularDistance = distanceInMeters / RADIUS_OF_EARTH_IN_METERS;
            double trueCourse = toRadians(bearing);
            
            double lat2 = asin( (sin(lat1) * cos(angularDistance)) +
                                (cos(lat1) * sin(angularDistance) * cos(trueCourse)) );
            
            double lon2 = lon1 + atan2( sin(trueCourse) * sin(angularDistance) * cos(lat1),
                                       cos(angularDistance) - (sin(lat1) * sin(lat2)) );

            lat2 = toDegrees(lat2);
            lon2 = toDegrees(lon2);
            lon2 = ((lon2 + 540) % 360) - 180;
            
            return new Location(lat2, lon2);
        }

        @Override
        public double calculateBearingFromTo(Location start, Location end)
        {
            checkThat(start, end)
                .are(notNull())
                .are(validLocation());
            
            //Formula:	θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ )
            //where	φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
            //note that angles need to be in radians to pass to trig functions!
            
            double lat1 = start.getLatitude();
            double lon1 = start.getLongitude();
            double lat2 = end.getLatitude();
            double lon2 = end.getLongitude();
            
            //Convert to radians
            lat1 = toRadians(lat1);
            lon1 = toRadians(lon1);
            lat2 = toRadians(lat2);
            lon2 = toRadians(lon2);
            
            double longitudeDelta = lon2 - lon1;
            
            double y = sin(longitudeDelta) * cos(lat2);
            double x = cos(lat1) * sin(lat2) -
                       sin(lat1) * cos(lat2) * cos(longitudeDelta);
            
            double bearing = atan2(y, x);

            double bearingInDegrees = toDegrees(bearing);
            
            return (bearingInDegrees + 360) % 360;
        }
        
    }
}
