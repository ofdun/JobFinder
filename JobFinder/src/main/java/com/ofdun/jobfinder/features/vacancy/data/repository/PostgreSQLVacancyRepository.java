package com.ofdun.jobfinder.features.vacancy.data.repository;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.data.mapper.VacancyMapper;
import com.ofdun.jobfinder.features.vacancy.data.spec.VacancySpecifications;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLVacancyRepository implements VacancyRepository {
    private final VacancyJpaRepository vacancyJpaRepository;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public Long createVacancy(VacancyModel vacancyModel) {
        return vacancyJpaRepository.save(VacancyMapper.toEntity(vacancyModel)).getId();
    }

    @Override
    public Optional<VacancyModel> getVacancyById(Long id) {
        return vacancyJpaRepository.findById(id).map(VacancyMapper::toModel);
    }

    @Override
    public VacancyModel updateVacancy(VacancyModel vacancyModel) {
        return vacancyJpaRepository.save(VacancyMapper.toEntity(vacancyModel)).getId() != null
                ? vacancyModel
                : null;
    }

    @Override
    public Boolean deleteVacancy(Long id) {
        return vacancyJpaRepository
                .findById(id)
                .map(
                        entity -> {
                            vacancyJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }

    @Override
    public PageResult<VacancyModel> searchVacancies(
            VacancySearchFilter filter, OffsetPagination pagination) {
        OffsetPagination p = pagination == null ? OffsetPagination.builder().build() : pagination;

        int limit = p.getLimit();
        int offset = p.getOffset();

        Specification<VacancyEntity> spec = VacancySpecifications.byFilter(filter);

        long total = vacancyJpaRepository.count(spec);

        var cb = entityManager.getCriteriaBuilder();

        var cq = cb.createQuery(VacancyEntity.class);
        var root = cq.from(VacancyEntity.class);
        cq.select(root);

        var predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        if (p.getSortBy() != null && !p.getSortBy().isBlank()) {
            cq.orderBy(
                    p.isSortDesc()
                            ? cb.desc(root.get(p.getSortBy()))
                            : cb.asc(root.get(p.getSortBy())));
        }

        var typed = entityManager.createQuery(cq);
        typed.setFirstResult(offset);
        typed.setMaxResults(limit);
        var entities = typed.getResultList();

        var items = entities.stream().map(VacancyMapper::toModel).toList();

        int page = limit == 0 ? 0 : offset / limit;
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / (double) limit);

        return new PageResult<>(items, page, limit, total, totalPages);
    }
}
