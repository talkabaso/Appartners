package com.example.appartners;

import java.util.ArrayList;

public class Partner extends User {

   private String imgUrl;
   private ArrayList<UserImage> myFav;


    public Partner(){

        super();
        imgUrl="";
        this.myFav=new ArrayList<UserImage>();
    }

    public ArrayList<UserImage> getMyFav() {
        return myFav;
    }

    public void setMyFav(ArrayList<UserImage> myFav) {
        this.myFav = myFav;
    }

    public void addFav(UserImage fav){

        myFav.add(fav);
    }

    public UserImage getFav(int i){

        return myFav.get(i);
    }

    public Partner(String id, String name, String gender, String city, int age, String email){

        super(id,name,gender,city,age,email);
        imgUrl="";
        this.myFav=new ArrayList<UserImage>();
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
