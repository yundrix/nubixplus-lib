package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.Supplier;
import com.nubixplus.lib.domain.types.DocumentType;
import com.nubixplus.lib.utils.Documents;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Suplidor de la compania, con el documento ya formateado para mostrar. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplierDTO extends BaseDTO {

    private String code;
    private String name;
    private String legalName;
    private String commercialName;
    private DocumentType documentType;
    private String documentNumber;
    private String documentNumberFormatted;
    private String email;
    private String phone;
    private String address;
    private String contactName;
    private Integer paymentTermsDays;
    private boolean active;

    protected SupplierDTO(Supplier supplier) {
        super(supplier);
        if (Objects.nonNull(supplier)) {
            this.code = supplier.getCode();
            this.name = supplier.getDisplayName();
            this.legalName = supplier.getLegalName();
            this.commercialName = supplier.getCommercialName();
            this.documentType = supplier.getDocumentType();
            this.documentNumber = supplier.getDocumentNumber();
            this.documentNumberFormatted = Documents.format(supplier.getDocumentNumber());
            this.email = supplier.getEmail();
            this.phone = supplier.getPhone();
            this.address = supplier.getAddress();
            this.contactName = supplier.getContactName();
            this.paymentTermsDays = supplier.getPaymentTermsDays();
            this.active = supplier.isActive();
        }
    }

    public static SupplierDTO of(Supplier supplier) {
        return Optional.ofNullable(supplier).map(SupplierDTO::new).orElse(null);
    }

    public static List<SupplierDTO> of(Collection<Supplier> suppliers) {
        if (Objects.isNull(suppliers)) {
            return List.of();
        }
        return suppliers.stream()
                .sorted(Comparator.comparing(Supplier::getCode))
                .map(SupplierDTO::of)
                .toList();
    }
}
