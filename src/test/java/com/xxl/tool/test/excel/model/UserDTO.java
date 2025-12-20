package com.xxl.tool.test.excel.model;


public class UserDTO {

    private long userId;
    private String userName;
    private UserSexEnum sex;

    public UserDTO() {
    }
    public UserDTO(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.sex = UserSexEnum.MALE;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserSexEnum getSex() {
        return sex;
    }

    public void setSex(UserSexEnum sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", sex=" + sex +
                '}';
    }
}
