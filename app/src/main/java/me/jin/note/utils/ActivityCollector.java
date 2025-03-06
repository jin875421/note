package me.jin.note.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;


public class ActivityCollector {
    private static List<Activity>activityList=new ArrayList<>();
    public static void addActivity(Activity act){
        activityList.add(act);
    }
    public static void removeActivity(Activity act){
        activityList.remove(act);
    }
    public static Activity getTopActivity(){
        if (activityList.isEmpty()){
            return null;
        }else {
            return activityList.get(activityList.size()-1);//size()-1
        }
    }
}
