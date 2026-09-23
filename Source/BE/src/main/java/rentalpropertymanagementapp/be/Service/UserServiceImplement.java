package rentalpropertymanagementapp.be.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rentalpropertymanagementapp.be.Model.User.User;
import rentalpropertymanagementapp.be.Repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImplement implements UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserServiceImplement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
