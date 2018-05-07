package com.siqiyan.lightlu.smallsqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.siqiyan.lightlu.smallsqlite.annotion.DbField;
import com.siqiyan.lightlu.smallsqlite.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 创建日期：2018/5/7 on 22:02
 *
 * @author ludaguang
 * @version 1.0
 *          类说明：数据库操作实现类，维持数据映射
 */

public abstract class BaseDao<T> implements IBaseDao<T> {
    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 是否初始化过
     */
    boolean isInit = false;
    private SQLiteDatabase database;
    private Class<T> entity;
    /**
     * 维护这表名与成员变量名的映射关系
     * key---》表名
     * value --》Field
     */
    private HashMap<String, Field> cacheMap;

    /**
     * 初始化数据
     *
     * @param entityClass
     * @param sqLiteDatabase
     * @return
     */
    protected synchronized boolean init(Class<T> entityClass, SQLiteDatabase sqLiteDatabase) {
        if (!isInit) {
            entity = entityClass;
            database = sqLiteDatabase;
            //获取表名
            if (entity.getAnnotation(DbTable.class) == null) {
                tableName = entity.getClass().getName();
            } else {
                tableName = entity.getAnnotation(DbTable.class).value();
            }
            if (!database.isOpen()) {
                return false;
            }
            //创建表
            if (!TextUtils.isEmpty(createTable())) {
                database.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;

        }
        return isInit;

    }

    /**
     * 初始化表的映射关系
     */
    private void initCacheMap() {
        String sql = "select * from " + this.tableName + " limit 1 , 0";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);

            String[] columnNames = cursor.getColumnNames();
            Field[] fields = entity.getFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }

            Field columnFiled = null;
            for (String column : columnNames) {
                for (Field field : fields) {
                    String fieldName = null;
                    if (field.getAnnotation(DbField.class) != null) {
                        fieldName = field.getAnnotation(DbField.class).value();
                    } else {
                        fieldName = field.getName();
                    }
                    /**
                     * 如果表的列名 等于了  成员变量的注解名字
                     */
                    if (column.equals(fieldName)) {
                        columnFiled = field;
                        break;
                    }

                    if (columnFiled != null) {
                        cacheMap.put(column, columnFiled);
                        break;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }


    }

    /**
     * 创建表语句,目前暂时由用户实现
     *
     * @return
     */
    protected abstract String createTable();


    @Override
    public long insert(T entity) {


        return ;
    }

    @Override
    public long update(T entity) {
        return 0;
    }
}
