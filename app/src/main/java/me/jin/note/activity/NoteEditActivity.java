package me.jin.note.activity;

import static android.content.ContentValues.TAG;
import static me.jin.note.activity.MainActivity.ip;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.iflytek.sparkchain.core.raasr.RAASR;
import com.iflytek.sparkchain.core.raasr.RAASRCallbacks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.CheckedOutputStream;

import jp.wasabeef.richeditor.RichEditor;
import me.jin.note.R;
import me.jin.note.base.BaseActivity;
import me.jin.note.bean.Note;
import me.jin.note.db.NoteManager;
import me.jin.note.utils.AudioRecorder;
import me.jin.note.utils.DouBaoUtil;
import me.jin.note.utils.FileUtils;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteEditActivity extends BaseActivity {
    private RichEditor et_addNote;
    private TextView mTitle;
    private final int RESULT_LOAD_IMAGES = 1, RESULT_CAMERA_IMAGE = 2;
    boolean isrun = false;
    private ImageView back, save,share;
    public String url = "http://" + ip + "/Note";
    private String id;
    private RAASR mRAASR;
    private AudioRecorder recorder;
    private static final String RAASRAPIKEY = "c80b4f338bac48b1d0c91b7f8c499cf4";
    private boolean flag = false; //用于是否处于录音状态
    private String orderId = null;
    private String resultTypes = "transfer";
    private String mCurrentPhotoPath;
    private boolean isSpinnerInitialized = true; // 添加标志变量
    private String content;
    private String category;
    private NoteManager noteManager;
    private EditText editText;
    private String title,uniqueId;
    RAASRCallbacks mRAASRCallbacks = new RAASRCallbacks() {
        @Override
        public void onResult(RAASR.RaAsrResult raAsrResult, Object o) {
            //以下信息需要开发者根据自身需求，如无必要，可不需要解析执行。
            int status = raAsrResult.getStatus();//订单流程状态
            String orderResult = raAsrResult.getOrderResult();//转写结果
            RAASR.RaAsrTransResult[] raAsrTransResults = raAsrResult.getTransResult();//翻译结果实例
            orderId = raAsrResult.getOrderId();//转写订单ID
            int failType = raAsrResult.getFailType();//订单异常状态
            long originalDuration = raAsrResult.getOriginalDuration();//原始音频时长，单位毫秒
            long realDuration = raAsrResult.getRealDuration();//真实音频时长，单位毫秒
            int taskEstimateTime = raAsrResult.getTaskEstimateTime();//订单预估耗时，单位毫秒

            String info = "{status:" + status + ",orderId:" + orderId + ",failType:" + failType + ",originalDuration:"
                    + originalDuration + ",realDuration:" + realDuration + ",taskEstimateTime:" + taskEstimateTime + "}\n";
            Log.d(TAG, info);
            FileUtils.longLog(TAG, orderResult + "\n");
            if ("transfer".equals(resultTypes)) {
                if (!TextUtils.isEmpty(orderResult)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //在et_addNote中插入结果
                            String html = et_addNote.getHtml();
                            et_addNote.setHtml(html + analysisResult(orderResult));
                            et_addNote.insertLink(orderResult, "");
                        }
                    });
                } else {
                }
            } else if ("translate".equals(resultTypes)) {
                String transResult = "";
                for (int i = 0; i < raAsrTransResults.length; i++) {
                    transResult = transResult + raAsrTransResults[i].getDst();
                }
            }
            isrun = false;
        }

        @Override
        public void onError(RAASR.RaAsrError raAsrError, Object o) {
            String errMsg  = raAsrError.getErrMsg();//错误信息
            int errCode    = raAsrError.getCode();//错误码
            String orderId = raAsrError.getOrderId();//转写订单ID
            int failType   = raAsrError.getFailType();//订单异常状态
            String info = "{errMsg:"+errMsg+",errCode:"+errCode+",orderId:"+orderId+",failType:"+failType+"}\n";
            Log.d(TAG,info);
            isrun = false;
        }
    };
    @Override
    protected int setLayoutResId() {
        return R.layout.activity_note_edit;
    }

    @Override
    protected void initData() {
        noteManager=new NoteManager(mContext);
        Intent intent=getIntent();
        title=intent.getStringExtra("title");
        id=intent.getStringExtra("id");
        content=intent.getStringExtra("content");
        category=intent.getStringExtra("currentCate");
        uniqueId=intent.getStringExtra("uniqueId");
    }

    @Override
    protected void initView() {
        editText = findViewById(R.id.title);
        save = findViewById(R.id.save);
        back = findViewById(R.id.back);
        share = findViewById(R.id.share);
        share.setOnClickListener(v -> {
            shareNote();
        });
        back.setOnClickListener(v -> {
            finish();
        });
        save.setOnClickListener(v -> {
            goBack();
        });
        et_addNote=findViewById(R.id.et_note_edit);
        et_addNote.setHtml(content);
        editText.setText(title);
    }
    private void shareNote(){
        //向服务器发送请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 建立网络连接
                String updateUrl = url + "/note/share?uniqueId="+uniqueId;
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                Request request = new Request.Builder()
                        .url(updateUrl)
                        .post(requestBody)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        // 成功响应
                        String responseBody = response.body().string();
                    } else {
                        // 请求失败
                        System.out.println("Request failed: " + response.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }

    private void goBack(){
        if (TextUtils.isEmpty(et_addNote.getHtml())){
            noteManager.delete(id);
            goToNoteList();
        }else {
            String s=et_addNote.getHtml().toString();
            Note note=new Note();
            note.setTitle(editText.getText().toString());
            System.out.println(editText.getText().toString());
            note.setId(id);//noteManager需要id
            note.setContent(s);
            Date date=new Date();
            note.setLastTime(Long.toString(date.getTime()));
            note.setCategory(category);
            noteManager.update(note);
            goToNoteList();
        }
    }
    private void goToNoteList(){
        finish();
    }

    @Override
    protected void initListener() {
        /**
         * 撤销当前标签状态下所有内容
         */
        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.undo();
            }
        });
        /**
         * 恢复撤销的内容
         */
        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.redo();
            }
        });
        /**
         * 加粗
         */
        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setBold();
                //修改所选的字体粗细
            }
        });
        /**
         * 斜体
         */
        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setItalic();
            }
        });
        /**
         * 下角表
         */
        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                if (et_addNote.getHtml() == null) {
                    return;
                }
                et_addNote.setSubscript();
            }
        });
        /**
         * 上角标
         */
        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                if (et_addNote.getHtml() == null) {
                    return;
                }
                et_addNote.setSuperscript();
            }
        });

        /**
         * 删除线
         */
        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setStrikeThrough();
            }
        });
        /**
         *下划线
         */
        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setUnderline();
            }
        });
        /**
         * 设置标题（1到6）
         */
        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setHeading(6);
            }
        });
        /**
         * 设置字体颜色
         */
        Spinner spinner = findViewById(R.id.action_txt_color);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(NoteEditActivity.this,
                R.array.color_items, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://红
                        et_addNote.setTextColor(Color.RED);
                        break;
                    case 1://黄
                        et_addNote.setTextColor(Color.YELLOW);
                        break;
                    case 2://蓝
                        et_addNote.setTextColor(Color.GREEN);
                        break;
                    case 3://绿
                        et_addNote.setTextColor(Color.BLUE);
                        break;
                    case 4://黑
                        et_addNote.setTextColor(Color.BLACK);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        Spinner spinner1 = findViewById(R.id.action_bg_color); // 假设布局文件中已经有一个 Spinner 控件，ID 为 action_bg_color_spinner
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(NoteEditActivity.this,
                R.array.text_back_color_items, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://红
                        et_addNote.setTextBackgroundColor(Color.RED);
                        break;
                    case 1://黄
                        et_addNote.setTextBackgroundColor(Color.YELLOW);
                        break;
                    case 2://蓝
                        et_addNote.setTextBackgroundColor(Color.BLUE);
                        break;
                    case 3://绿
                        et_addNote.setTextBackgroundColor(Color.GREEN);
                        break;
                    case 4://黑
                        et_addNote.setTextBackgroundColor(Color.BLACK);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        /**
         * 向右缩进
         */
        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setIndent();
            }
        });
        /**
         * 向左缩进
         */
        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setOutdent();
            }
        });
        /**
         *文章左对齐
         */
        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.setAlignLeft();
            }
        });
        /**
         * 文章居中对齐
         */
        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setAlignCenter();
            }
        });
        /**
         * 文章右对齐
         */
        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setAlignRight();
            }
        });
        /**
         * 无序排列
         */
        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setBullets();
            }
        });
        /**
         * 有序排列
         */
        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setNumbers();
            }
        });
        /**
         * 引用
         */
        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.setBlockquote();
            }
        });
        /**
         * 选择框
         */
        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                et_addNote.insertTodo();
            }
        });
        //语音识别
        findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_addNote.focusEditor();
                if (!flag){
                    //进行录音
                    recorder = new AudioRecorder(getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/audiorecordtest.pcm");
                    //删除缓存文件
                    if (getExternalCacheDir().exists()) {
                        getExternalCacheDir().delete();
                    }
                    recorder.startRecording();
                    flag = true;
                }else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            recorder.stopRecording();
                            runRaasr();
                            flag =false;
                        }
                    }).start();
                }
            }
        });
        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进行图片识别
                showPopupWindow();
            }
        });
        Spinner spinner2 = findViewById(R.id.action_insert_video);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(NoteEditActivity.this,
                R.array.ai_item, R.layout.spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized) {
                    isSpinnerInitialized = false; // 设置为false，表示初始化完成
                    return; // 不执行任何操作
                }
                switch (position) {
                    case 0: // 美化
                        et_addNote.focusEditor();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(1111);
                                if (et_addNote.getHtml().length() > 0) {
                                    String response = DouBaoUtil.sendStandardRequest("你的任务是对给定的HTML格式笔记进行美化。具体操作包括对标题加粗，段落缩进，换行，调整行间距和调整字体大小，最终回复仅为美化后的HTML，不包含多余的话。\n" +
                                            "在美化时，请遵循以下操作：\n" +
                                            "1. 对于标题标签（如h1 - h6），添加style属性使其字体加粗，例如：<h1 style=\"font-weight: bold;\">标题内容</h1>。\n" +
                                            "2. 对于段落标签（p），添加style属性实现段落缩进、换行、调整行间距和字体大小，例如：<p style=\"text-indent: 2em; line-height: 1.5; font-size: 16px; margin-bottom: 10px;\">段落内容</p>。\n" +
                                            "3. 确保所有标签都正确闭合，且HTML语法正确。\n" +
                                            "4. 可以适当修改字体颜色和粗细以突出重点"+
                                            "5. 可使用的格式美化功能包括：加粗、斜体、字体颜色、字体背景颜色、字体大小、下划线等。"+
                                            "6. 回答时只回答html代码部分即可，不需任何解释"+
                                            "以下是需要美化的原始html格式笔记：\n", et_addNote.getHtml());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            et_addNote.setHtml(response);
                                        }
                                    });
                                }
                            }
                        }).start();
                        break;
                    case 1: // 润色
                        System.out.println(2222);
                        et_addNote.focusEditor();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (et_addNote.getHtml().length() > 0) {
                                    String response = DouBaoUtil.sendStandardRequest("你需要对给定的文章进行润色。文章的类型有学术论文、新闻报道和文学作品，不同类型的文章有不同的润色要求。\n" +
                                            "以下是针对不同文章类型的润色指导：\n" +
                                            "### 学术论文\n" +
                                            "- **增强专业性词汇**：替换基础常见词汇为学术领域更专业、准确的词汇。例如，“很多” 可替换为 “诸多”；“一些” 可替换为 “部分”；“另外” 可替换为 “此外”；“比如” 可替换为 “例如”；“所以” 可替换为 “因此” 等。\n" +
                                            "- **优化句式结构**：\n" +
                                            "    - 运用复杂句式，增加句子的修饰成分和逻辑层次。如将简单陈述句扩展为包含介词短语、定语从句等的复杂句子。\n" +
                                            "    - 合理使用连接词和过渡语，如 “诸多研究成果显示”“究其缘由”“此外”“因此” 等，强化逻辑关系，使论述更严谨流畅。\n" +
                                            "- **提升整体风格**：确保语言风格正式、规范，符合学术论文的严谨性要求，避免口语化表达。\n" +
                                            "\n" +
                                            "### 新闻报道\n" +
                                            "- **精准用词**：使用更具新闻行业特色、准确生动的词汇。比如，“发生” 可替换为 “突发”；“受伤” 可替换为 “遇袭”（根据具体情况）；“迅速” 可替换为 “火速”；“目前” 可替换为 “当下” 等。\n" +
                                            "- **优化句式**：\n" +
                                            "    - 使句子结构更清晰，可适当调整语序或添加主语等成分。\n" +
                                            "    - 保持简洁明了的同时，增加一些能够增强新闻冲击力的句式，如使用短句、排比句等突出关键信息。\n" +
                                            "- **强化新闻风格**：突出新闻的及时性、准确性和简洁性，语言简洁有力，符合新闻快速传达信息的特点，吸引读者注意力。\n" +
                                            "\n" +
                                            "### 文学作品\n" +
                                            "- **增添文学性词汇**：运用富有诗意、表现力强的词汇。像 “静静” 可替换为 “伫立”；“心里” 可替换为 “心间”；“想着” 可替换为 “思忖”；“轻轻” 可替换为 “袅袅”；“突然” 可替换为 “蓦地”“猝然” 等。\n" +
                                            "- **丰富句式**：\n" +
                                            "    - 运用更多修辞手法，如比喻、拟人、夸张、排比等，增强语句的感染力和画面感。\n" +
                                            "    - 增加修饰性成分，调整句式结构，营造出独特的文学氛围，使语言更具韵律美和节奏感。\n" +
                                            "- **塑造文学风格**：根据作品的主题和情感基调，打造富有个性、诗意且能引发读者情感共鸣的文学风格，注重细节描写和情感表达的细腻性。\n" +
                                            "\n" +
                                            "请输出润色后的文章内容。不改变原本格式。", et_addNote.getHtml());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            et_addNote.setHtml(response);
                                        }
                                    });
                                }
                            }
                        }).start();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    private void showPopupWindow() {
        View popView = View.inflate(this, R.layout.popupwindow_camera_need, null);
        Button bt_album = popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancel = popView.findViewById(R.id.btn_pop_cancel);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1 / 3;
        final PopupWindow popupWindow = new PopupWindow(popView, width, height);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //用户点击从相册选择
        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
                popupWindow.dismiss();
            }
        });
        //用户选择拍照上传
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NoteEditActivity.this,
                        Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {

                    // 如果权限尚未授予，则请求权限
                    ActivityCompat.requestPermissions(NoteEditActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            123);
                }
                //如果权限已经授予
                if (ContextCompat.checkSelfPermission(NoteEditActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takeCamera(RESULT_CAMERA_IMAGE);
                }

                popupWindow.dismiss();
            }
        });
        //用户选择取消
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM, 0, 50);
    }
    //启动相机
    private void takeCamera(int num) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, num);
            }
        }
    }
    //处理拍摄的图片
    private File createImageFile() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;
        try {
            image = File.createTempFile(generateFileName(), ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    //生成文件名
    private String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "JPEG_" + timeStamp + "_";
    }
    //处理所获得的图片（拍照和选相册择都在这）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGES && data != null) {
                if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    int count = clipData.getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri selectedImage = clipData.getItemAt(i).getUri();
                        File file = getFileFromUri(selectedImage);
                        OCR(file);


                    }
                } else if(data.getData() != null) {
                    Uri selectedImage = data.getData();
                    File file = getFileFromUri(selectedImage);
                    OCR(file);

                }
            }
        }
    }
    private File getFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            String displayName = null;
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                displayName = cursor.getString(index);
            }
            cursor.close();
            if (displayName != null) {
                InputStream inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    File file = new File(getCacheDir(), displayName);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                    return file;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void openFilePicker() {
        System.out.println("打开文件选择器");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGES);
    }
    private void runRaasr(){
        if(isrun){
            return;
        }
        String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/audiorecordtest.pcm";//识别音频存放路径，开发者可根据自身需求修改，demo仅做演示。路径要求有读写权限
        String resultType = "transfer";//结果类型。transfer:转写，translate:翻译。具体参考集成文档
        resultTypes = resultType;
        if(mRAASR == null){
            initraasr();
        }

        orderId = null;
        mRAASR.transLanguage("en");//翻译目标语种
        mRAASR.language("cn");//识别语种
        mRAASR.roleType(0);//是否开启角色分离,0:关闭，1:打开
        int ret = mRAASR.aRun(filePath,resultType,"12345");
        Log.d(TAG,"RAASR start:"+ret);
        if(ret !=0 ){
            isrun = false;
        }else{
            isrun = true;
        }
    }
    public void initraasr() {
        mRAASR = new RAASR(RAASRAPIKEY);
        mRAASR.registerCallbacks(mRAASRCallbacks);
    }
    private String analysisResult(String orderResult){
        List<String> resultList = extractChineseCharacters(orderResult);
        StringBuilder sb = new StringBuilder();
        for (String str : resultList) {
            sb.append(str);
        }
        String result = sb.toString();
        Log.d(TAG,"analysisResult:"+result);
        return result;
    }
    private List<String> extractChineseCharacters(String jsonString) {
        List<String> chineseCharacters = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONArray latticeArray = object.getJSONArray("lattice");
            for (int i = 0; i < latticeArray.length(); i++) {
                JSONObject jsonObject = latticeArray.getJSONObject(i);
                String json1Best = jsonObject.getString("json_1best");
                JSONObject stObject = new JSONObject(json1Best).getJSONObject("st");
                JSONArray rtArray = stObject.getJSONArray("rt");
                for (int j = 0; j < rtArray.length(); j++) {
                    JSONArray wsArray = rtArray.getJSONObject(j).getJSONArray("ws");
                    for (int k = 0; k < wsArray.length(); k++) {
                        JSONArray cwArray = wsArray.getJSONObject(k).getJSONArray("cw");
                        for (int l = 0; l < cwArray.length(); l++) {
                            JSONObject cwObject = cwArray.getJSONObject(l);
                            String word = cwObject.getString("w");
                            chineseCharacters.add(word);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"extractChineseCharacters:"+e.toString());
        }
        return chineseCharacters;
    }
    private void OCR(File file){
        new Thread(new Runnable() {

            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), fileBody)
                        .addFormDataPart("language", "")
                        .addFormDataPart("pdf_page", "");

                Request request = new Request.Builder()
                        .url("https://eolink.o.apispace.com/ocrbase/ocr/v1/base_file")
                        .addHeader("X-APISpace-Token","gdc194gfbzv6kramvzdls4oulsyxab7q")
                        .post(builder.build())
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String result = response.body().string();
                    //TODO 处理识别结果
                    String result1 = processOCRResult(result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //在et_addNote中插入结果
                            String html = et_addNote.getHtml();
                            if (html == null){
                                html = "";
                            }
                            System.out.println(result1);
                            et_addNote.setHtml(html + result1);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }
    public String processOCRResult(String ocrResult) {
        try {
            JSONObject jsonObject = new JSONObject(ocrResult);
            JSONArray wordsResult = jsonObject.getJSONArray("words_result");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < wordsResult.length(); i++) {
                JSONObject wordObj = wordsResult.getJSONObject(i);
                String word = wordObj.getString("word");
                sb.append(word).append(" "); // 添加空格分隔每个单词
            }

            return sb.toString().trim(); // 去除末尾多余的空格
        } catch (JSONException e) {
            e.printStackTrace();
            return "Failed to process OCR result.";
        }
    }
}
