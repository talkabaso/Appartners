package com.example.appartners;

import java.util.Objects;

public class UserImage extends User {

    private String imgUser;

    public UserImage(){

        this.imgUser="";
    }

    public UserImage(String imgUser){

        this.imgUser=imgUser;
    }

    public UserImage(User u,String imgUser){

        super(u.getId(),u.getName(),u.getGender(),u.getCity(),u.getAge(),u.getEmail());
        this.imgUser=imgUser;

    }


    public String getImgUser() {
        return imgUser;
    }

    public void setImgUser(String imgUser) {
        this.imgUser = imgUser;
    }
}
