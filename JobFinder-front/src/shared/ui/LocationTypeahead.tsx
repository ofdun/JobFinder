import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getLocationById, searchLocations } from '../api/locationApi';
import { useDebouncedValue } from '../lib/useDebouncedValue';

interface LocationTypeaheadProps {
  label: string;
  value?: number;
  onChange: (next?: number) => void;
  placeholder?: string;
}

export function LocationTypeahead({
  label,
  value,
  onChange,
  placeholder = 'Начните вводить город или страну',
}: LocationTypeaheadProps) {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebouncedValue(query, 300);

  const selectedLocationQuery = useQuery({
    queryKey: ['location', value],
    queryFn: () => getLocationById(value as number),
    enabled: Number.isInteger(value) && (value as number) > 0,
    staleTime: 1000 * 60 * 60,
  });

  const suggestionsQuery = useQuery({
    queryKey: ['locations-search', debouncedQuery],
    queryFn: () => searchLocations(debouncedQuery, 20),
    enabled: debouncedQuery.trim().length >= 2,
    staleTime: 1000 * 60 * 5,
  });

  const selectedLabel = useMemo(() => {
    if (!selectedLocationQuery.data) {
      return null;
    }

    return `${selectedLocationQuery.data.city}, ${selectedLocationQuery.data.country}`;
  }, [selectedLocationQuery.data]);

  return (
    <label>
      {label}
      <div className="location-typeahead">
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={placeholder}
        />

        {selectedLabel ? (
          <div className="selected-location" role="status" aria-live="polite">
            <span className="selected-location-label">Выбрана локация</span>
            <span className="selected-location-value">{selectedLabel}</span>
            <button
              type="button"
              className="selected-location-clear"
              aria-label="Очистить выбранную локацию"
              onClick={() => onChange(undefined)}
            >
              Сбросить
            </button>
          </div>
        ) : null}

        {suggestionsQuery.isLoading ? <p>Ищем локации...</p> : null}
        {suggestionsQuery.isError ? <p className="error">Не удалось загрузить локации.</p> : null}

        {suggestionsQuery.data && suggestionsQuery.data.length > 0 ? (
          <div className="location-suggestions" role="listbox" aria-label="Подсказки локаций">
            {suggestionsQuery.data.map((location) => (
              <button
                key={location.id}
                type="button"
                className="location-suggestion"
                onClick={() => {
                  onChange(location.id);
                  setQuery('');
                }}
              >
                {location.city}, {location.country}
              </button>
            ))}
          </div>
        ) : null}
      </div>
    </label>
  );
}

