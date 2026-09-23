package rentalpropertymanagementapp.be.Model.Contract;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractTenantId implements Serializable {

    private UUID contract_id;
    private UUID tenant_id;
}
