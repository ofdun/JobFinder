CREATE INDEX IF NOT EXISTS idx_locations_city_lower ON jobfinder.locations ((lower(city)));
CREATE INDEX IF NOT EXISTS idx_locations_country_lower ON jobfinder.locations ((lower(country)));

