package me.jin.note.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;



public class Note implements Serializable,Comparable<Note> {
    private String uniqueId;
    private String title;
    private String userId;
    private String id;//这里数据类型应该改为int
    private String content;
    private String firstTime;//note生成时间
    private String lastTime;//note最后编辑时间
    private String category;
    private boolean isFlag=false;//当isFlag=true,note的背景drawable变色
    private boolean isShare  = false;//是否共享
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public boolean isShare() {
        return isShare;
    }
    public void setShare(boolean share) {
        isShare = share;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String s){
        this.content=s;
    }
    public void setFirstTime(String s){
        this.firstTime=s;
    }
    public String getFirstTime(){
        return firstTime;
    }
    public void setLastTime(String s){
        this.lastTime=s;
    }
    public String getLastTime(){
        return lastTime;
    }
    public void setFlag(boolean isFlag){
        this.isFlag=isFlag;
    }
    public boolean isFlag(){
        return isFlag;
    }
    public void setCategory(String s){
        this.category=s;
    }
    public String getCategory(){
        return category;
    }
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int compareTo(@NonNull Note o) {
        if (Long.parseLong(this.getLastTime())==Long.parseLong(o.getLastTime())){//string型转long型
            return 0;
        }else if (Long.parseLong(this.getLastTime())>Long.parseLong(o.getLastTime())){
            return -1;
        }else {
            return 1;
        }
    }
}
