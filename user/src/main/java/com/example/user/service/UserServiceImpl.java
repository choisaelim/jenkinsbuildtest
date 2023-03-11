package com.example.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user.dto.ResponseOrder;
import com.example.user.dto.UserDto;
import com.example.user.jpa.UserEntity;
import com.example.user.jpa.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final BCryptPasswordEncoder passEncoder;

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        // client 단에서 받은 userdto를 entity 클래스로 변환
        UserEntity entity = mapper.map(userDto, UserEntity.class);
        entity.setEncryptedPwd(passEncoder.encode(userDto.getPwd()));
        userRepository.save(entity);

        UserDto userVo = mapper.map(entity, UserDto.class);
        return userVo;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity entity = userRepository.findByUserId(userId);

        if (entity == null)
            throw new UsernameNotFoundException("user id not found");

        UserDto userDto = new ModelMapper().map(entity, UserDto.class);

        List<ResponseOrder> ordersList = new ArrayList<>();
        userDto.setOrdersList(ordersList);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByEmail(username);

        if (entity == null)
            throw new UsernameNotFoundException(username);

        UserDetails userDetails = User.builder()
                .username(entity.getEmail())
                .password(entity.getEncryptedPwd())
                .roles("USER")
                .build();

        return userDetails;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email);

        if (entity == null)
            throw new UsernameNotFoundException("user id not found");

        UserDto userDto = new ModelMapper().map(entity, UserDto.class);
        return userDto;
    }

}
