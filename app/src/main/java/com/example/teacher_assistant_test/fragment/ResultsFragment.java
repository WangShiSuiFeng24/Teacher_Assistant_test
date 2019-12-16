package com.example.teacher_assistant_test.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.activity.MainActivity;
import com.example.teacher_assistant_test.adapter.RecordAdapter;
import com.example.teacher_assistant_test.bean.Mark;
import com.example.teacher_assistant_test.bean.Record;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.ExportSheet;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.JsonParser;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.util.MyDividerItemDecoration;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.StrProcess;
import com.example.teacher_assistant_test.util.WrapContentLinearLayoutManager;
import com.github.clans.fab.FloatingActionButton;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.apache.log4j.chainsaw.Main;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.ToDoubleBiFunction;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import jxl.format.Colour;

/**
 * Created by Andong Ming on 2019.12.13.
 */
public class ResultsFragment extends Fragment implements FragmentBackHandler{

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 2;


    //打开一个test则为true，初始为false
    private boolean isOpenATest = false;

    //当前点击test_id
    private long current_test_id = -1;

    //当前点击test_name
    private String current_test_name;


    private LinearLayout record_title;
    private TextView stu_gender;
    private ImageView check_box;

    private Button save_to_db;
    private Button share_by_excel;


    //recordUI中编辑时弹出的"bottom_dialog"
    private LinearLayout my_collection_bottom_dialog;

    private TextView selectNum;
    private TextView selectAll;
    private Button btnDelete;


    //recordUI中子控件
    private boolean isIdSortPressed = false;
    private ImageView id_sort;
    private boolean isScoreSortPressed = false;
    private ImageView score_sort;


    //recordUI Visibility 不随需求改变的控件
    private RecyclerViewEmptySupport recordRecyclerView;
    private FloatingActionButton record_fab;

    private MyDividerItemDecoration recordDividerItemDecoration;

    private List<Record> recordList = new ArrayList<>();
    //备份，即使当前页面被编辑后，students始终保存刚进入页面时的数据
    private List<Record> backUpRecordList = new ArrayList<>();
    private RecordAdapter recordAdapter;

    //设置recordList更新默认为false
    private boolean isRecordListUpdate = false;

    //讯飞语音识别
    private RecognizerDialog mIatDialog = null;
    private LinkedHashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener mInitListener;
    private RecognizerDialogListener mRecognizerDialogListener;

    //设置编辑模式默认为RECORD_MODE_CHECK
    private static final int RECORD_MODE_CHECK = 0;
    private static final int RECORD_MODE_EDIT = 1;
    private int editMode = RECORD_MODE_CHECK;

    //默认全选状态为false,编辑状态为false，选中数量为0
    private boolean isSelectAll = false;
    private boolean editorStatus = false;
    private int index = 0;

