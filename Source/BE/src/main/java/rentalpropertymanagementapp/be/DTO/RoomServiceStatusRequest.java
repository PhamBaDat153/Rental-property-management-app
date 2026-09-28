package rentalpropertymanagementapp.be.DTO;

import jakarta.validation.constraints.NotNull;

public record RoomServiceStatusRequest(@NotNull Boolean is_active) { }
