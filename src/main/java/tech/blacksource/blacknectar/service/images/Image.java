/*
 * Copyright 2017 BlackSourceLabs.
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

package tech.blacksource.blacknectar.service.images;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 * Represents an Image that is associated with a Store.
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@BuilderPattern(role = PRODUCT)
public final class Image
{

    private final UUID storeId;
    private final String imageId;
    private final byte[] imageData;
    private final int height;
    private final int width;
    private final int sizeInBytes;
    private final String contentType;
    private final String imageType;
    private final String source;
    private final URL url;

    Image(UUID storeId,
          String imageId,
          byte[] imageData,
          int height,
          int width,
          int sizeInBytes,
          String contentType,
          String imageType,
          String source,
          URL url)
    {
        checkThat(storeId).is(notNull());
        checkThat(imageId).is(nonEmptyString());
        
        this.storeId = storeId;
        this.imageId = imageId;
        this.imageData = imageData;
        this.height = height;
        this.width = width;
        this.sizeInBytes = sizeInBytes;
        this.contentType = contentType;
        this.imageType = imageType;
        this.source = source;
        this.url = url;
    }

    public boolean hasImageData()
    {
        return imageData != null && imageData.length > 0;
    }

    public boolean hasContentType()
    {
        return !isNullOrEmpty(contentType);
    }

    public boolean hasImageType()
    {
        return !isNullOrEmpty(imageType);
    }

    public boolean hasSource()
    {
        return !isNullOrEmpty(source);
    }

    public boolean hasURL()
    {
        return url != null;
    }

    public UUID getStoreId()
    {
        return storeId;
    }

    public String getImageId()
    {
        return imageId;
    }

    public byte[] getImageData()
    {
        return imageData;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getSizeInBytes()
    {
        return sizeInBytes;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getImageType()
    {
        return imageType;
    }

    public String getSource()
    {
        return source;
    }

    public URL getUrl()
    {
        return url;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.storeId);
        hash = 29 * hash + Objects.hashCode(this.imageId);
        hash = 29 * hash + Arrays.hashCode(this.imageData);
        hash = 29 * hash + this.height;
        hash = 29 * hash + this.width;
        hash = 29 * hash + this.sizeInBytes;
        hash = 29 * hash + Objects.hashCode(this.contentType);
        hash = 29 * hash + Objects.hashCode(this.imageType);
        hash = 29 * hash + Objects.hashCode(this.source);
        hash = 29 * hash + Objects.hashCode(this.url);
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
        final Image other = (Image) obj;
        if (this.height != other.height)
        {
            return false;
        }
        if (this.width != other.width)
        {
            return false;
        }
        if (this.sizeInBytes != other.sizeInBytes)
        {
            return false;
        }
        if (!Objects.equals(this.imageId, other.imageId))
        {
            return false;
        }
        if (!Objects.equals(this.contentType, other.contentType))
        {
            return false;
        }
        if (!Objects.equals(this.imageType, other.imageType))
        {
            return false;
        }
        if (!Objects.equals(this.source, other.source))
        {
            return false;
        }
        if (!Objects.equals(this.storeId, other.storeId))
        {
            return false;
        }
        if (!Arrays.equals(this.imageData, other.imageData))
        {
            return false;
        }
        if (!Objects.equals(this.url, other.url))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Image{" + "storeId=" + storeId + ", imageId=" + imageId + ", imageData=" + imageData + ", height=" + height + ", width=" + width + ", sizeInBytes=" + sizeInBytes + ", contentType=" + contentType + ", imageType=" + imageType + ", source=" + source + ", url=" + url + '}';
    }

    @BuilderPattern(role = BUILDER)
    public static final class Builder
    {

        private UUID storeId;
        private String imageId;
        private byte[] imageData;
        private int height;
        private int width;
        private int sizeInBytes;
        private String contentType;
        private String imageType;
        private String source;
        private URL url;

        public Builder()
        {
        }

        public static Builder newInstance()
        {
            return new Builder();
        }

        public static Builder fromImage(@Required Image image)
        {
            checkThat(image).is(notNull());

            Builder builder = newInstance();
            builder.storeId = image.storeId;
            builder.imageId = image.imageId;
            builder.imageData = image.imageData;
            builder.height = image.height;
            builder.width = image.width;
            builder.sizeInBytes = image.sizeInBytes;
            builder.contentType = image.contentType;
            builder.imageType = image.imageType;
            builder.source = image.source;
            builder.url = image.url;

            return builder;
        }

        public Builder withStoreID(@NonEmpty String storeId) throws IllegalArgumentException
        {
            checkThat(storeId)
                .is(nonEmptyString())
                .is(validUUID());

            this.storeId = UUID.fromString(storeId);
            return this;
        }

        public Builder withStoreID(@Required UUID storeId) throws IllegalArgumentException
        {
            checkThat(storeId)
                .is(notNull());

            this.storeId = storeId;
            return this;
        }

        public Builder withImageID(@NonEmpty String imageId) throws IllegalArgumentException
        {
            checkThat(imageId)
                .is(nonEmptyString());

            this.imageId = imageId;
            return this;
        }

        public Builder withImageData(@Required byte[] binary) throws IllegalArgumentException
        {
            checkThat(binary)
                .is(notNull());

            checkThat(binary.length)
                .usingMessage("Image Data cannot be empty")
                .is(greaterThan(0));

            this.imageData = binary;
            return this;
        }

        public Builder withWidthAndHeight(int width, int height) throws IllegalArgumentException
        {
            checkThat(width, height)
                .are(positiveInteger());

            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withSizeInBytes(int size) throws IllegalArgumentException
        {
            checkThat(size)
                .is(positiveInteger());

            this.sizeInBytes = size;
            return this;
        }

        public Builder withContentType(@NonEmpty String contentType) throws IllegalArgumentException
        {
            checkThat(contentType).is(nonEmptyString());

            this.contentType = contentType;
            return this;
        }

        public Builder withImageType(@NonEmpty String imageType) throws IllegalArgumentException
        {
            checkThat(imageType).is(nonEmptyString());

            this.imageType = imageType;
            return this;
        }

        public Builder withSource(@NonEmpty String source) throws IllegalArgumentException
        {
            checkThat(source).is(nonEmptyString());

            this.source = source;
            return this;
        }

        public Builder withURL(@NonEmpty String url) throws IllegalArgumentException, MalformedURLException
        {
            checkThat(url)
                .is(nonEmptyString())
                .is(validURL());

            this.url = new URL(url);
            return this;
        }

        public Builder withURL(@Required URL url) throws IllegalArgumentException
        {
            checkThat(url).is(notNull());

            this.url = url;
            return this;
        }
        
        public Builder withoutURL()
        {
            this.url = null;
            return this;
        }
        
        public Builder withoutImageData()
        {
            this.imageData = null;
            return this;
        }
        
        public Image build() throws IllegalStateException
        {
            checkThat(storeId)
                .usingMessage("storeId is missing")
                .is(notNull());

            checkThat(imageId)
                .usingMessage("imageId is required")
                .is(nonEmptyString());

            return new Image(storeId, imageId, imageData, height, width, sizeInBytes, contentType, imageType, source, url);
        }

        @Override
        public String toString()
        {
            return "Builder{" + "storeId=" + storeId + ", imageId=" + imageId + ", imageData=" + imageData + ", height=" + height + ", width=" + width + ", sizeInBytes=" + sizeInBytes + ", contentType=" + contentType + ", imageType=" + imageType + ", source=" + source + ", url=" + url + '}';
        }

    }

}
