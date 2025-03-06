package me.jin.note.listener;

import java.util.List;


/**
 * 权限管理接口
 */
public interface PermissionListener {
    void onGranted();
    //void onGranted(List<String>grantedPermission);
    void onDenied(List<String> deniedPermission);
}
