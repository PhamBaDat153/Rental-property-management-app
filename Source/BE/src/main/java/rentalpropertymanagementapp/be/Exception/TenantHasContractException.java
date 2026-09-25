package rentalpropertymanagementapp.be.Exception;

public class TenantHasContractException extends RuntimeException {
    public TenantHasContractException() {
        super("Không thể xóa user vì tenant đã được sử dụng trong hợp đồng");
    }
}
