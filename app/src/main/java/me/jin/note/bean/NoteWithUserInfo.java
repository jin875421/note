package me.jin.note.bean;

import android.os.Parcelable;

import java.io.Serializable;

public class NoteWithUserInfo implements Serializable {
    private Note note;
    private UserInfo userInfo;

    public NoteWithUserInfo(Note note, UserInfo userInfo) {
        this.note = note;
        this.userInfo = userInfo;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
