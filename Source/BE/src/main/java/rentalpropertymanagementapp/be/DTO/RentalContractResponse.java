package rentalpropertymanagementapp.be.DTO;

import rentalpropertymanagementapp.be.Model.Contract.RentalContract;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalContractResponse(UUID contract_id, UUID room_id, LocalDate start_date, LocalDate end_date,
                                     LocalDateTime signed_at, BigDecimal rent_amount, BigDecimal deposit_required,
                                     Integer billing_day, Integer payment_due_days, ActiveStatus status,
                                     String terms, String document_url, LocalDateTime terminated_at,
                                     String termination_reason, LocalDateTime created_at, LocalDateTime updated_at) {
    public static RentalContractResponse from(RentalContract contract) {
        return new RentalContractResponse(contract.getContract_id(), contract.getRoom().getRoom_id(),
                contract.getStart_date(), contract.getEnd_date(), contract.getSigned_at(), contract.getRent_amount(),
                contract.getDeposit_required(), contract.getBilling_day(), contract.getPayment_due_days(), contract.getStatus(),
                contract.getTerms(), contract.getDocument_url(), contract.getTerminated_at(), contract.getTermination_reason(),
                contract.getCreated_at(), contract.getUpdated_at());
    }
}
