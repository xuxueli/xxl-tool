package com.xxl.tool.test.jsonrpc.service.impl;

import com.xxl.tool.response.Response;
import com.xxl.tool.test.jsonrpc.service.UserService;
import com.xxl.tool.test.jsonrpc.service.model.ResultDTO;
import com.xxl.tool.test.jsonrpc.service.model.UserDTO;

import java.util.*;

public class UserServiceImpl implements UserService {

    @Override
    public ResultDTO createUser(UserDTO userDTO) {
        System.out.println("createUser: " + userDTO.toString());
        return new ResultDTO(true, "createUser success");
    }

    @Override
    public ResultDTO updateUser(String name, Integer age) {
        System.out.println("updateUser: name: " + name + ", age: " + age);
        return new ResultDTO(true, "updateUser success");
    }

    @Override
    public UserDTO loadUser(String name) {
        System.out.println("loadUser: name: " + name);
        return new UserDTO(name+"(success)", new Random().nextInt(28));
    }

    @Override
    public List<UserDTO> queryUserByAge() {
        List<UserDTO> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            users.add(new UserDTO("user(success)"+i, 17+i));
        }
        return users;
    }

    @Override
    public void refresh() {
        System.out.println("refresh");
    }

    @Override
    public Response<UserDTO> load(String name) {
        System.out.println("load: name: " + name);
        return Response.ofSuccess(new UserDTO(name+"(success)", new Random().nextInt(28)));
    }

}
