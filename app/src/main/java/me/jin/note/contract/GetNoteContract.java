package me.jin.note.contract;

import android.content.Context;

import java.util.List;

import me.jin.note.bean.Category;
import me.jin.note.bean.Note;



public interface GetNoteContract {
    interface View{//activity或fragment
        void getNoteSuccess(List<Note> list);
        void getCategorySuccess(List<Category>categoryList);
        void getError(String msg);
    }
    interface Model{//后台耗时操作
        void getNote(Context context,String category);
        void getCategory(Context context);
    }
    interface Presenter{//桥梁,连接UI和后台耗时操作
        void getNote(Context context,String category);
        void getCategory(Context context);
        void getNoteSuccess(List<Note>list);
        void getCategorySuccess(List<Category>categoryList);
        void getError(String msg);
    }
}
