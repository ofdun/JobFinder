import { InputHTMLAttributes, useId, useState } from 'react';
import { Icon } from './Icon';

type PasswordInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  label: string;
};

export function PasswordInput({ label, id, ...props }: PasswordInputProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const [visible, setVisible] = useState(false);
  const toggleLabel = visible ? 'Скрыть пароль' : 'Показать пароль';

  return (
    <div className="password-field">
      <label htmlFor={inputId}>{label}</label>
      <div className="password-control">
        <input {...props} id={inputId} type={visible ? 'text' : 'password'} />
        <button
          className="password-toggle"
          type="button"
          aria-label={toggleLabel}
          aria-controls={inputId}
          aria-pressed={visible}
          title={toggleLabel}
          disabled={props.disabled}
          onClick={() => setVisible(value => !value)}
        >
          <Icon name={visible ? 'eyeOff' : 'eye'} />
        </button>
      </div>
    </div>
  );
}
