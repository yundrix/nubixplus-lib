package com.nubixplus.lib.stereotype;

import com.nubixplus.lib.domain.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public abstract class DefaultBaseService<T extends BaseEntity> implements BaseService<T> {

    private static final String EMPTY = "";
    private static final String WILDCARD = "Service";
    private static final String DEFAULT_PREFIX = "Default";
    public static final String NOT_FOUND_MESSAGE = "No se pudo encontrar el registro con ID: %s. %s.";

    @Override
    public T save(T entity) {
        return this.getRepository().save(entity);
    }

    @Override
    @Transactional
    public T create(T entity) {
        return this.getRepository().save(entity);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return this.getRepository().findAll(pageable);
    }

    @Override
    public List<T> findAll(Specification<T> specification) {
        return this.getRepository().findAll(specification);
    }

    @Override
    public Page<T> findAll(Pageable pageable, Specification<T> specification) {
        return this.findAll(specification, pageable);
    }

    @Override
    public Page<T> findAll(Specification<T> specification, Pageable pageable) {
        return this.getRepository().findAll(specification, pageable);
    }

    @Override
    public T findById(Long id) {
        return this.getRepository().findById(id).orElseThrow(() -> {
            final String entityName = getClass().getSimpleName()
                    .replace(DEFAULT_PREFIX, EMPTY)
                    .replace(WILDCARD, EMPTY);
            return new NotFoundException(String.format(NOT_FOUND_MESSAGE, id, entityName));
        });
    }

    @Override
    public Optional<T> getById(Long id) {
        return this.getRepository().findById(id);
    }

    protected abstract BaseRepository<T> getRepository();

}
