-- Used to store information about Photos retrieved from the
-- Google Places API.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Google_Places_Photos
(
    photo_reference TEXT,
    place_id TEXT,
    height NUMERIC,
    width NUMERIC,
    html_attributions TEXT[],
    url TEXT,

    PRIMARY KEY (photo_reference)
);
