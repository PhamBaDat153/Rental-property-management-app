package rentalpropertymanagementapp.be;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rentalpropertymanagementapp.be.Controller.ApiExceptionHandler;
import rentalpropertymanagementapp.be.Controller.UserController;
import rentalpropertymanagementapp.be.DTO.LoginRequest;
import rentalpropertymanagementapp.be.Model.Enum.LoginType;
import rentalpropertymanagementapp.be.Model.User.Tenant;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Service.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void loginReturnsOnlyBoundedUserFields() throws Exception {
        UserService service = mock(UserService.class);
        User user = User.builder().user_id(UUID.randomUUID()).user_name("landlord").tenant(new Tenant()).build();
        when(service.authenticate(any(), any(), any())).thenReturn(Optional.of(user));
        MockMvc mvc = standaloneSetup(new UserController(service)).setControllerAdvice(new ApiExceptionHandler()).build();

        mvc.perform(post("/be/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new LoginRequest("landlord", "password", LoginType.Manage))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(user.getUser_id().toString()))
                .andExpect(jsonPath("$.user_name").value("landlord"))
                .andExpect(jsonPath("$.tenant").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());
    }

    @Test
    void loginRejectsInvalidOrMalformedRequests() throws Exception {
        UserService service = mock(UserService.class);
        MockMvc mvc = standaloneSetup(new UserController(service)).setControllerAdvice(new ApiExceptionHandler()).build();

        mvc.perform(post("/be/user/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        mvc.perform(post("/be/user/login").contentType(MediaType.APPLICATION_JSON).content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginReturnsUnauthorizedWithoutUserData() throws Exception {
        UserService service = mock(UserService.class);
        when(service.authenticate(any(), any(), any())).thenReturn(Optional.empty());
        MockMvc mvc = standaloneSetup(new UserController(service)).setControllerAdvice(new ApiExceptionHandler()).build();

        mvc.perform(post("/be/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new LoginRequest("landlord", "wrong", LoginType.Manage))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.user_id").doesNotExist());
    }
}
