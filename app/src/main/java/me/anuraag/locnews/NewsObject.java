package me.anuraag.locnews;

/**
 * Created by Anuraag on 12/7/14.
 */
public class NewsObject {
    private String name,description,url,author;

    public NewsObject(String name,String description, String url, String author){
        this.name = name;
        this.description = description;
        this.url = url;
        this.author = author;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public void setAuthor(String author){
        this.author = author;
    }
    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public String getUrl(){
        return this.url;
    }
    public String getAuthor(){
        return this.author;
    }
    public String toString(){
        return this.name + " " + this.description  + " "+ this.author;
    }
}
