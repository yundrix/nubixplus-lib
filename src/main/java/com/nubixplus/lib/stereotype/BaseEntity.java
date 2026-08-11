package com.nubixplus.lib.stereotype;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class BaseEntity implements Serializable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void persist() {
        this.prePersist();
    }

    @PreUpdate
    public void update() {
        this.preUpdate();
    }

    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isNew() {
        return this.id == null;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
               "id=" + id +
               '}';
    }
}
