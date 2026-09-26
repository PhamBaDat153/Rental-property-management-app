package rentalpropertymanagementapp.be;

import org.junit.jupiter.api.Test;
import rentalpropertymanagementapp.be.DTO.ContractTenantRequest;
import rentalpropertymanagementapp.be.DTO.RentalContractRequest;
import rentalpropertymanagementapp.be.Exception.ResourceConflictException;
import rentalpropertymanagementapp.be.Model.Enum.ActiveStatus;
import rentalpropertymanagementapp.be.Service.ContractTenantServiceImplement;
import rentalpropertymanagementapp.be.Service.RentalContractResourceImplement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

import rentalpropertymanagementapp.be.Repository.RentalContractRepository;
import rentalpropertymanagementapp.be.Repository.RoomRepository;
import rentalpropertymanagementapp.be.Repository.LocationRepository;
import rentalpropertymanagementapp.be.Service.RoomServiceImplement;
import rentalpropertymanagementapp.be.Model.Room.Room;

import static org.mockito.Mockito.*;

class RentalResourceCrudTest {
    @Test
    void rejectsContractEndBeforeStart() {
        RentalContractResourceImplement service = new RentalContractResourceImplement(null, null, null, null, null);
        assertThrows(ResourceConflictException.class, () -> service.create(new RentalContractRequest(
                UUID.randomUUID(), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1),
                BigDecimal.ZERO, BigDecimal.ZERO, null, 0, ActiveStatus.ACTIVE, null, null), null));
    }

    @Test
    void rejectsAssignmentMoveOutBeforeMoveIn() {
        ContractTenantServiceImplement service = new ContractTenantServiceImplement(null, null, null);
        assertThrows(ResourceConflictException.class, () -> service.create(new ContractTenantRequest(
                UUID.randomUUID(), UUID.randomUUID(), false,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1))));
    }

    @Test
    void rejectsRoomCreateForMissingLocation() {
        LocationRepository locations = mock(LocationRepository.class);
        when(locations.findById(any())).thenReturn(java.util.Optional.empty());
        RoomServiceImplement service = new RoomServiceImplement(mock(RoomRepository.class), locations, mock(RentalContractRepository.class), mock(rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader.class));
        assertThrows(rentalpropertymanagementapp.be.Exception.ResourceNotFoundException.class, () -> service.create(new rentalpropertymanagementapp.be.DTO.RoomRequest(
                UUID.randomUUID(), "A1", null, null, null, 1, BigDecimal.ZERO, rentalpropertymanagementapp.be.Model.Enum.AvailableStatus.AVAILABLE, null), null));
    }

    @Test
    void protectsRoomDeleteWhenContractExists() {
        UUID roomId = UUID.randomUUID();
        RoomRepository rooms = mock(RoomRepository.class);
        when(rooms.findById(roomId)).thenReturn(java.util.Optional.of(new Room()));
        when(mock(RentalContractRepository.class).existsByRoomId(roomId)).thenReturn(true);
        RentalContractRepository contracts = mock(RentalContractRepository.class);
        when(contracts.existsByRoomId(roomId)).thenReturn(true);
        RoomServiceImplement service = new RoomServiceImplement(rooms, mock(LocationRepository.class), contracts, mock(rentalpropertymanagementapp.be.Ultilities.CloudinaryFileUploader.class));
        assertThrows(rentalpropertymanagementapp.be.Exception.ResourceConflictException.class, () -> service.delete(roomId));
        verify(rooms, never()).deleteById(roomId);
    }
}
