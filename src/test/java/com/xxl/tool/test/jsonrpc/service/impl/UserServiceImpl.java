package com.xxl.tool.test.jsonrpc.service.impl;

import com.xxl.tool.test.jsonrpc.service.UserService;
import com.xxl.tool.test.jsonrpc.service.model.ResultDTO;
import com.xxl.tool.test.jsonrpc.service.model.UserDTO;

import java.util.*;

public class UserServiceImpl implements UserService {

    @Override
    public ResultDTO createUser(UserDTO userDTO) {
        System.out.println("UserServiceImpl userDTO: " + userDTO.toString());
        return new ResultDTO(true, "createUser success");
    }

    @Override
    public ResultDTO updateUser(String name, Integer age) {
        System.out.println("UserServiceImpl name: " + name + ", age: " + age);
        return new ResultDTO(true, "updateUser success");
    }

    @Override
    public UserDTO loadUser(String name) {
        return new UserDTO(name, new Random().nextInt(28));
    }

    @Override
    public List<UserDTO> queryUserByAge() {
        List<UserDTO> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            users.add(new UserDTO("user"+i, 17+i));
        }
        return users;
    }

}
