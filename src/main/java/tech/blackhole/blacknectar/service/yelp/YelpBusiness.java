
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

package tech.blackhole.blacknectar.service.yelp;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.concurrency.Mutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadUnsafe;
import tech.sirwellington.alchemy.annotations.objects.Pojo;

/**
 *
 * @author SirWellington
 */
@Pojo
@Mutable
@ThreadUnsafe
public class YelpBusiness
{

    public String id;
    public String name;
    public String url;
    public int rating;
    public String phone;
    public Boolean isClosed;
    public List<Category> categories;
    public int reviewCount;
    public Coordinate coordinates;
    @SerializedName("image_url")
    public String imageURL;
    @Optional
    public Double distance;

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.url);
        hash = 37 * hash + this.rating;
        hash = 37 * hash + Objects.hashCode(this.phone);
        hash = 37 * hash + Objects.hashCode(this.isClosed);
        hash = 37 * hash + Objects.hashCode(this.categories);
        hash = 37 * hash + this.reviewCount;
        hash = 37 * hash + Objects.hashCode(this.coordinates);
        hash = 37 * hash + Objects.hashCode(this.imageURL);
        hash = 37 * hash + Objects.hashCode(this.distance);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final YelpBusiness other = (YelpBusiness) obj;
        if (this.rating != other.rating)
        {
            return false;
        }
        if (this.reviewCount != other.reviewCount)
        {
            return false;
        }
        if (!Objects.equals(this.id, other.id))
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.url, other.url))
        {
            return false;
        }
        if (!Objects.equals(this.phone, other.phone))
        {
            return false;
        }
        if (!Objects.equals(this.imageURL, other.imageURL))
        {
            return false;
        }
        if (!Objects.equals(this.isClosed, other.isClosed))
        {
            return false;
        }
        if (!Objects.equals(this.categories, other.categories))
        {
            return false;
        }
        if (!Objects.equals(this.coordinates, other.coordinates))
        {
            return false;
        }
        if (!Objects.equals(this.distance, other.distance))
        {
            return false;
        }
        return true;
    }

    @Pojo
    @Mutable
    @ThreadUnsafe
    public static class Category
    {

        public String alias;
        public String title;

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 61 * hash + Objects.hashCode(this.alias);
            hash = 61 * hash + Objects.hashCode(this.title);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Category other = (Category) obj;
            if (!Objects.equals(this.alias, other.alias))
            {
                return false;
            }
            if (!Objects.equals(this.title, other.title))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Category{" + "alias=" + alias + ", title=" + title + '}';
        }

    }

    @Pojo
    @Mutable
    @ThreadUnsafe
    public static class Coordinate
    {

        double latitude;
        double longitude;

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 59 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
            hash = 59 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Coordinate other = (Coordinate) obj;
            if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude))
            {
                return false;
            }
            if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Coordinate{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
        }

    }

    @Pojo
    @Mutable
    @ThreadUnsafe
    public static class Address
    {

        public String city;
        public String state;
        public String country;
        public String address1;
        public String address2;
        public String address3;
        public String zipCode;

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.city);
            hash = 83 * hash + Objects.hashCode(this.state);
            hash = 83 * hash + Objects.hashCode(this.country);
            hash = 83 * hash + Objects.hashCode(this.address1);
            hash = 83 * hash + Objects.hashCode(this.address2);
            hash = 83 * hash + Objects.hashCode(this.address3);
            hash = 83 * hash + Objects.hashCode(this.zipCode);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Address other = (Address) obj;
            if (!Objects.equals(this.city, other.city))
            {
                return false;
            }
            if (!Objects.equals(this.state, other.state))
            {
                return false;
            }
            if (!Objects.equals(this.country, other.country))
            {
                return false;
            }
            if (!Objects.equals(this.address1, other.address1))
            {
                return false;
            }
            if (!Objects.equals(this.address2, other.address2))
            {
                return false;
            }
            if (!Objects.equals(this.address3, other.address3))
            {
                return false;
            }
            if (!Objects.equals(this.zipCode, other.zipCode))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Address{" + "city=" + city + ", state=" + state + ", country=" + country + ", address1=" + address1 + ", address2=" + address2 + ", address3=" + address3 + ", zipCode=" + zipCode + '}';
        }

    }

}
