package com.example.appartners;

import java.util.ArrayList;

public class Apartment extends User {

    private double price;
    private int occupants;
    private String street;
    private String roomType;
    private int numOfRooms;
    private ArrayList<String> imagesUri;
    private ArrayList<UserImage> myFav;


    public Apartment(){

        super();
        this.price=0;
        this.occupants=0;
        this.street="";
        this.roomType ="";
        this.numOfRooms=0;
        this.imagesUri=new ArrayList<String>();
        this.myFav=new ArrayList<UserImage>();
    }


    public Apartment(String id,String name, String gender, String city, int age, String email){

        super(id,name,gender,city,age,email);
        this.price=0;
        this.occupants=0;
        this.street="";
        this.roomType = "";
        this.numOfRooms=0;
        this.imagesUri=new ArrayList<String>();
        this.myFav=new ArrayList<UserImage>();

    }


    public Apartment(Apartment other){

        this.price=other.price;
        this.occupants=other.occupants;
        this.street=other.street;
        this.roomType =other.roomType;
        this.numOfRooms=other.numOfRooms;
        this.imagesUri=other.imagesUri;
        this.myFav=other.myFav;

    }

    public double getPrice() {
        return price;
    }

    public int getOccupants() {
        return occupants;
    }

    public String getStreet() {
        return street;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNumOfRooms() {
        return numOfRooms;
    }

    public ArrayList<String> getImagesUri() {
        return imagesUri;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setOccupants(int occupants) {
        this.occupants = occupants;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setNumOfRooms(int numOfRooms) {
        this.numOfRooms = numOfRooms;
    }

    public void setImagesUri(ArrayList<String> imagesUri) {
        this.imagesUri = imagesUri;
    }

    public void addImg(String img){

        imagesUri.add(img);
    }

    public String getImg(int i){

        if(i<imagesUri.size()){

            return imagesUri.get(i);

        }else{

            return "";
        }
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


}