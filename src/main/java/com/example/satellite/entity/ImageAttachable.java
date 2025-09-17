package com.example.satellite.entity;



public interface ImageAttachable {
    Long getId();
    byte[] getImageData();
    void setImageData(byte[] data);
    String getImageContentType();
    void setImageContentType(String ct);
    String getImageFilename();
    void setImageFilename(String name);
}

