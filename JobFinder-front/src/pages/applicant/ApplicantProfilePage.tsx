import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { deleteApplicantById, getApplicantById, updateApplicantById } from '../../shared/api/applicantApi';
import { clearSession, getCurrentUserId } from '../../shared/session/sessionStore';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';

export function ApplicantProfilePage() {
  const userId = getCurrentUserId();
  const navigate = useNavigate();

  const query = useQuery({
    queryKey: ['applicant-profile', userId],
    queryFn: () => getApplicantById(userId as number),
    enabled: userId !== null,
  });

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [address, setAddress] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [locationId, setLocationId] = useState<number | undefined>(1);

  useEffect(() => {
    if (!query.data) {
      return;
    }

    setName(query.data.name ?? '');
    setEmail(query.data.email ?? '');
    setAddress(query.data.address ?? '');
    setPhoneNumber(query.data.phoneNumber ?? '');
    setLocationId(query.data.location?.id);
  }, [query.data]);

  const updateMutation = useMutation({
    mutationFn: () =>
      updateApplicantById(userId as number, {
        name,
        email,
        password: password || undefined,
        address,
        phoneNumber,
        locationId: locationId as number,
      }),
    onSuccess: () => {
      setPassword('');
      query.refetch();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteApplicantById(userId as number),
    onSuccess: () => {
      clearSession();
      navigate('/');
    },
  });

  if (userId === null) {
    return <p className="page error">Не удалось определить пользователя. Перелогиньтесь.</p>;
  }

  return (
    <main className="page">
      <h1>Профиль соискателя</h1>
      {query.isLoading ? <p>Загрузка профиля...</p> : null}
      {query.isError ? <p className="error">Не удалось загрузить профиль.</p> : null}

      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
      >
        <label>Имя<input value={name} onChange={(event) => setName(event.target.value)} required /></label>
        <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></label>
        <label>Новый пароль<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>Адрес<input value={address} onChange={(event) => setAddress(event.target.value)} /></label>
        <label>Телефон<input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} /></label>
        <LocationTypeahead label="Локация" value={locationId} onChange={setLocationId} />
        <button type="submit" disabled={updateMutation.isPending || !locationId}>{updateMutation.isPending ? 'Сохранение...' : 'Сохранить профиль'}</button>
      </form>

      <button type="button" className="danger profile-delete-action" onClick={() => deleteMutation.mutate()} disabled={deleteMutation.isPending}>
        {deleteMutation.isPending ? 'Удаление...' : 'Удалить профиль'}
      </button>
    </main>
  );
}

