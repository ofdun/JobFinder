import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { getCategories } from '../../shared/api/categoryApi';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { createResume } from '../../shared/api/resumeApi';
import { formatLanguageProficiency } from '../../shared/lib/format';
import { getCurrentUserId } from '../../shared/session/sessionStore';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';
import { Education, JobExperience, EducationDegree } from '../../shared/types/resume';

export function CreateResumePage() {
    const navigate = useNavigate();
    const userId = getCurrentUserId();

    const [categoryId, setCategoryId] = useState(1);
    const [description, setDescription] = useState('');
    const [skillIds, setSkillIds] = useState<number[]>([]);
    const [languageIds, setLanguageIds] = useState<number[]>([]);

    const [educations, setEducations] = useState<Omit<Education, 'id' | 'resumeId'>[]>([]);
    const [jobExperiences, setJobExperiences] = useState<Omit<JobExperience, 'id' | 'resumeId'>[]>([]);

    const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: getCategories });
    const skillsQuery = useQuery({ queryKey: ['skills'], queryFn: getSkills });
    const languagesQuery = useQuery({ queryKey: ['languages'], queryFn: getLanguages });

    useEffect(() => {
        if (categoriesQuery.data?.length) setCategoryId(categoriesQuery.data[0].id);
    }, [categoriesQuery.data]);

    const mutation = useMutation({
        mutationFn: () => createResume({
            applicantId: userId as number,
            categoryId,
            description,
            skillIds,
            languageIds,
            educations,
            jobExperiences,
        }),
        onSuccess: () => navigate('/me/resumes'),
    });

    const addEducation = () => {
        setEducations([...educations, {
            educationDegree: 'BACHELOR',
            institutionName: '',
            faculty: '',
            department: '',
            yearOfGraduation: new Date().getFullYear()
        }]);
    };

    const addExperience = () => {
        setJobExperiences([...jobExperiences, {
            position: '',
            companyName: '', // Важно!
            description: '',
            startDate: new Date().toISOString().split('T')[0]
        }]);
    };

    if (userId === null) return <p className="page error">Авторизуйтесь.</p>;

    return (
        <main className="page">
            <h1>Создание резюме</h1>
            <form className="card" onSubmit={(e: FormEvent) => { e.preventDefault(); mutation.mutate(); }}>
                <label>Категория
                    <select value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))} required>
                        {categoriesQuery.data?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                </label>

                <label>О себе<textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} required /></label>

                <MultiSelectChips label="Навыки" options={skillsQuery.data ?? []} selectedIds={skillIds} onChange={setSkillIds} />

                <hr />
                <h3>Образование</h3>
                {educations.map((edu, idx) => (
                    <div key={idx} className="card" style={{ marginBottom: '12px', background: '#f8fafc' }}>
                        <div className="toolbar">
                            <label>Степень
                                <select value={edu.educationDegree} onChange={(e) => {
                                    const next = [...educations];
                                    next[idx].educationDegree = e.target.value as EducationDegree;
                                    setEducations(next);
                                }}>
                                    <option value="BACHELOR">Бакалавр</option>
                                    <option value="MASTER">Магистр</option>
                                    <option value="SPECIALIST">Специалист</option>
                                    <option value="PHD">PhD</option>
                                </select>
                            </label>
                            <label>Год окончания
                                <input type="number" value={edu.yearOfGraduation} onChange={(e) => {
                                    const next = [...educations];
                                    next[idx].yearOfGraduation = parseInt(e.target.value) || 2024;
                                    setEducations(next);
                                }} required />
                            </label>
                        </div>
                        <label>Учебное заведение
                            <input value={edu.institutionName} onChange={(e) => {
                                const next = [...educations];
                                next[idx].institutionName = e.target.value;
                                setEducations(next);
                            }} required /></label>
                        <div className="toolbar">
                            <label>Факультет
                                <input value={edu.faculty} onChange={(e) => {
                                    const next = [...educations];
                                    next[idx].faculty = e.target.value;
                                    setEducations(next);
                                }} required /></label>
                            <label>Кафедра
                                <input value={edu.department} onChange={(e) => {
                                    const next = [...educations];
                                    next[idx].department = e.target.value;
                                    setEducations(next);
                                }} required /></label>
                        </div>
                        <button type="button" className="danger" onClick={() => setEducations(educations.filter((_, i) => i !== idx))}>Удалить</button>
                    </div>
                ))}
                <button type="button" onClick={addEducation}>+ Добавить образование</button>

                <hr />
                <h3>Опыт работы</h3>
                {jobExperiences.map((exp, idx) => (
                    <div key={idx} className="card" style={{ marginBottom: '12px', background: '#f8fafc' }}>
                        <div className="toolbar">
                            <label>Должность
                                <input value={exp.position} onChange={(e) => {
                                    const next = [...jobExperiences];
                                    next[idx].position = e.target.value;
                                    setJobExperiences(next);
                                }} required /></label>
                            <label>Компания
                                <input value={exp.companyName} onChange={(e) => {
                                    const next = [...jobExperiences];
                                    next[idx].companyName = e.target.value;
                                    setJobExperiences(next);
                                }} required /></label>
                        </div>
                        <div className="toolbar">
                            <label>Начало
                                <input type="date" value={exp.startDate} onChange={(e) => {
                                    const next = [...jobExperiences];
                                    next[idx].startDate = e.target.value;
                                    setJobExperiences(next);
                                }} required /></label>
                            <label>Конец
                                <input type="date" value={exp.endDate || ''} onChange={(e) => {
                                    const next = [...jobExperiences];
                                    next[idx].endDate = e.target.value || undefined;
                                    setJobExperiences(next);
                                }} /></label>
                        </div>
                        <label>Описание задач
                            <textarea value={exp.description} onChange={(e) => {
                                const next = [...jobExperiences];
                                next[idx].description = e.target.value;
                                setJobExperiences(next);
                            }} rows={2} required /></label>
                        <button type="button" className="danger" onClick={() => setJobExperiences(jobExperiences.filter((_, i) => i !== idx))}>Удалить</button>
                    </div>
                ))}
                <button type="button" onClick={addExperience}>+ Добавить опыт</button>

                {mutation.isError && <p className="error">Ошибка: {(mutation.error as any).message}</p>}
                <div className="toolbar" style={{ marginTop: '20px' }}>
                    <button type="submit" disabled={mutation.isPending}>Создать резюме</button>
                    <Link to="/me/resumes">Отмена</Link>
                </div>
            </form>
        </main>
    );
}