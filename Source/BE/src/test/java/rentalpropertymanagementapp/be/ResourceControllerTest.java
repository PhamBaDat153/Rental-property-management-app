package rentalpropertymanagementapp.be;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rentalpropertymanagementapp.be.Controller.ApiExceptionHandler;
import rentalpropertymanagementapp.be.Controller.LocationController;
import rentalpropertymanagementapp.be.Controller.RoomController;
import rentalpropertymanagementapp.be.DTO.LocationRequest;
import rentalpropertymanagementapp.be.DTO.LocationResponse;
import rentalpropertymanagementapp.be.DTO.RoomRequest;
import rentalpropertymanagementapp.be.DTO.RoomResponse;
import rentalpropertymanagementapp.be.Model.Enum.AvailableStatus;
import rentalpropertymanagementapp.be.Model.Room.Location;
import rentalpropertymanagementapp.be.Model.Room.Room;
import rentalpropertymanagementapp.be.Service.LocationService;
import rentalpropertymanagementapp.be.Service.RoomService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import org.springframework.mock.web.MockMultipartFile;

class ResourceControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void locationResponseDoesNotSerializeRooms() throws Exception {
        LocationService service = mock(LocationService.class);
        Location location = Location.builder().location_code("LOC-1").address_line("Address")
                .province_name("Province").status(AvailableStatus.AVAILABLE).build();
        when(service.getLocationById(any())).thenReturn(location);
        MockMvc mvc = standaloneSetup(new LocationController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();

        mvc.perform(get("/be/locations/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location_code").value("LOC-1"))
                .andExpect(jsonPath("$.rooms").doesNotExist());
    }

    @Test
    void invalidRoomRequestReturnsBadRequest() throws Exception {
        MockMvc mvc = standaloneSetup(new RoomController(mock(RoomService.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        RoomRequest request = new RoomRequest(null, "", null, null, BigDecimal.valueOf(-1), 0,
                BigDecimal.valueOf(-1), null, null);

        mvc.perform(multipart("/be/rooms")
                        .file(new MockMultipartFile("room", "room.json", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request)))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
