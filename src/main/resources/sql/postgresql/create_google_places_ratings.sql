-- Used to store information about Ratings for Google Places 
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Google_Places_Ratings
(
    rating_id TEXT gen_random_uuid()::TEXT,
    place_id TEXT,
    aspects TEXT,
    author_name TEXT,
    author_url TEXT,
    relative_time_description TEXT,
    body TEXT,
    time TIMESTAMP
);
