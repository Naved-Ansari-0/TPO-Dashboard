package com.example.tpo;

public class DataSingleton {
    private static final DataSingleton instance = new DataSingleton();
    private String accessToken;

    private String searchByRollNoAPILink;
    private DataSingleton(){
    }
    public static DataSingleton getInstance(){
        return instance;
    }
    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public void setSearchByRollNoAPILink(String searchByRollNoAPILink){
        this.searchByRollNoAPILink = searchByRollNoAPILink;
    }

    public String getSearchByRollNoAPILink(){
        return searchByRollNoAPILink;
    }


}
