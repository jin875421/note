package me.jin.note.model;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jin.note.bean.Category;
import me.jin.note.bean.Note;
import me.jin.note.contract.GetNoteContract;
import me.jin.note.db.NoteManager;

public class GetNoteModelImpl implements GetNoteContract.Model {
    private GetNoteContract.Presenter mPresenter;
    private NoteManager noteManager;
    public GetNoteModelImpl(GetNoteContract.Presenter presenter){
        this.mPresenter=presenter;
    }

    @Override
    public void getNote(Context context,String category) {
        noteManager=new NoteManager(context);
        List<Note>noteList=new ArrayList<>();
        if (noteManager.isEmpty()){
            noteManager.initTable();
        }
        if (category.equals("所有")){
            noteList=noteManager.selectAll();
        }else {
            noteList=noteManager.selectCategory(category);//noteList
        }
        if (noteList!=null){
            //对note按照最后编辑时间重新排序
            Collections.sort(noteList);
            mPresenter.getNoteSuccess(noteList);
        }
    }

    @Override
    public void getCategory(Context context) {
        noteManager=new NoteManager(context);
        if (noteManager.isEmpty()){
            noteManager.initTable();
        }
        List<Category> categoryList=noteManager.selectAllCategoryBean();
        Collections.sort(categoryList);
        mPresenter.getCategorySuccess(categoryList);
    }
}
