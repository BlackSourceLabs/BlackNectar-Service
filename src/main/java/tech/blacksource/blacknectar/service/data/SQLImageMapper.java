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

package tech.blacksource.blacknectar.service.data;

import com.google.common.base.Objects;
import com.google.inject.ImplementedBy;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import tech.blacksource.blacknectar.service.images.Image;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@ImplementedBy(SQLImageMapper.Impl.class)
@FunctionalInterface
public interface SQLImageMapper extends RowMapper<Image>
{

    /**
     * Creates an {@link Image} object from the {@link ResultSet}.
     *
     * @param results
     * @param rowNum
     * @return
     * @throws SQLException
     */
    @Override
    public Image mapRow(ResultSet results, int rowNum) throws SQLException;

    static class Impl implements SQLImageMapper
    {

        @Override
        public Image mapRow(ResultSet results, int rowNum) throws SQLException
        {
            if (results == null)
            {
                return null;
            }

            String imageId = results.getString(SQLColumns.Images.IMAGE_ID);
            checkThat(imageId)
                .usingMessage("results missing imageId")
                .is(nonEmptyString());

            UUID storeId = results.getObject(SQLColumns.Images.STORE_ID, UUID.class);
            checkThat(storeId)
                .usingMessage("results missing storeId")
                .is(notNull());

            Image.Builder builder = Image.Builder.newInstance()
                .withStoreID(storeId)
                .withImageID(imageId);

            String contentType = results.getString(SQLColumns.Images.CONTENT_TYPE);
            if (!isNullOrEmpty(contentType))
            {
                builder = builder.withContentType(contentType);
            }

            String imageType = results.getString(SQLColumns.Images.IMAGE_TYPE);
            if (!isNullOrEmpty(imageType))
            {
                builder = builder.withImageType(imageType);
            }

            int width = results.getInt(SQLColumns.Images.WIDTH);
            int height = results.getInt(SQLColumns.Images.HEIGHT);
            if (width > 0 && height > 0)
            {
                builder = builder.withWidthAndHeight(width, height);
            }

            int sizeInBytes = results.getInt(SQLColumns.Images.SIZE_IN_BYTES);
            if (sizeInBytes > 0)
            {
                builder = builder.withSizeInBytes(sizeInBytes);
            }

            String source = results.getString(SQLColumns.Images.SOURCE);
            if (!isNullOrEmpty(source))
            {
                builder = builder.withSource(source);
            }

            String url = results.getString(SQLColumns.Images.URL);

            if (!isNullOrEmpty(url))
            {
                try
                {
                    builder = builder.withURL(url);
                }
                catch (IllegalArgumentException | MalformedURLException ex)
                {

                    throw new SQLDataException("could not convert to URL: " + url, ex);
                }
            }
            
            if (hasColumn(results, SQLColumns.Images.IMAGE_BINARY))
            {
                byte[] binary = results.getBytes(SQLColumns.Images.IMAGE_BINARY);

                if (binary != null)
                {
                    builder = builder.withImageData(binary);
                }
            }

            return builder.build();
        }

        private boolean hasColumn(ResultSet results, String expectedColumn) throws SQLException
        {
            ResultSetMetaData metadata = results.getMetaData();
            
            for (int i = 1; i <= metadata.getColumnCount(); ++i)
            {
                String columnName = metadata.getColumnLabel(i);
                
                if (Objects.equal(columnName, expectedColumn))
                {
                    return true;
                }
            }
            
            return false;
        }
        
        

    }

}
