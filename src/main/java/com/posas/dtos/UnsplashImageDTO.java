package com.posas.dtos;

import java.util.List;

import lombok.Data;

@Data
public class UnsplashImageDTO {
    private String id;
    private String created_at;
    private String updated_at;
    private String promoted_at;
    private Long width;
    private Long height;
    private String color;
    private String blur_hash;
    private String description;
    private String alt_description;
    private UnsplashImageURLs urls;
    private Object links;
    private Long likes;
    private Boolean liked_by_user;
    private Object current_user_collections;
    private Boolean sponsorship;
    private Object topic_submissions;
    private Object user;
    private Object exif;
    private Object location;
    private Object meta;
    private Boolean public_domain;
    private List<Object> tags;
    private List<Object> tags_preview;
    private Long views;
    private String downloads;
    private Object topics;
}