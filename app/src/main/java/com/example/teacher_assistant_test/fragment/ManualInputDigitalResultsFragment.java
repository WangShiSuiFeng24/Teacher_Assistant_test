package com.example.teacher_assistant_test.fragment;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.activity.MainActivity;
import com.example.teacher_assistant_test.adapter.ResultAdapter;
import com.example.teacher_assistant_test.bean.Result;
import com.example.teacher_assistant_test.util.Calculator;
import com.example.teacher_assistant_test.util.CheckExpression;
import com.example.teacher_assistant_test.util.ExportSheet;
import com.example.teacher_assistant_test.util.GetAlertDialog;
import com.example.teacher_assistant_test.util.IDUSTool;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.util.MyDividerItemDecoration;
import com.example.teacher_assistant_test.util.RecyclerViewEmptySupport;
import com.example.teacher_assistant_test.util.ToastHelper;
import com.example.teacher_assistant_test.util.WrapContentLinearLayoutManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualInputDigitalResultsFragment extends Fragment implements  FragmentBackHandler{

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 2;

    private List<Result> resultList = new ArrayList<>();
    private List<String> backUpStuId = new ArrayList<>();

    private RecyclerViewEmptySupport recyclerView;
    private ResultAdapter resultAdapter;

    private long current_test_id = -1;

    private String current_test_name;

    private boolean isResultListUpdate = false;

    private Button save_to_db;
    private Button share_by_excel;


    public ManualInputDigitalResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manual_input_results, container, false);

        save_to_db = view.findViewById(R.id.save_to_db);
        share_by_excel = view.findViewById(R.id.share_by_excel);
        setSaveBtnBackground(isResultListUpdate);

        initResultList();

        recyclerView = view.findViewById(R.id.Recycler_View_Result);

        //设置RecyclerView空布局
        View emptyView = view.findViewById(R.id.empty_view);
        TextView emptyMessage = view.findViewById(R.id.empty_message);
        emptyMessage.setText("暂无任何学生数据，请到基本学生信息页面添加学生信息");

        recyclerView.setEmptyView(emptyView);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);

        resultAdapter = new ResultAdapter(resultList);

        recyclerView.setAdapter(resultAdapter);

        //设置是否显示性别列，根据传入的isShowGender
        boolean isShowGender = getArguments().getBoolean("isShowGender");
        if (isShowGender) {
            view.findViewById(R.id.stu_gender).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.stu_gender).setVisibility(View.GONE);
        }
        resultAdapter.setIsShowGender(isShowGender);

        //设置自定义去底部分割线
        MyDividerItemDecoration myDividerItemDecoration = new MyDividerItemDecoration(getActivity(), MyDividerItemDecoration.VERTICAL, false);

        recyclerView.addItemDecoration(myDividerItemDecoration);



        resultAdapter.setOnScoreFillListener(new ResultAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(int position, String score) {
                resultAdapterOnScoreFill(position, score);
            }
        });

        resultAdapter.setOnStarClickListener(new ResultAdapter.OnStarClickListener() {
            @Override
            public void onStarClick(int position) {
                resultAdapterOnStarClick(position);
            }
        });

        resultAdapter.setOnFooterClickListener(new ResultAdapter.OnFooterClickListener() {
            @Override
            public void onFooterClick() {
                resultAdapterOnFooterClick();
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

        // Inflate the layout for this fragment
        return view;
    }

    private void initResultList() {
        String sqlSelect = "select Student.stu_id, Student.stu_name, Student.stu_gender "
                + "from Student";

        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());

        Cursor cursor = db.rawQuery(sqlSelect, new String[] {});

        while (cursor.moveToNext()) {
            int stu_id = cursor.getInt(cursor.getColumnIndex("stu_id"));
            String stu_name = cursor.getString(cursor.getColumnIndex("stu_name"));
            String stu_gender = cursor.getString(cursor.getColumnIndex("stu_gender"));

            Result result = new Result(String.valueOf(stu_id), stu_name, stu_gender);
            result.setCorrect(false);
//            result.setScore(String.valueOf(0));
//            result.setTotal_score(0);

            resultList.add(result);

            backUpStuId.add(String.valueOf(stu_id));
        }
        cursor.close();
    }

    /**
     * 先判断传入的score是否和resultList中对应的score相等，若相等则不作任何处理
     *
     * TextUtils.isEmpty判断null或者空字符串""则为true，否则为false（" "含有一个空格也为false）
     *
     * 传入的score和resultList中对应的score都为null或空字符串，则视为相等
     * @param position 当前EditText位置
     * @param score EditText的改变值
     */
    private void resultAdapterOnScoreFill(int position, String score) {
        if (position < resultList.size()) {

            //先判断传入的score是否和resultList中对应的score相等，若相等则不作任何处理
            /*
                TextUtils.isEmpty判断null或者空字符串""则为true，否则为false（" "含有一个空格也为false）
                传入的score和resultList中对应的score都为null或空字符串，则视为相等
             */
            if (TextUtils.isEmpty(resultList.get(position).getScore()) && TextUtils.isEmpty(score)) {
                return;
            }

            /*
                既非null也非空字符串""，则用equals再判断是否相等
             */
            if (!TextUtils.isEmpty(resultList.get(position).getScore()) && !TextUtils.isEmpty(score) && score.equals(resultList.get(position).getScore())) {
                return;
            }

            isResultListUpdate = true;
            setSaveBtnBackground(true);

            resultList.get(position).setScore(score);

            if (!TextUtils.isEmpty(score)) {
//                resultList.get(position).setScore(score);

                if (!score.contains(" ") && new CheckExpression().checkExpression(score)) {
                    int total_score = (int) new Calculator().calculate(score);
                    resultList.get(position).setTotal_score(total_score);

                    if(!recyclerView.isComputingLayout()) {
                        resultAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                //score编辑框为空，则设置total_score为0
                resultList.get(position).setTotal_score(0);
                if(!recyclerView.isComputingLayout()) {
                    resultAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 订正列点亮事件
     * @param position 点击位置
     */
    private void resultAdapterOnStarClick(int position) {
        Result result = resultList.get(position);
        boolean isCorrect = result.isCorrect();
        if (!isCorrect) {
            result.setCorrect(true);
//            Toast.makeText(getActivity(), R.string.revision_completed, Toast.LENGTH_SHORT).show();
            ToastHelper.showToast(getActivity(), getString(R.string.revision_completed), 800);
        } else {
            result.setCorrect(false);
//            Toast.makeText(getActivity(), R.string.revision_uncompleted, Toast.LENGTH_SHORT).show();
            ToastHelper.showToast(getActivity(), getString(R.string.revision_uncompleted), 800);
        }
        isResultListUpdate = true;
        setSaveBtnBackground(true);
        resultAdapter.notifyDataSetChanged();
    }


    /**
     *  每个成绩表页脚增加一个“统计信息”入口，点击后打开统计信息表，内容为：
     *     - 总分：xxx（分值要可以编辑）
     *     - 成绩尚缺：（列出姓名）
     *     - 订正尚缺：（列出姓名）
     */
    private void resultAdapterOnFooterClick() {
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
                String input = test_full_mark_edit.getText().toString();

                if (!TextUtils.isEmpty(input)) {
                    int test_full_mark = Integer.parseInt(input);
                    if (current_test_id > 0) {
                        updateTestFullMarkByTestId(current_test_id, test_full_mark);
                    }
                }

                alertDialog.dismiss();
            }
        });

        //成绩尚缺显示score为空的学生姓名
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i < resultList.size(); i++) {
            Result result = resultList.get(i);
            if (TextUtils.isEmpty(result.getScore())) {
                stringBuilder.append(result.getStu_name());
                if (i != resultList.size()-1) {
                    stringBuilder.append("、");
                }
            }
        }

        lack_score_name.setText(stringBuilder.toString());


        //订正尚缺则只显示当前页面isCorrect为false的学生姓名
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int i=0; i<resultList.size(); i++) {
            Result result = resultList.get(i);
            if (!result.isCorrect()) {
                stringBuilder1.append(result.getStu_name());
                if (i != resultList.size()-1) {
                    stringBuilder1.append("、");
                }
            }
        }

        lack_correct_name.setText(stringBuilder1.toString());
    }


    /**
     * 保存当前页面数据到数据库
     */
    private void saveDataToDatabase() {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());
        if (checkResultListLegality()) {

            if (current_test_name != null && current_test_id > 0) {

                for (String s : backUpStuId) {
                    db.delete("StudentMark", "stu_id = ? AND test_id = ?", new String[] {s+"", current_test_id+""});
                }

                for (Result result : resultList) {
                    String stu_id = result.getStu_id();
                    String score = result.getScore();
                    int total_score = result.getTotal_score();
                    boolean isCorrect = result.isCorrect();

                    new IDUSTool(getActivity()).insertStuMarkDB(stu_id, current_test_id, score, total_score, isCorrect);
                }

                Toast.makeText(getActivity(), R.string.save_successfully, Toast.LENGTH_SHORT).show();

                isResultListUpdate = false;
                setSaveBtnBackground(false);

                return;
            }

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
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager = (InputMethodManager) test_name_edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(test_name_edit, 0);
                }
            }, 300);


            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input_name = test_name_edit.getText().toString().trim();

                    long unique_test_id = getTest_idMax() + 1;

                    current_test_id = unique_test_id;

                    current_test_name = input_name;

                    if (TextUtils.isEmpty(input_name)) {
                        Toast.makeText(getActivity(), getString(R.string.test_name_cannot_be_empty) + input_name, Toast.LENGTH_SHORT).show();
                    } else {
                        //查询数据库中是否存在与将要插入的input_name相同的test_name，若相同，则提示用户test_name已存在重新输入，否则直接插入全部数据
                        if (hasSameTestName(input_name)) {
                            Toast.makeText(getActivity(), input_name+" 已存在，请重新输入", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //先把current_test_id和input_name插入到StudentTest表中

                        new IDUSTool(getActivity()).insertStuTest(current_test_id, input_name);

                        //保存总分
                        if (!TextUtils.isEmpty(test_full_mark_edit.getText().toString())) {
                            int test_full_mark = Integer.parseInt(test_full_mark_edit.getText().toString());
                            updateTestFullMarkByTestId(current_test_id, test_full_mark);
                        }

                        //直接插入当前页面数据到StudentMark表中
                        for (Result result : resultList) {
                            String stu_id = result.getStu_id();
                            String score = result.getScore();
                            int total_score = result.getTotal_score();
                            boolean isCorrect = result.isCorrect();

                            new IDUSTool(getActivity()).insertStuMarkDB(stu_id, current_test_id, score, total_score, isCorrect);
                        }

                        Toast.makeText(getActivity(), R.string.save_successfully, Toast.LENGTH_SHORT).show();

                        mListener.handleTitleBar(input_name);

                        isResultListUpdate = false;
                        setSaveBtnBackground(false);
                    }

                    alertDialog.dismiss();
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
            if (checkResultListLegality()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String the_only_test_name = dateFormat.format(Calendar.getInstance().getTime());

                if (current_test_name != null) {
                    the_only_test_name = current_test_name;
                }

                //导出Excel
                ExportSheet exportSheet = new ExportSheet(getActivity(), the_only_test_name, resultList);

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

    private void setSaveBtnBackground(boolean isResultListUpdate) {
        if (isResultListUpdate) {
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
     * 检查当前页面数据score合法性
     * @return 合法返回true,不合法返回false
     */
    private boolean checkResultListLegality() {
        boolean isScoreLegal = true;
        for (Result result : resultList) {
            //设置为空时score也合法
            if (TextUtils.isEmpty(result.getScore())) {

                continue;
            }
            if (result.getScore().contains(" ")) {
                isScoreLegal = false;
                Toast.makeText(getActivity(), getString(R.string.score_has_blank_space_hint, result.getStu_id(), result.getScore()), Toast.LENGTH_SHORT).show();
                break;
            }
            if (!(new CheckExpression().checkExpression(result.getScore()))) {
                isScoreLegal = false;
                Toast.makeText(getActivity(), getString(R.string.illegal_score_hint, result.getStu_id(), result.getScore()), Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return isScoreLegal;
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
     * 查询数据库中是否存在与将要插入的input_name相同的test_name，若相同，返回true，否则返回false
     * @param input_name 用户手动输入的测试名
     * @return 有相同test_name则返回true，否则返回false
     */
    private boolean hasSameTestName(String input_name) {
        SQLiteDatabase db = MyDatabaseHelper.getInstance(getActivity());

        String sqlSelect = "select test_name from StudentTest";
        Cursor cursor = db.rawQuery(sqlSelect, new String[]{});

        while (cursor.moveToNext()) {
            String test_name = cursor.getString(cursor.getColumnIndex("test_name"));

            if (input_name.equals(test_name)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public DigitalResultsFragmentListener mListener;

    public interface DigitalResultsFragmentListener {
        void handleTitleBar(String title);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //对传进来的Context进行接口转换
        if (context instanceof MainActivity) {
            mListener = (DigitalResultsFragmentListener) context;
        }
    }

    @Override
    public boolean onBackPressed() {

        if (isResultListUpdate) {
            AlertDialog alertDialog = GetAlertDialog.getAlertDialog(getActivity(), getString(R.string.save),
                    getString(R.string.recordUI_back_hint), null, getString(R.string.save), getString(R.string.not_save), getString(R.string.cancel));

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save_to_db.performClick();

                    if (!isResultListUpdate) {
                        //结束当前fragment
                        if (getFragmentManager().getBackStackEntryCount() > 0) {
                            getFragmentManager().popBackStack();
                            mListener.handleTitleBar(null);
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
                        mListener.handleTitleBar(null);
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
                mListener.handleTitleBar(null);
            }
            return true;
        }

//        mListener.toDigitalResults();
//
//        return BackHandlerHelper.handleBackPress(this);
    }
}
