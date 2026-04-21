import { FormEvent, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { registerApplicant } from '../../shared/api/authApi';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';

export function RegisterApplicantForm() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [address, setAddress] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [locationId, setLocationId] = useState<number | undefined>();
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: () =>
      registerApplicant({
        name,
        email,
        password,
        address,
        phoneNumber,
        locationId: locationId as number,
      }),
    onSuccess: () => navigate('/login'),
  });

  const onSubmit = (event: FormEvent) => {
    event.preventDefault();
    mutation.mutate();
  };

  return (
    <form className="card" onSubmit={onSubmit}>
      <h2>Регистрация соискателя</h2>
      <label>Имя<input value={name} onChange={(event) => setName(event.target.value)} required /></label>
      <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></label>
      <label>Пароль<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required /></label>
      <label>Адрес<input value={address} onChange={(event) => setAddress(event.target.value)} /></label>
      <label>Телефон<input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} /></label>
      <LocationTypeahead
        label="Локация"
        value={locationId}
        onChange={setLocationId}
        placeholder="Введите город или страну"
      />
      {mutation.isError ? <p className="error">Ошибка регистрации.</p> : null}
      <button type="submit" disabled={mutation.isPending || !locationId}>{mutation.isPending ? 'Отправка...' : 'Зарегистрироваться'}</button>
    </form>
  );
}

