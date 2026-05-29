import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getCategories } from '../../shared/api/categoryApi';
import { getLanguages } from '../../shared/api/languageApi';
import { getSkills } from '../../shared/api/skillApi';
import { deleteResumeById, getResumeById, updateResumeById } from '../../shared/api/resumeApi';
import { MultiSelectChips } from '../../shared/ui/MultiSelectChips';
import { Education, JobExperience, EducationDegree } from '../../shared/types/resume';

export function EditResumePage() {
    const { id } = useParams();
    const resumeId = Number(id);
    const navigate = useNavigate();

    const [categoryId, setCategoryId] = useState(1);
    const [description, setDescription] = useState('');
    const [skillIds, setSkillIds] = useState<number[]>([]);
    const [languageIds, setLanguageIds] = useState<number[]>([]);
    const [educations, setEducations] = useState<Omit<Education, 'id' | 'resumeId'>[]>([]);
    const [jobExperiences, setJobExperiences] = useState<Omit<JobExperience, 'id' | 'resumeId'>[]>([]);

    const query = useQuery({
        queryKey: ['resume', resumeId],
        queryFn: () => getResumeById(resumeId),
        enabled: !!resumeId,
    });

    const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: getCategories });
    const skillsQuery = useQuery({ queryKey: ['skills'], queryFn: getSkills });

    useEffect(() => {
        if (!query.data) return;
        setCategoryId(query.data.category?.id ?? 1);
        setDescription(query.data.description ?? '');
        setSkillIds(query.data.skills?.map(s => s.id) ?? []);

        setEducations(query.data.educations?.map(e => ({
            educationDegree: e.educationDegree,
            institutionName: e.institutionName,
            faculty: e.faculty,
            department: e.department,
            yearOfGraduation: e.yearOfGraduation
        })) ?? []);

        setJobExperiences(query.data.jobExperiences?.map(e => ({
            position: e.position,
            companyName: e.companyName, // Было пропущено в UI
            description: e.description,
            startDate: e.startDate,
            endDate: e.endDate
        })) ?? []);
    }, [query.data]);

    const updateMutation = useMutation({
        mutationFn: () => updateResumeById(resumeId, {
            applicantId: query.data?.applicantId,
            categoryId, description, skillIds, languageIds, educations, jobExperiences
        }),
        onSuccess: () => query.refetch(),
    });

    const deleteMutation = useMutation({
        mutationFn: () => deleteResumeById(resumeId),
        onSuccess: () => navigate('/me/resumes'),
    });

    return (
        <main className="page">
            <h1>Редактирование резюме #{resumeId}</h1>
            <form className="card" onSubmit={(e: FormEvent) => { e.preventDefault(); updateMutation.mutate(); }}>
                <label>Категория
                    <select value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))}>
                        {categoriesQuery.data?.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                </label>

                <label>Описание<textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} required /></label>
                <MultiSelectChips label="Навыки" options={skillsQuery.data ?? []} selectedIds={skillIds} onChange={setSkillIds} />

                <hr />
                <h3>Образование</h3>
                {educations.map((edu, idx) => (
                    <div key={idx} className="card" style={{ marginBottom: '10px' }}>
                        <div className="toolbar">
                            <label>Степень
                                <select value={edu.educationDegree} onChange={(e) => {
                                    const next = [...educations]; next[idx].educationDegree = e.target.value as EducationDegree; setEducations(next);
                                }}>
                                    <option value="BACHELOR">Бакалавр</option>
                                    <option value="MASTER">Магистр</option>
                                    <option value="PHD">PhD</option>
                                </select>
                            </label>
                            <label>Год
                                <input type="number" value={edu.yearOfGraduation} onChange={(e) => {
                                    const next = [...educations]; next[idx].yearOfGraduation = parseInt(e.target.value); setEducations(next);
                                }} />
                            </label>
                        </div>
                        <label>Учебное заведение<input value={edu.institutionName} onChange={(e) => {
                            const next = [...educations]; next[idx].institutionName = e.target.value; setEducations(next);
                        }} required /></label>
                        <div className="toolbar">
                            <label>Факультет<input value={edu.faculty} onChange={(e) => {
                                const next = [...educations]; next[idx].faculty = e.target.value; setEducations(next);
                            }} required /></label>
                            <label>Кафедра<input value={edu.department} onChange={(e) => {
                                const next = [...educations]; next[idx].department = e.target.value; setEducations(next);
                            }} required /></label>
                        </div>
                        <button type="button" className="danger" onClick={() => setEducations(educations.filter((_, i) => i !== idx))}>Удалить</button>
                    </div>
                ))}
                <button type="button" onClick={() => setEducations([...educations, { educationDegree: 'BACHELOR', institutionName: '', faculty: '', department: '', yearOfGraduation: 2024 }])}>+ Добавить обучение</button>

                <hr />
                <h3>Опыт работы</h3>
                {jobExperiences.map((exp, idx) => (
                    <div key={idx} className="card" style={{ marginBottom: '10px' }}>
                        <div className="toolbar">
                            <label>Должность<input value={exp.position} onChange={(e) => {
                                const next = [...jobExperiences]; next[idx].position = e.target.value; setJobExperiences(next);
                            }} required /></label>
                            <label>Компания<input value={exp.companyName} onChange={(e) => {
                                const next = [...jobExperiences]; next[idx].companyName = e.target.value; setJobExperiences(next);
                            }} required /></label>
                        </div>
                        <div className="toolbar">
                            <label>Начало<input type="date" value={exp.startDate} onChange={(e) => {
                                const next = [...jobExperiences]; next[idx].startDate = e.target.value; setJobExperiences(next);
                            }} required /></label>
                            <label>Конец<input type="date" value={exp.endDate || ''} onChange={(e) => {
                                const next = [...jobExperiences]; next[idx].endDate = e.target.value; setJobExperiences(next);
                            }} /></label>
                        </div>
                        <label>Задачи<textarea value={exp.description} onChange={(e) => {
                            const next = [...jobExperiences]; next[idx].description = e.target.value; setJobExperiences(next);
                        }} required /></label>
                        <button type="button" className="danger" onClick={() => setJobExperiences(jobExperiences.filter((_, i) => i !== idx))}>Удалить опыт</button>
                    </div>
                ))}
                <button type="button" onClick={() => setJobExperiences([...jobExperiences, { position: '', companyName: '', description: '', startDate: new Date().toISOString().split('T')[0] }])}>+ Добавить опыт</button>

                {updateMutation.isError && <p className="error">Ошибка сохранения: {(updateMutation.error as any).message}</p>}
                <div className="toolbar" style={{ marginTop: '20px' }}>
                    <button type="submit" disabled={updateMutation.isPending}>Сохранить изменения</button>
                    <button type="button" className="danger" onClick={() => deleteMutation.mutate()}>Удалить резюме</button>
                    <Link to="/me/resumes">Назад</Link>
                </div>
            </form>
        </main>
    );
}