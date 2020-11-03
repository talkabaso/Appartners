package com.example.appartners;

public class upload {

        private String mName;
        private String mImageUrl;

        public upload() {
            //empty constructor needed
        }

        public upload(String name, String imageUrl) {
            if (name.trim().equals(""))  {  // trim->ignore spaces
                name = "No Name";
            }

            mName = name;
            mImageUrl = imageUrl;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getImageUrl() {
            return mImageUrl;
        }

        public void setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
        }
    }