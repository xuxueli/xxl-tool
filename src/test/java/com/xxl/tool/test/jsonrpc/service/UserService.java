package com.xxl.tool.test.jsonrpc.service;

import com.xxl.tool.test.jsonrpc.service.model.ResultDTO;
import com.xxl.tool.test.jsonrpc.service.model.UserDTO;

import java.util.List;

public interface UserService {

    public ResultDTO createUser(UserDTO userDTO);

    public ResultDTO updateUser(String name, Integer age);

    public UserDTO loadUser(String name);

    public List<UserDTO> queryUserByAge();

}
