package com.example.teacher_assistant_test.fragment;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.teacher_assistant_test.R;
import com.example.teacher_assistant_test.adapter.ResultAdapter;
import com.example.teacher_assistant_test.bean.Result;
import com.example.teacher_assistant_test.util.MyDatabaseHelper;
import com.example.teacher_assistant_test.util.MyDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualInputDigitalResultsFragment extends Fragment {

    private List<Result> resultList = new ArrayList<>();


    public ManualInputDigitalResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manual_input_digital_results, container, false);

        initResultList();

        RecyclerView recyclerView = view.findViewById(R.id.Recycler_View_Result);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);

        ResultAdapter resultAdapter = new ResultAdapter(resultList);

        recyclerView.setAdapter(resultAdapter);

        MyDividerItemDecoration myDividerItemDecoration = new MyDividerItemDecoration(getActivity(), MyDividerItemDecoration.VERTICAL, false);

        recyclerView.addItemDecoration(myDividerItemDecoration);

        resultAdapter.setOnFooterClickListener(new ResultAdapter.OnFooterClickListener() {
            @Override
            public void onFooterClick() {

            }
        });

        resultAdapter.setOnScoreFillListener(new ResultAdapter.OnScoreFillListener() {
            @Override
            public void onScoreFill(int position, String score) {

            }
        });

        resultAdapter.setOnStarClickListener(new ResultAdapter.OnStarClickListener() {
            @Override
            public void onStarClick(int position) {

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
            result.setScore(String.valueOf(0));
            result.setTotal_score(0);

            resultList.add(result);
        }
    }

}
