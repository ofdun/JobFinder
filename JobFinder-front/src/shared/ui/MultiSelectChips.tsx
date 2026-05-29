interface MultiSelectOption {
  id: number;
  name: string;
}

interface MultiSelectChipsProps {
  label: string;
  options: MultiSelectOption[];
  selectedIds: number[];
  onChange: (next: number[]) => void;
  placeholder?: string;
}

export function MultiSelectChips({
  label,
  options,
  selectedIds,
  onChange,
  placeholder = 'Выберите значение',
}: MultiSelectChipsProps) {
  const selectedOptions = options.filter((option) => selectedIds.includes(option.id));
  const availableOptions = options.filter((option) => !selectedIds.includes(option.id));

  return (
    <label>
      {label}
      <div className="multi-select-chips">
        <select
          value=""
          onChange={(event) => {
            const nextId = Number(event.target.value);
            if (!Number.isInteger(nextId) || nextId <= 0 || selectedIds.includes(nextId)) {
              return;
            }

            onChange([...selectedIds, nextId]);
          }}
          disabled={availableOptions.length === 0}
        >
          <option value="">{availableOptions.length === 0 ? 'Все добавлено' : placeholder}</option>
          {availableOptions.map((option) => (
            <option key={option.id} value={option.id}>
              {option.name}
            </option>
          ))}
        </select>

        <div className="chip-list" aria-live="polite">
          {selectedOptions.map((option) => (
            <span key={option.id} className="chip">
              {option.name}
              <button
                type="button"
                className="chip-remove"
                aria-label={`Удалить ${option.name}`}
                onClick={() => onChange(selectedIds.filter((id) => id !== option.id))}
              >
                x
              </button>
            </span>
          ))}
        </div>
      </div>
    </label>
  );
}

