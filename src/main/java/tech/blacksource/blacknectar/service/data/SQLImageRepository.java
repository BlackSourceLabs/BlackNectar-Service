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

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.images.ImageRepository;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class SQLImageRepository implements ImageRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(SQLImageRepository.class);
    
    private final Aroma aroma;
    private final JdbcTemplate database;
    private final SQLImageMapper imageMapper;
    
    @Inject
    SQLImageRepository(Aroma aroma, JdbcTemplate database, SQLImageMapper imageMapper)
    {
        checkThat(aroma, database, imageMapper)
            .are(notNull());
        
        this.aroma = aroma;
        this.database = database;
        this.imageMapper = imageMapper;
    }
    
    @Override
    public void addImage(Image image) throws BlackNectarAPIException
    {
        checkNotNull(image);

        try
        {
            _addImage(image);
        }
        catch (Exception ex)
        {
            makeNoteThatFailedToSaveImage(image, ex);
            throw new OperationFailedException(ex);
        }
    }

    @Override
    public Image getImage(UUID storeId, String imageId) throws DoesNotExistException, BlackNectarAPIException
    {
        checkNotNull(storeId);
        checkNotEmpty(imageId);

        try
        {
            return _getImage(storeId, imageId);
        }
        catch(EmptyResultDataAccessException ex)
        {
            throw new DoesNotExistException(ex);
        }
        catch (Exception ex)
        {
            makeNoteThatFailedToGetImage(storeId, imageId, ex);
            throw new OperationFailedException(ex);
        }
    }

    @Override
    public List<Image> getImagesForStore(UUID storeId) throws BlackNectarAPIException
    {
        checkNotNull(storeId);

        try
        {
            return _getImagesForStore(storeId);
        }
        catch (Exception ex)
        {
            makeNoteThatFailedToGetImagesForStore(storeId, ex);
            throw new OperationFailedException(ex);
        }
    }


    @Override
    public boolean hasImages(UUID storeId) throws BlackNectarAPIException
    {
        checkNotNull(storeId);

        try
        {
            return _hasImage(storeId);
        }
        catch (Exception ex)
        {
            makeNoteThatFailedToCheckIfStoreHasImages(storeId, ex);
            throw new OperationFailedException(ex);
        }

    }

    @Override
    public void deleteImage(UUID storeId, String imageId) throws BlackNectarAPIException
    {
        checkNotNull(imageId);
        checkNotEmpty(imageId);

        try
        {
            _deleteImage(storeId, imageId);
        }
        catch (Exception ex)
        {
            makeNoteThatFailedToDeleteImage(storeId, imageId, ex);
            throw new OperationFailedException(ex);
        }
    }

    private void makeNoteThatFailedToSaveImage(Image image, Exception ex)
    {
        String message = "Failed to save Image: {}";
        LOG.error(message, image, ex);
        
        aroma.begin().titled("SQL Image Save Failed")
            .withBody(message, image, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void _addImage(Image image)
    {
        String insertStatement = SQLQueries.INSERT_STORE_IMAGE;

        database.update(insertStatement,
                        image.getStoreId(),
                        image.getImageId(),
                        image.getHeight(),
                        image.getWidth(),
                        image.getSizeInBytes(),
                        image.getContentType(),
                        image.getImageType(),
                        image.getSource(),
                        image.getUrl().toString());
    }

    private Image _getImage(UUID storeId, String imageId)
    {
        String query = SQLQueries.QUERY_IMAGE;

        Image result = database.queryForObject(query, imageMapper, storeId, imageId);
        checkHaveResult(result);

        return result;
    }

    private List<Image> _getImagesForStore(UUID storeId)
    {
        String query = SQLQueries.QUERY_IMAGES_FOR_STORE;

        List<Image> results = database.query(query, imageMapper, storeId);

        return Lists.nullToEmpty(results);
    }

    private boolean _hasImage(UUID storeId)
    {
        String query = SQLQueries.COUNT_IMAGES_FOR_STORE;

        Integer count = database.queryForObject(query, Integer.class, storeId);
        return count != null && count > 0;
    }

    private void _deleteImage(UUID storeId, String imageId)
    {
        String query = SQLQueries.DELETE_IMAGE;

        int result = database.update(query, storeId, imageId);

        if (result == 0)
        {
            LOG.info("Did not really delete image: [{}]/[{}]", storeId, imageId);
        }

    }

    private void makeNoteThatFailedToGetImage(UUID storeId, String imageId, Exception ex)
    {
        String message = "Failed to get image: StoreID[{}] | ImageID[{}]";
        LOG.error(message, storeId, imageId, ex);
        
        aroma.begin().titled("SQL Image Get Failed")
            .withBody(message, storeId, imageId, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void makeNoteThatFailedToGetImagesForStore(UUID storeId, Exception ex)
    {
        String message = "Failed to get images for Store: [{}]";
        LOG.error(message, storeId, ex);
        aroma.begin().titled("SQL Image Get Failed")
            .withBody(message, storeId, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void makeNoteThatFailedToCheckIfStoreHasImages(UUID storeId, Exception ex)
    {
        String message = "Failed to check if store has images: [{}]";
        LOG.error(message, storeId, ex);
       
        aroma.begin().titled("SQL Store Image Check Failed")
            .withBody(message, storeId, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void makeNoteThatFailedToDeleteImage(UUID storeId, String imageId, Exception ex)
    {
        String message = "Failed to delete Image. Store: [{}] | Image ID: [{}]";
        LOG.error(message, storeId, imageId, ex);
       
        aroma.begin().titled("SQL Image Delete Failed")
            .withBody(message, storeId, imageId, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void checkNotNull(Object object)
    {
        checkThat(object)
            .throwing(BadArgumentException.class)
            .is(notNull());
    }

    private void checkNotEmpty(String imageId)
    {
        checkThat(imageId)
            .throwing(BadArgumentException.class)
            .is(nonEmptyString());
    }

    private void checkHaveResult(Image result)
    {
        checkThat(result)
            .throwing(DoesNotExistException.class)
            .is(notNull());
    }

}