    public ResultsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.record_mark, container, false);

        record_title = view.findViewById(R.id.record_title);
        stu_gender = view.findViewById(R.id.stu_gender);
        check_box = view.findViewById(R.id.check_box);

        save_to_db = view.findViewById(R.id.save_to_db);
        setSaveBtnBackground(isRecordListUpdate);
        share_by_excel = view.findViewById(R.id.share_by_excel);

        //绑定recordUI中编辑时弹出的"bottom_dialog"
        my_collection_bottom_dialog = view.findViewById(R.id.my_collection_bottom_dialog);

        selectNum = view.findViewById(R.id.tv_select_num);
        selectAll = view.findViewById(R.id.select_all);
        btnDelete = view.findViewById(R.id.btn_delete);

        //绑定子控件
        id_sort = view.findViewById(R.id.id_sort);
        score_sort = view.findViewById(R.id.score_sort);

        //绑定recordUI Visibility 不随需求改变的控件
        recordRecyclerView = view.findViewById(R.id.Recycler_View_Record);
        record_fab = view.findViewById(R.id.record_fab);


        //先设置item标题、保存按钮、分享按钮不可见
        record_title.setVisibility(View.GONE);
        check_box.setVisibility(View.GONE);

        save_to_db.setVisibility(View.GONE);
        share_by_excel.setVisibility(View.GONE);



        //设置recordRecyclerView的空View
        View emptyView = view.findViewById(R.id.empty_view);
        TextView emptyMessage = view.findViewById(R.id.empty_message);
        emptyMessage.setText(R.string.recordUI_empty_message);

        //设置分割线
        recordDividerItemDecoration = new MyDividerItemDecoration(getActivity(), MyDividerItemDecoration.VERTICAL, false);
        recordRecyclerView.addItemDecoration(recordDividerItemDecoration);

        WrapContentLinearLayoutManager record_linearLayoutManager = new WrapContentLinearLayoutManager(getActivity());
        recordRecyclerView.setLayoutManager(record_linearLayoutManager);
        recordRecyclerView.setEmptyView(emptyView);
        recordAdapter = new RecordAdapter(recordList);
        recordRecyclerView.setAdapter(recordAdapter);




        //设置recordUI控件监听
        setRecordUIWidgetListener();



        //获取Bundle
        isOpenATest = getArguments().getBoolean("isOpenATest");
        boolean isShowGender = getArguments().getBoolean("isShowGender");

        if (isOpenATest) {
            current_test_id = getArguments().getLong("current_test_id");
            current_test_name = getArguments().getString("current_test_name");

            initRecordList();


            mListener.notifyUpdateUI(current_test_name, null, 0);
        } else {
            record_fab.performClick();
        }

        if (recordList.size() != 0) {

//            titleBarView.setRightText(getString(R.string.edit));
//            titleBarView.setRightTextColor(Color.WHITE);

            mListener.notifyUpdateUI(null, getString(R.string.edit), Color.WHITE);
//
            record_title.setVisibility(View.VISIBLE);

            save_to_db.setVisibility(View.VISIBLE);
            share_by_excel.setVisibility(View.VISIBLE);
        }



        if (isShowGender) {
            stu_gender.setVisibility(View.VISIBLE);
        } else {
            stu_gender.setVisibility(View.GONE);
        }
        recordAdapter.setIsShowGender(isShowGender);




        mRecognizerDialogListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(this.getClass().getName(), "recognizer json result：" + results.getResultString());

                //处理讯飞语音听写返回的结果，将处理结果存入recordList,并更新adapter和相关控件的显示隐藏
                updateRecordListByRecognizer(results, isLast);


            }

            /**
             * 过滤掉没有说话的错误码显示
             */
            @Override
            public void onError(SpeechError error) {
                TextView tv_error = mIatDialog.getWindow().getDecorView().findViewWithTag("errtxt");
                if (tv_error != null) {
                    tv_error.setText(R.string.not_speak);
                }

//                View view = mIatDialog.getWindow().getDecorView().findViewWithTag("errview");
//                view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//                Toast.makeText(RecordMarkActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        };

        // 将“12345678”替换成您申请的 APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(getActivity(), SpeechConstant.APPID + "=5db04b35");

        return view;
    }

    private void setSaveBtnBackground(boolean isRecordListUpdate) {
        if (isRecordListUpdate) {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            save_to_db.setEnabled(true);
            save_to_db.setTextColor(Color.WHITE);
        } else {
//            save_to_db.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
            save_to_db.setEnabled(false);
            save_to_db.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_b7b8bd));
        }
    }

    /**
     *点击testItem,初始化recordList
     */
    private void initRecordList() {
        //先清空，在添加
        recordList.clear();
        backUpRecordList.clear();

        Log.i("MainActivity", "开始初始化Student。。。。。。");
        String sqlSelect="SELECT StudentMark.stu_id,Student.stu_name,Student.stu_gender,StudentMark.test_id,StudentTest.test_name,StudentMark.score,StudentMark.total_score,StudentMark.isCorrect "
                + "FROM StudentMark INNER JOIN Student ON StudentMark.stu_id = Student.stu_id "
                + "INNER JOIN StudentTest ON StudentMark.test_id = StudentTest.test_id";
        //扫描数据库，将信息放入markList
        SQLiteDatabase sd = MyDatabaseHelper.getInstance(getActivity());
        Cursor cursor=sd.rawQuery(sqlSelect,new String[]{});

        //打印cursor中的行数
        Log.i("MainActivity", "cursor.getCount()："+cursor.getCount());

        while(cursor.moveToNext()){
            long test_id = cursor.getInt(cursor.getColumnIndex("test_id"));
            Log.i("MainActivity", "数据库test_id："+test_id);

            //只有当查询出的条目的test_id等于传入的this.test_id时才将该条目add到List<Student>
            if(test_id == current_test_id) {
                String stu_id = String.valueOf(cursor.getInt(cursor.getColumnIndex("stu_id")));
                String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));
                String score = cursor.getString(cursor.getColumnIndex("score"));
                int total_score = cursor.getInt(cursor.getColumnIndex("total_score"));

                //读取SQLite studentMark表订正列
                boolean isCorrect = ((cursor.getInt(cursor.getColumnIndex("isCorrect")) == 1) ? true : false);

                Record record = new Record(stu_id, stu_name, stu_gender, test_name, score, total_score, isCorrect);

                //此处非常关键，若add同一个Record，则改变recordList中Record对象的成员方法或变量时，
                // backUpRecordList中的对应的Record对象的成员方法或变量也会随之改变
                //因为两个List中的Record在添加的时候就是同一个Record（地址一样）
                Record backUpRecord = new Record(stu_id, stu_name, stu_gender, test_name, score, total_score, isCorrect);

                recordList.add(record);
                backUpRecordList.add(backUpRecord);


//                Student student = new Student(stu_id, stu_name, stu_gender, test_name, score, total_score);
//                studentList.add(student);
//                backUpStudentList.add(student);
            }
        }
        cursor.close();
    }


    /**
     * 设置recordUI控件监听
     */
    private void setRecordUIWidgetListener() {
        //recordUI中点击和长按点击事件
        recordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //recordAdapter点击事件
                recordAdapterOnItemClick(position);
            }

            @Override
            public void onItemLongClick(int position) {
                //暂不处理
            }
        });

        //recordUI中学号id编辑框监听事件
        recordAdapter.setOnStuIdFillListener(new RecordAdapter.OnStuIdFillListener() {
            @Override
            public void onStuIdFill(int position, String stu_id) {
                //编辑学号监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {

                    if (TextUtils.isEmpty(recordList.get(position).getStu_id()) && TextUtils.isEmpty(stu_id)) {
                        return;
                    }

                    if (!TextUtils.isEmpty(recordList.get(position).getStu_id()) && !TextUtils.isEmpty(stu_id)
                            && recordList.get(position).getStu_id().equals(stu_id)) {
                        return;
                    }

                    recordList.get(position).setStu_id(stu_id);

                    //改 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);
                }
            }
        });

        //recordUI中成绩score编辑框监听事件
        recordAdapter.setOnScoreFillListener(new RecordAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(final int position, String score) {
                //编辑成绩监听
                //判断当前位置是否存在，因为删除item会触发文本改变事件afterTextChanged(Editable s)
                if(position < recordList.size()) {

                    if (TextUtils.isEmpty(recordList.get(position).getScore()) && TextUtils.isEmpty(score)) {
                        return;
                    }

                    if (!TextUtils.isEmpty(recordList.get(position).getScore()) && !TextUtils.isEmpty(score)
                            && recordList.get(position).getScore().equals(score)) {
                        return;
                    }

                    //改 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);

                    recordList.get(position).setScore(score);

                    if(!TextUtils.isEmpty(score)) {
//                        recordList.get(position).setScore(score);
                        //先要判断编辑的score字符串是否符合规则，是则计算total_score,否则不计算
                        //增加判断score中是否存在空格，若存在则不计算，不存在则计算total_score，否则会发生异常
                        if(!score.contains(" ") && new CheckExpression().checkExpression(score)) {
//                        score = score.replaceAll(" ","");
                            int total_score = (int) new Calculator().calculate(score);//score中有空格会发生异常
                            recordList.get(position).setTotal_score(total_score);

                            if(!recordRecyclerView.isComputingLayout()) {
                                recordAdapter.notifyDataSetChanged();
                            }

                        }
                    } else {
                        //score编辑框为空，则设置total_score为0
                        recordList.get(position).setTotal_score(0);
                        if(!recordRecyclerView.isComputingLayout()) {
                            recordAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        recordAdapter.setOnStarClickListener(new RecordAdapter.OnStarClickListener() {
            @Override
            public void onStarClick(int position) {
                recordAdapterOnStarClick(position);
            }
        });

        recordAdapter.setOnFooterClickListener(new RecordAdapter.OnFooterClickListener() {
            @Override
            public void onFooterClick(int position) {
                recordAdapterOnFooterClick(position);
            }
        });


        //recordUI中的fab点击事件
        record_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecognizerDialog();
            }
        });

        //保存当前页面数据到数据库
        save_to_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveDataToDatabase();
            }
        });

        //分享当前页面
        share_by_excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareExcel();
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAllItem();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecordItem();
            }
        });
    }

    /**
     * recordAdapter点击事件
     * @param position 当前item位置
     */
    private void recordAdapterOnItemClick(int position) {
        if (editorStatus) {
            Record record = recordList.get(position);
            boolean isSelect = record.isSelect();
            if (!isSelect) {
                index++;
                record.setSelect(true);
                if (index == recordList.size()) {
                    isSelectAll = true;
                    selectAll.setText(R.string.cancel_select_all);
                }
            } else {
                index--;
                record.setSelect(false);
                isSelectAll = false;
                selectAll.setText(R.string.select_all);
            }
            setDeleteBtnBackground(index);
            selectNum.setText(String.valueOf(index));
            recordAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 订正列点亮事件
     * @param position 点击位置
     */
    private void recordAdapterOnStarClick(int position) {
        Record record = recordList.get(position);
        boolean isCorrect = record.isCorrect();
        if (!isCorrect) {
            record.setCorrect(true);
            Toast.makeText(getActivity(), R.string.revision_completed, Toast.LENGTH_SHORT).show();
        } else {
            record.setCorrect(false);
            Toast.makeText(getActivity(), R.string.revision_uncompleted, Toast.LENGTH_SHORT).show();
        }

        isRecordListUpdate = true;

        setSaveBtnBackground(true);
        recordAdapter.notifyDataSetChanged();
    }

    /**
     *  每个成绩表页脚增加一个“统计信息”入口，点击后打开统计信息表，内容为：
     *     - 总分：xxx（分值要可以编辑）
     *     - 成绩尚缺：（列出姓名）
     *     - 订正尚缺：（列出姓名）
     * @param position 点击位置
     */
    private void recordAdapterOnFooterClick(int position) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.statistical_info_dialog, null, false);

        EditText test_full_mark_edit = view.findViewById(R.id.test_full_mark_edit);

        LinearLayout linearLayout = view.findViewById(R.id.ll_edit_parent);

        linearLayout.setFocusableInTouchMode(true);


        TextView lack_score_name = view.findViewById(R.id.lack_score_name);
        TextView lack_correct_name = view.findViewById(R.id.lack_correct_name);

        AlertDialog alertDialog = GetAlertDialog.getAlertDialog(getActivity(), "统计信息",
                null, view, getString(R.string.confirm), getString(R.string.cancel));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                //恢复统计信息编辑总分按钮,编辑保存状态flag默认值0
//                flag = 0;

                String input = test_full_mark_edit.getText().toString();

                if (!TextUtils.isEmpty(input)) {
                    int test_full_mark = Integer.parseInt(input);
                    updateTestFullMarkByTestId(current_test_id, test_full_mark);
                }

                alertDialog.dismiss();
            }
        });


        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());

        //根据current_test_id查询对应test_full_mark
        String sqlSelectTestFullMark = "select StudentTest.test_full_mark "
                + "from StudentTest where test_id = " + current_test_id;

        Cursor cursor0 = db.rawQuery(sqlSelectTestFullMark, new String[]{});

        while (cursor0.moveToNext()) {
            int test_full_mark = cursor0.getInt(cursor0.getColumnIndex("test_full_mark"));
            test_full_mark_edit.setText(String.valueOf(test_full_mark));

            test_full_mark_edit.setSelection(test_full_mark_edit.getText().length());

//            test_full_mark_edit.clearFocus();


        }
        cursor0.close();


        //先查询Student表中所有学生的学号，对比去掉当前页面的学号，返回剩下学号对应的姓名
        String sqlSelectStuIdAndStuName = "select Student.stu_id, Student.stu_name "
                + "from Student";

        Cursor cursor = db.rawQuery(sqlSelectStuIdAndStuName, new String[]{});

        SparseArray<String> sparseArray = new SparseArray<>();

        while (cursor.moveToNext()) {
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));

            sparseArray.put(stu_id, stu_name);
        }
        cursor.close();

        for (int i=sparseArray.size()-1; i>=0; i--) {
            for (int j=0; j<recordList.size(); j++) {
                Record record = recordList.get(j);
                if (sparseArray.keyAt(i) == Integer.parseInt(record.getStu_id())) {
                    sparseArray.removeAt(i);
                    break;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<sparseArray.size(); i++) {
            String name = sparseArray.valueAt(i);
            stringBuilder.append(name);
            if (i != sparseArray.size()-1) {
                stringBuilder.append("、");
            }
        }

        for (int i=0; i<recordList.size(); i++) {
            Record record = recordList.get(i);
            if (TextUtils.isEmpty(record.getScore())) {
                stringBuilder.append(record.getStu_name());
                stringBuilder.append("、");
            }
        }
        //删除最后一个顿号
        stringBuilder.deleteCharAt(stringBuilder.length()-1);


        lack_score_name.setText(stringBuilder.toString());

        //订正尚缺则只显示当前页面isCorrect为false的学生姓名
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int i=0; i<recordList.size(); i++) {
            Record record = recordList.get(i);
            if (!record.isCorrect()) {
                stringBuilder1.append(record.getStu_name());
                if (i != recordList.size()-1) {
                    stringBuilder1.append("、");
                }
            }
        }

        lack_correct_name.setText(stringBuilder1.toString());

    }

    /**
     * 显示讯飞语音听写的dialog，并设置相关参数
     */
    private void showRecognizerDialog() {
        //初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        //使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(getActivity(), mInitListener);

//                if(mIatDialog!=null)
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "1500");
        mIatDialog.setParameter("dwa", "wpgs");

        mIatDialog.setListener(mRecognizerDialogListener);

        mIatDialog.show();
        TextView txt = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText(R.string.tip);//更改内容
        txt.setTextColor(Color.WHITE);
        txt.getPaint().setFlags(Paint.SUBPIXEL_TEXT_FLAG);//去下划线

        TextView title = mIatDialog.getWindow().getDecorView().findViewWithTag("title");
        title.setTextColor(Color.WHITE);
    }

    /**
     * 保存当前页面数据到数据库
     */
    private void saveDataToDatabase() {

        final SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());

        if (checkRecordListLegality()) {

            //通过testItem点击进入recordUI
            if(isOpenATest) {
                //先删除数据库同backupRecordList中学号的学生，再将recordList直接插入数据库
                for (int i = 0; i < backUpRecordList.size(); i++) {
                    String delete_stu_id = backUpRecordList.get(i).getStu_id();
                    db.delete("StudentMark", "stu_id = ? AND test_id = ?", new String[]{"" + delete_stu_id + "", ""+current_test_id+""});
                }//先删
                for (Record record : recordList) {
                    String stu_id = record.getStu_id();

                    String score = record.getScore();
                    int total_score = record.getTotal_score();

                    boolean isCorrect = record.isCorrect();

                    new IDUSTool(getActivity()).insertStuMarkDB(stu_id, current_test_id, score, total_score, isCorrect);
                }//再插入
                db.close();
                Toast.makeText(getActivity(), R.string.save_successfully, Toast.LENGTH_SHORT).show();

                isRecordListUpdate = false;
                setSaveBtnBackground(false);

                return;
            }

            //非点击testItem，新建模式的保存方式
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_test_name_and_test_full_mark_view, null, false);

            EditText test_name_edit = view.findViewById(R.id.test_name_edit);
            EditText test_full_mark_edit = view.findViewById(R.id.test_full_mark_edit);

            final AlertDialog alertDialog = GetAlertDialog
                    .getAlertDialog(getActivity(),getString(R.string.please_enter_the_test_name),
                            null, view, getString(R.string.confirm), getString(R.string.cancel));

            test_name_edit.setFocusable(true);
            test_name_edit.setFocusableInTouchMode(true);
            test_name_edit.requestFocus();

            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                public void run()
                {
                    InputMethodManager inputManager =(InputMethodManager)test_name_edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(test_name_edit, 0);
                }
            },300);

            //拿到按钮并判断是否是POSITIVE_BUTTON，然后我们自己实现监听
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = test_name_edit.getText().toString().trim();
                    //生成唯一id
