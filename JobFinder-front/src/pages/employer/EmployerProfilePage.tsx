import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { deleteEmployerById, getEmployerById, updateEmployerById } from '../../shared/api/employerApi';
import { clearSession, getCurrentUserId } from '../../shared/session/sessionStore';
import { LocationTypeahead } from '../../shared/ui/LocationTypeahead';

export function EmployerProfilePage() {
  const userId = getCurrentUserId();
  const navigate = useNavigate();

  const query = useQuery({
    queryKey: ['employer-profile', userId],
    queryFn: () => getEmployerById(userId as number),
    enabled: userId !== null,
  });

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');
  const [websiteLink, setWebsiteLink] = useState('');
  const [locationId, setLocationId] = useState<number | undefined>(1);

  useEffect(() => {
    if (!query.data) {
      return;
    }

    setName(query.data.name ?? '');
    setEmail(query.data.email ?? '');
    setDescription(query.data.description ?? '');
    setAddress(query.data.address ?? '');
    setWebsiteLink(query.data.websiteLink ?? '');
    setLocationId(query.data.location?.id);
  }, [query.data]);

  const updateMutation = useMutation({
    mutationFn: () =>
      updateEmployerById(userId as number, {
        name,
        email,
        password: password || undefined,
        description,
        address,
        websiteLink,
        locationId: locationId as number,
      }),
    onSuccess: () => {
      setPassword('');
      query.refetch();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteEmployerById(userId as number),
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
      <h1>Профиль работодателя</h1>
      {query.isLoading ? <p>Загрузка профиля...</p> : null}
      {query.isError ? <p className="error">Не удалось загрузить профиль.</p> : null}

      <form
        className="card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
      >
        <label>Название компании<input value={name} onChange={(event) => setName(event.target.value)} required /></label>
        <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></label>
        <label>Новый пароль<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>Описание<textarea rows={4} value={description} onChange={(event) => setDescription(event.target.value)} /></label>
        <label>Адрес<input value={address} onChange={(event) => setAddress(event.target.value)} /></label>
        <label>Сайт<input type="url" value={websiteLink} onChange={(event) => setWebsiteLink(event.target.value)} /></label>
        <LocationTypeahead label="Локация" value={locationId} onChange={setLocationId} />
        <button type="submit" disabled={updateMutation.isPending || !locationId}>{updateMutation.isPending ? 'Сохранение...' : 'Сохранить профиль'}</button>
      </form>

      <button type="button" className="danger profile-delete-action" onClick={() => deleteMutation.mutate()} disabled={deleteMutation.isPending}>
        {deleteMutation.isPending ? 'Удаление...' : 'Удалить профиль'}
      </button>
    </main>
  );
}

