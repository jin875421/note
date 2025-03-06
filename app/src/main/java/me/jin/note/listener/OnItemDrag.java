package me.jin.note.listener;


public interface OnItemDrag {
    void onMove(int fromPosition,int toPosition);
    void onSwiped(int position);
}
