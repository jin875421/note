package me.jin.note.presenter;

import android.content.Context;

import java.util.List;

import me.jin.note.bean.Category;
import me.jin.note.bean.Note;
import me.jin.note.contract.GetNoteContract;
import me.jin.note.model.GetNoteModelImpl;


public class GetNotePresenterImpl implements GetNoteContract.Presenter {
    private GetNoteContract.View mView;
    private GetNoteContract.Model mModel;
    public GetNotePresenterImpl(GetNoteContract.View view){
        this.mView=view;
        mModel=new GetNoteModelImpl(this);
    }

    @Override
    public void getNote(Context context,String category) {
        mModel.getNote(context,category);
    }

    @Override
    public void getCategory(Context context) {
        mModel.getCategory(context);
    }

    @Override
    public void getCategorySuccess(List<Category> categoryList) {
        mView.getCategorySuccess(categoryList);
    }

    @Override
    public void getNoteSuccess(List<Note> list) {
        mView.getNoteSuccess(list);
    }

    @Override
    public void getError(String msg) {
        mView.getError(msg);
    }

}
