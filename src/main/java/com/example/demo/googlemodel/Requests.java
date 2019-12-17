package com.example.demo.googlemodel;

import java.util.List;

public class Requests
{
    private Image image;

    List<Features> features;

    public void setImage(Image image){
        this.image = image;
    }
    public Image getImage(){
        return image;
    }
    public void setFeatures(List<Features> features){
        this.features = features;
    }
    public List<Features> getFeatures(){
        return features;
    }
}