//                                final long unique_test_id = new Date().getTime();//太长
                    //每次先查询StudentTest表长getCount，将unique_test_id设置为表长+1
//                            final long unique_test_id = getCount() + 1;

                    final long unique_test_id = getTest_idMax() + 1;

                    Log.i("RecordMarkActivity", "unique_test_id:"+unique_test_id);
                    if (input.equals("")) {
                        Toast.makeText(getActivity(), getString(R.string.test_name_cannot_be_empty) + input, Toast.LENGTH_SHORT).show();
                        return;
                    } else {
//                            String editText = edit.getText().toString().trim();

                        //将用户输入的Test_Name,unique_test_id和当前页面数据一起保存到数据库中

                        //查询数据库中是否存在与将要插入的preMark相同的test_name,若相同，则提示用户test_name已存在重新输入，否则直接新建插入全部数据
                        String sqlSelect = "SELECT test_name FROM StudentTest";
                        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

                        if(cursor.isLast()) {
                            //StudentTest表为空，第一次更新
                            new IDUSTool(getActivity()).insertStuTest(unique_test_id, input);

                            //将当前全局test_id设置为已保存的unique_test_id
                            current_test_id = unique_test_id;

                            //保存总分
                            if (!TextUtils.isEmpty(input = test_full_mark_edit.getText().toString())) {
                                int test_full_mark = Integer.parseInt(input);
                                updateTestFullMarkByTestId(unique_test_id, test_full_mark);
                            }

                            Toast.makeText(getActivity(), R.string.save_successfully, Toast.LENGTH_SHORT).show();
                        }

                        else {
                            //StudentTest表不为空
//                                //flag初始设为false,代表StudentTest表中有没相同的test_name
//                                boolean flag = false;
                            while(cursor.moveToNext()) {
                                String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

                                if(input.equals(test_name)) {
                                    //StudentTest表中已有相同test_name,提示用户重新输入
                                    Toast.makeText(getActivity(), input+" 已存在，请重新输入", Toast.LENGTH_SHORT).show();
//                                        flag = true;
                                    return;
                                }
                            }

                            //循环检查完毕，此时没有相同的test_name,直接向StudentTest表中插入所有数据，不用判断

                            //先把test_id和test_name插入到StudentTest表中
                            new IDUSTool(getActivity()).insertStuTest(unique_test_id, input);
                            Log.i("RecordMarkActivity", "向StudentTest表中插入unique_test_id:"+unique_test_id+"，input:"+input+"成功");

                            //再向StudentMark表中插入当前页面数据
                            Iterator<Record> iterator = recordList.iterator();
                            while(iterator.hasNext()) {
                                //先用preMark保存当前页面mark条目
                                final Record preRecord = iterator.next();

                                String stu_id = preRecord.getStu_id();
//                                    long test_id = unique_test_id;
                                String score = preRecord.getScore();
                                int total_score = preRecord.getTotal_score();

                                boolean isCorrect = preRecord.isCorrect();

                                new IDUSTool(getActivity()).insertStuMarkDB(stu_id, unique_test_id, score, total_score, isCorrect);
//                                                Toast.makeText(RecordMarkActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                Log.i("RecordMarkActivity", "向StudentMark表中插入stu_id："+stu_id
                                        +"\r\nunique_test_id："+unique_test_id
                                        +"\r\nscore："+score
                                        +"\r\ntotal_score"+total_score+"成功");

                                //将当前全局test_id设置为已保存的unique_test_id
                                current_test_id = unique_test_id;

                            }
//                                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();

                        //保存总分
                        if (!TextUtils.isEmpty(test_full_mark_edit.getText().toString())) {
                            int test_full_mark = Integer.parseInt(test_full_mark_edit.getText().toString());
                            updateTestFullMarkByTestId(unique_test_id, test_full_mark);
                        }

                        Toast.makeText(getActivity(), R.string.save_successfully, Toast.LENGTH_SHORT).show();
//                        titleBarView.setTitle(input);


                        mListener.notifyUpdateUI(input, null, 0);

                        isOpenATest = true;

                        isRecordListUpdate = false;
                        setSaveBtnBackground(false);

                        //让AlertDialog消失
                        alertDialog.cancel();
                    }
                }
            });
        }
    }

    /**
     * 分享当前页面
     */
    private void shareExcel() {

        //先判断权限是否授予，没有则单独申请，有则继续
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
        } else {
            if (checkRecordListLegality()) {
                //生成唯一id
//          final long unique_test_id = new Date().getTime();//太长
                //每次先查询StudentTest表长getCount，将unique_test_id设置为表长+1
//          final long unique_test_id = getCount() + 1;
//                final long unique_test_id = getTest_idMax() + 1;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String the_only_test_name = dateFormat.format(Calendar.getInstance().getTime());

                if (isOpenATest) {
                    the_only_test_name = current_test_name;
                }

                //导出Excel
                ExportSheet exportSheet = new ExportSheet(getActivity(), the_only_test_name, recordList);

                exportSheet.exportSheet();

                try {
                    File file = new File(exportSheet.getFileName());

                    Uri shareFileUri = FileUtil.getFileUri(getActivity(), ShareContentType.FILE, file);

                    new Share2.Builder(getActivity())
                            //指定分享的文件类型
                            .setContentType(ShareContentType.FILE)
                            //设置要分享的文件Uri
                            .setShareFileUri(shareFileUri)
                            //设置分享选择器的标题
                            .setTitle(getString(R.string.share_file))
                            .build()
                            //发起分享
                            .shareBySystem();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 全选和反选按钮逻辑
     */
    private void selectAllItem() {
        if(recordAdapter == null ) return;
        if(!isSelectAll) {
            for (int i = 0, j = recordList.size(); i < j; i++) {
                recordList.get(i).setSelect(true);
            }
            index = recordList.size();
            btnDelete.setEnabled(true);
            selectAll.setText(R.string.cancel_select_all);
            isSelectAll = true;
        } else {
            for (int i=0, j = recordList.size(); i < j; i++) {
                recordList.get(i).setSelect(false);
            }
            index = 0;
            btnDelete.setEnabled(false);
            selectAll.setText(R.string.select_all);
            isSelectAll = false;
        }
        recordAdapter.notifyDataSetChanged();
        setDeleteBtnBackground(index);
        selectNum.setText(String.valueOf(index));
    }

    /**
     * 删除按钮逻辑
     */
    private void deleteRecordItem() {
        if (index == 0) {
            btnDelete.setEnabled(false);
            return;
        }
        final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(getActivity(), getString(R.string.alarm),
                getString(R.string.delete_one_item_hint), null,
                getString(R.string.confirm), getString(R.string.cancel));

        if (index == 1) {
            alertDialog.setMessage(getString(R.string.delete_one_item_hint));
        } else {
            alertDialog.setMessage(getString(R.string.delete_multiple_item_hint, index));
        }

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = recordList.size(), j = 0; i > j; i--) {
                    Record record = recordList.get(i-1);
                    if (record.isSelect()) {

                        recordList.remove(record);
                        index--;
                    }
                }

                //删 更新
                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                index = 0;
                selectNum.setText(String.valueOf(0));
                setDeleteBtnBackground(index);
                if (recordList.size() == 0) {
                    updateEditMode();

                    my_collection_bottom_dialog.setVisibility(View.GONE);

                    if (isOpenATest) {

                        save_to_db.setVisibility(View.VISIBLE);

                        share_by_excel.setVisibility(View.VISIBLE);

                        record_title.setVisibility(View.GONE);

                        record_fab.setVisibility(View.VISIBLE);

                    } else {

                        save_to_db.setVisibility(View.GONE);

                        share_by_excel.setVisibility(View.GONE);

                        record_title.setVisibility(View.GONE);

                        record_fab.setVisibility(View.VISIBLE);

//                        titleBarView.setRightText("");
                        mListener.notifyUpdateUI(null, "", 0);
                    }
                }
                recordAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

    }

    /**
     * 编辑和取消按钮互转
     */
    public void updateEditMode() {
        editMode = editMode == RECORD_MODE_CHECK ? RECORD_MODE_EDIT : RECORD_MODE_CHECK;
        if (editMode == RECORD_MODE_EDIT) {
//            titleBarView.setRightText(getString(R.string.cancel));
            mListener.notifyUpdateUI(null, getString(R.string.cancel), 0);

            my_collection_bottom_dialog.setVisibility(View.VISIBLE);

            check_box.setVisibility(View.VISIBLE);

            save_to_db.setVisibility(View.GONE);
//            clear_data.setVisibility(View.GONE);
            share_by_excel.setVisibility(View.GONE);
            record_fab.setVisibility(View.GONE);

            editorStatus = true;
        } else {
//            titleBarView.setRightText(getString(R.string.edit));
            mListener.notifyUpdateUI(null, getString(R.string.edit), 0);

            my_collection_bottom_dialog.setVisibility(View.GONE);

            check_box.setVisibility(View.GONE);

            save_to_db.setVisibility(View.VISIBLE);
//            clear_data.setVisibility(View.VISIBLE);
            share_by_excel.setVisibility(View.VISIBLE);
            record_fab.setVisibility(View.VISIBLE);

            editorStatus = false;
            clearAll();
        }
        recordAdapter.setEditMode(editMode);
    }

    /**
     * 恢复（初始）默认设置
     */
    private void clearAll() {
        selectNum.setText(String.valueOf(0));
        isSelectAll = false;
        selectAll.setText(R.string.select_all);
        setDeleteBtnBackground(0);
    }


    /**
     * 根据选择的数量是否为0来判断按钮的是否可点击
     * @param size 选择的数量
     */
    private void setDeleteBtnBackground(int size) {
        if(size != 0) {
            btnDelete.setBackgroundResource(R.drawable.button_shape);
            btnDelete.setEnabled(true);
            btnDelete.setTextColor(Color.WHITE);
        } else {
            btnDelete.setBackgroundResource(R.drawable.button_unclickable_shape);
            btnDelete.setEnabled(false);
            btnDelete.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_b7b8bd));
        }
    }

    /**
     * 更新总分test_full_mark通过测试号test_id
     * @param test_id 测试号
     * @param test_full_mark 总分
     */
    private void updateTestFullMarkByTestId(long test_id, int test_full_mark) {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());

        ContentValues values = new ContentValues();

        values.put("test_full_mark", test_full_mark);

        db.update("StudentTest", values, "test_id = ?", new String[] {"" + test_id + ""});
    }

    /**
     * 检查当前页面数据id,score合法性
     * @return 合法返回true,不合法返回false
     */
    private boolean checkRecordListLegality() {
//        if (recordList.size() == 0) {
//            Toast.makeText(MainActivity.this, "数据为空，保存失败", Toast.LENGTH_LONG).show();
//            return false;
//        } else {
        //检查recordList中是否有非法stu_id或非法score
        boolean isStu_idAndScoreLegal = true;
        for(Record record : recordList) {

            //先判断学号是否为空，若为空则直接返回false
            if (TextUtils.isEmpty(record.getStu_id())) {
                isStu_idAndScoreLegal = false;
                Toast.makeText(getActivity(), R.string.stu_id_can_not_be_null, Toast.LENGTH_SHORT).show();
                break;
            }

            if (!canParseInt(record.getStu_id())) {
                isStu_idAndScoreLegal = false;
                Toast.makeText(getActivity(), getString(R.string.illegal_stu_id_hint), Toast.LENGTH_SHORT).show();
                break;
            }

            //设置为空时score也合法
            if (TextUtils.isEmpty(record.getScore())) {
                continue;
            }

            //判断表达式中是否含有空格
            if (record.getScore().contains(" ")) {
                isStu_idAndScoreLegal = false;
                Toast.makeText(getActivity(), getString(R.string.score_has_blank_space_hint, record.getStu_id(), record.getScore()), Toast.LENGTH_SHORT).show();
                break;
            }
            //判断表达式是否为算术表达式，但不能判断是否存在非法空格
            if (!(new CheckExpression().checkExpression(record.getScore()))) {
                isStu_idAndScoreLegal = false;
                Toast.makeText(getActivity(), getString(R.string.illegal_score_hint, record.getStu_id(), record.getScore()), Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if(!isStu_idAndScoreLegal) {
            return false;
        } else {
            //检查markList中是否有重复stu_id
            boolean hasSameStu_id = false;
            List<Record> checkList = new ArrayList<>();

            //list.contains(o)，系统会对list中的每个元素e调用o.equals(e)方法，假如list中有n个元素，
            // 那么会调用n次o.equals(e)，只要有一次o.equals(e)返回了true，那么list.contains(o)返回true，
            // 否则返回false。
            for (Record record : recordList) {
                if (checkList.contains(record)) {
                    Toast.makeText(getActivity(), getString(R.string.same_stu_id_hint, record.getStu_id()), Toast.LENGTH_SHORT).show();
                    hasSameStu_id = true;
                    break;
                }
                checkList.add(record);
            }
            return !hasSameStu_id;
        }
//        }
    }

    /**
     * SQLite查询test_id最大值
     * @return 数据库中test_id最大值
     */
    private long getTest_idMax() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());
        Cursor cursor = db.rawQuery("select max(test_id) from StudentTest", null);
        cursor.moveToNext();
        Long maxTest_id = cursor.getLong(0);
        cursor.close();
        return maxTest_id;
    }

    /**
     * 使用正则表达式判断该字符串是否为数字，第一个\是转义符，\d+表示匹配1个或 //多个连续数字，"+"和"*"类似，"*"表示0个或多个
     * @param string 待验证字符串
     * @return 是数字则返回true，否则返回false
     */
    public boolean canParseInt(String string){
        if(string == null){ //验证是否为空
            return false;
        }
        return string.matches("\\d+");
    }

    /**
     * xml中设置的监听，对id排序
     * @param v xml中view
     */
    public void idSortControl(View v) {
        if(!isIdSortPressed) {
            isIdSortPressed = true;
            id_sort.setImageResource(R.drawable.ic_drop_up);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        //是“比较o1和o2的大小”。
                        // 返回“负数”，意味着“o1比o2小”
                        // 返回“零”，意味着“o1等于o2”
                        // 返回“正数”，意味着“o1大于o2”
                        //需要先判断o1.getStu_id()和o2.getStu_id()的String类型能否转成int，否则会造成异常
                        boolean o1Can = canParseInt(o1.getStu_id());
                        boolean o2Can = canParseInt(o2.getStu_id());

                        if(o1Can) {
                            if(o2Can) {
                                //都有值,升序
                                return Integer.parseInt(o1.getStu_id()) - Integer.parseInt(o2.getStu_id());
                            } else {
                                //升序，定义非法的小
                                return 1;
                            }
                        } else if (o2Can) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });

                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                recordAdapter.notifyDataSetChanged();
            }
        } else {
            isIdSortPressed = false;
            id_sort.setImageResource(R.drawable.ic_drop_down);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        boolean o1Can = canParseInt(o1.getStu_id());
                        boolean o2Can = canParseInt(o2.getStu_id());

                        if(o1Can) {
                            if(o2Can) {
                                //都有值,降序
                                return Integer.parseInt(o2.getStu_id()) - Integer.parseInt(o1.getStu_id()) ;
                            } else {
                                //降序，定义非法的小
                                return -1;
                            }
                        } else if (o2Can) {
                            return 1;
                        } else {
                            return 0;
                        }

                    }
                });

                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                recordAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * xml中设置的监听，对score排序
     * @param v xml中view
     */
    public void scoreSortControl(View v) {
        if(!isScoreSortPressed) {
            isScoreSortPressed = true;
            score_sort.setImageResource(R.drawable.ic_drop_up);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o1.getTotal_score() - o2.getTotal_score();
                    }
                });

                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                recordAdapter.notifyDataSetChanged();
            }
        } else {
            isScoreSortPressed = false;
            score_sort.setImageResource(R.drawable.ic_drop_down);
            if(recordList.size() != 0) {
                Collections.sort(recordList, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return  o2.getTotal_score() - o1.getTotal_score();
                    }
                });

                isRecordListUpdate = true;
                setSaveBtnBackground(true);

                recordAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 处理讯飞语音听写返回的结果，将处理结果存入recordList,并更新adapter和相关控件的显示隐藏
     * @param results 讯飞语音听写返回的结果
     * @param isLast 是否结束
     */
    private void updateRecordListByRecognizer(RecognizerResult results, boolean isLast) {
        //读取动态修正返回结果
        String resultStr = updateResult(results);
        if(!isLast) return;
        Log.d("MainActivity", "recognizer result：" + resultStr);

        //处理resultStr,将处理后的结果markList中，并刷新页面，newMarkList中为将语音识别结果处理后的字符串
        List<Mark> newMarkList = StrProcess.StrToMarkList(resultStr);

        if(newMarkList == null) {
            //处理结果为null，无有效成绩
            final AlertDialog alertDialog = GetAlertDialog
                    .getAlertDialog(getActivity(), getString(R.string.hint),
                            getString(R.string.no_valid_mark_hint, resultStr),
                            null, getString(R.string.confirm), getString(R.string.cancel));
            alertDialog.setCanceledOnTouchOutside(false);

        } else {
            //不为空，先显示数据，可编辑修改，后由用户选择是否保存数据到数据库中
            //遍历newMarkList，将其添加到markList

            final Iterator<Mark> iteratorNew = newMarkList.iterator();
            while(iteratorNew.hasNext()) {
                final Mark newMark = iteratorNew.next();


                //先根据学号stu_id查出姓名stu_name和stu_gender
                SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());
                Cursor cursor = db.query("Student", new String[]{"stu_name", "stu_gender"}, "stu_id = ?",
                        new String[]{""+newMark.getStu_id()+""}, null, null, null);
                String stu_name = null;
                String stu_gender = null;
                while (cursor.moveToNext()) {
                    stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
                    stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));
                }
                cursor.close();

                final Record newRecord = new Record();
                newRecord.setStu_id(newMark.getStu_id());
                newRecord.setScore(newMark.getScore());
                newRecord.setTotal_score(newMark.getTotal_score());
                newRecord.setStu_name(stu_name);
                newRecord.setStu_gender(stu_gender);


                if(recordList.size() == 0) {
                    //第一次添加
                    recordList.add(newRecord);
                    recordAdapter.notifyDataSetChanged();

//                    titleBarView.setRightText(getString(R.string.edit));
//                    titleBarView.setRightTextColor(Color.WHITE);

                    mListener.notifyUpdateUI(null, getString(R.string.edit), Color.WHITE);

                    save_to_db.setVisibility(View.VISIBLE);
//                                clear_data.setVisibility(View.VISIBLE);
                    share_by_excel.setVisibility(View.VISIBLE);


                    record_title.setVisibility(View.VISIBLE);

                    //增 更新
                    isRecordListUpdate = true;
                    setSaveBtnBackground(true);

                } else {
                    //flag初始设为false,代表学号不相同
                    boolean flag = false;
                    for(int i=0; i<recordList.size(); i++) {
                        final Record record = recordList.get(i);
                        if(newRecord.getStu_id().equals(record.getStu_id())) {

                            flag = true;
                            //增加判断若有学号但是成绩为空的情况
                            if (record.getScore() == null) {
                                record.setScore(newRecord.getScore());
                                record.setTotal_score(newRecord.getTotal_score());

                                //改 更新
                                isRecordListUpdate = true;
                                setSaveBtnBackground(true);

                                recordAdapter.notifyDataSetChanged();
                                break;
                            }

                            //语音识别stu_id相同，这里处理
                            //弹出dialog,是否更改数据，
                            flag = true;
                            final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(getActivity(),
                                    getString(R.string.alarm),
                                    getString(R.string.recordUI_record_same_stu_id_hint, newMark.getStu_id(), record.getScore(), newRecord.getScore()),
                                    null, getString(R.string.confirm), getString(R.string.cancel));
                            alertDialog.setCanceledOnTouchOutside(false);

                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //学号相同，更改成绩
                                    record.setScore(newRecord.getScore());
                                    record.setTotal_score(newRecord.getTotal_score());

                                    //改 更新
                                    isRecordListUpdate = true;
                                    setSaveBtnBackground(true);

                                    recordAdapter.notifyDataSetChanged();
                                    alertDialog.dismiss();
                                }
                            });
                            break;
                        }
                    }

                    if(!flag) {
                        //语音识别stu_id没有一个相同，这里处理
                        recordList.add(newRecord);

                        //增 更新
                        isRecordListUpdate = true;
                        setSaveBtnBackground(true);

                        recordAdapter.notifyDataSetChanged();

//                        titleBarView.setRightText(getString(R.string.edit));
//                        titleBarView.setRightTextColor(Color.WHITE);
                        mListener.notifyUpdateUI(null, getString(R.string.edit), Color.WHITE);


                        save_to_db.setVisibility(View.VISIBLE);
//                                    clear_data.setVisibility(View.VISIBLE);
                        share_by_excel.setVisibility(View.VISIBLE);

                        record_title.setVisibility(View.VISIBLE);

                    }
                }

            }
            //清空错误缓存
            mIatResults.clear();
        }
    }

    /**
     * 读取动态修正返回结果
     * @param results 讯飞语音识别结果
     * @return 返回修正后的结果
     */
    private String updateResult(RecognizerResult results) {

        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d(getActivity().getLocalClassName(), "parseIatResult：" + text);

        String sn = null;
        String pgs = null;
        String rg = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
            pgs = resultJson.optString("pgs");
            rg = resultJson.optString("rg");
            Log.d(this.getClass().getName(), "sn："+sn+"\r\npgs："+pgs+"\r\nrg："+rg);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        //如果pgs是rpl就在已有的结果中删除掉要覆盖的sn部分
        if (pgs.equals("rpl")) {
            Log.d(this.getClass().getName(), "recognizer result replace：" + results.getResultString());
//
            String[] strings = rg.replace("[", "").replace("]", "").split(",");
            int begin = Integer.parseInt(strings[0]);
            int end = Integer.parseInt(strings[1]);
            for (int i = begin; i <= end; i++) {
                mIatResults.remove(i+"");
                Log.d(this.getClass().getName(), "修正mIatResults："+mIatResults.toString());
            }
        }

        mIatResults.put(sn, text);
        Log.d(this.getClass().getName(), "mIatResults"+mIatResults.toString());
        StringBuffer resultBuffer = new StringBuffer();

        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
        //mResultText.setText(resultBuffer.toString())
        //mResultText.setSelection(mResultText.length())
    }


    public ResultsFragment.ResultsListener mListener;

    public interface ResultsListener {
        void notifyUpdateUI(String title, String rightText, int rightTextColor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mListener = (ResultsFragment.ResultsListener) context;
        }
    }

    @Override
    public boolean onBackPressed() {

        if (editorStatus) {

            editorStatus = false;
            updateEditMode();

            //收回my_collection_bottom_dialog
            my_collection_bottom_dialog.setVisibility(View.GONE);

            //显示底部三按钮
            save_to_db.setVisibility(View.VISIBLE);
            setSaveBtnBackground(isRecordListUpdate);

            share_by_excel.setVisibility(View.VISIBLE);

            record_fab.setVisibility(View.VISIBLE);

            return true;
        }

        if (isRecordListUpdate) {

            final AlertDialog alertDialog = GetAlertDialog.getAlertDialog(getActivity(), getString(R.string.save),
                    getString(R.string.recordUI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    save_to_db.performClick();

                    if (!isRecordListUpdate) {
                        //结束当前fragment
                        if (getFragmentManager().getBackStackEntryCount() > 0) {
                            getFragmentManager().popBackStack();
                            // TODO: 2019-12-13 这里需要思考
                            mListener.notifyUpdateUI(null, null, 0);
                        }
                    }

                    alertDialog.dismiss();

                }
            });

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //结束当前fragment
                    if (getFragmentManager().getBackStackEntryCount() > 0) {
                        getFragmentManager().popBackStack();
                        // TODO: 2019-12-13 这里需要思考
                        mListener.notifyUpdateUI(null, null, 0);
                    }
                    alertDialog.dismiss();
                }
            });

            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertDialog.dismiss();
                }
            });
            return true;
        } else {
            //结束当前fragment
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                mListener.notifyUpdateUI(null, null, 0);
            }
            return true;
        }
    }

    public List<Record> getRecordList() {
        return recordList;
    }
}
