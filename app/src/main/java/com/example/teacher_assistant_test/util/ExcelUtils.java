package com.example.teacher_assistant_test.util;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    public static WritableFont arial14font = null;
    public static WritableCellFormat arial14format = null;

    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;

    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式 颜色背景等...
     */
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);

            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(Alignment.CENTRE);
            arial14format.setBorder(Border.ALL, BorderLineStyle.THIN);
            arial14format.setBackground(Colour.VERY_LIGHT_YELLOW);


            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);

            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(Alignment.CENTRE);
            arial10format.setBorder(Border.ALL, BorderLineStyle.THIN);
            arial10format.setBackground(Colour.VERY_LIGHT_YELLOW);


            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setAlignment(Alignment.CENTRE);//对齐格式
            arial12format.setBorder(Border.ALL, BorderLineStyle.THIN);//设置边框


        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Excel
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("成绩表",0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for(int col=0; col<colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));//先是列，后是行
            }
            sheet.setRowView(0, 340);//设置行高

            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList, String fileName, Context c) {
        if(objList != null && objList.size() > 0) {
            WritableWorkbook writeBook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);

                in = new FileInputStream(new File(fileName));

                Workbook workbook = Workbook.getWorkbook(in);
                writeBook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = writeBook.getSheet(0);

                //i:列（Column)，j:行(Row)。
                for(int j=0; j<objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    for(int i=0; i<list.size(); i++) {
                        sheet.addCell(new Label(i, j+1, list.get(i), arial12format));
                        if(list.get(i).length() <= 5) {
                            sheet.setColumnView(i, list.get(i).length()+8);//设置列宽
                        } else {
                            sheet.setColumnView(i, list.get(i).length()+5);//设置列宽
                        }
                    }
                    sheet.setRowView(j+1, 350);
                }

                writeBook.write();
                Toast.makeText(c, "导出到手机中文件夹Record成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(writeBook != null) {
                    try {
                        writeBook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
