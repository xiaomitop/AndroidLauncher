package com.jit.demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.jit.demo.R;
import com.jit.demo.adapter.ScrollAdapter;
import com.jit.demo.model.MoveItem;
import com.jit.demo.wight.ScrollLayout;
import com.jit.demo.wight.ScrollLayout.OnAddOrDeletePage;
import com.jit.demo.wight.ScrollLayout.OnEditModeListener;
import com.jit.demo.wight.ScrollLayout.OnPageChangedListener;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnAddOrDeletePage,
        OnPageChangedListener, OnEditModeListener {

    // 滑动控件的容器Container
    private ScrollLayout mContainer;

    // Container的Adapter
    private ScrollAdapter mItemsAdapter;
    // Container中滑动控件列表
    private List<MoveItem> mList;

    //xUtils中操纵SQLite的助手类
    private DbUtils mDbUtils;
    private LinearLayout mLlDot;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 从缓存中初始化滑动控件列表
        getDataFromCache();
        // 初始化控件
        initView();
        //初始化容器Adapter
        loadBackground();
    }

    private void getDataFromCache() {
        mDbUtils = DbUtils.create(this);
        try {
            //使用xUtils，基于orderId从SQLite数据库中获取滑动控件
            mList = mDbUtils.findAll(Selector.from(MoveItem.class).orderBy("orderId", false));
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        inflater = LayoutInflater.from(this);
        mContainer = (ScrollLayout) findViewById(R.id.container);
        mLlDot = (LinearLayout) findViewById(R.id.ll_dot);
        //如果没有缓存数据，则手动添加10条
        if (mList == null || mList.size() == 0) {
            mList = new ArrayList<MoveItem>();
            for (int i = 1; i < 11; i++) {
                MoveItem item = new MoveItem();
                //根据drawable name获取对于的ID
                item.setImgdown(getDrawableId("item" + i + "_down"));
                item.setImgurl(getDrawableId("item" + i + "_normal"));
                item.setOrderId(i);
                item.setMid(i);
                mList.add(item);
            }
        }
        //初始化Container的Adapter
        mItemsAdapter = new ScrollAdapter(this, mList);
        //设置Container添加删除Item的回调
        mContainer.setOnAddPage(this);
        //设置Container页面换转的回调，比如自第一页滑动第二页
        mContainer.setOnPageChangedListener(this);
        //设置Container编辑模式的回调，长按进入修改模式
        mContainer.setOnEditModeListener(this);
        //设置Adapter
        mContainer.setSaAdapter(mItemsAdapter);
        //动态设置Container每页的列数为2行
        mContainer.setColCount(3);
        //动态设置Container每页的行数为4行
        mContainer.setRowCount(2);
        //调用refreView绘制所有的Item
        mContainer.refreView();

        findViewById(R.id.btnDel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemsAdapter.delete(2);
                //调用refreView绘制所有的Item
                mContainer.refreView();
            }
        });
    }

    // 设置Container滑动背景图片
    private void loadBackground() {
        /*Options options = new Options();
		options.inSampleSize = 2;
		mContainer.setBackGroud(BitmapFactory.decodeResource(getResources(),
				R.drawable.main_bg, options));*/
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", "com.jit.demo");
    }

    @Override
    public void onBackPressed() {
        //back键监听，如果在编辑模式，则取消编辑模式
        if (mContainer.isEditting()) {
            mContainer.showEdit(false);
            return;
        } else {
            try {
                //退出APP前，保存当前的Items，记得所有item的位置
                List<MoveItem> list = mContainer.getAllMoveItems();
                mDbUtils.saveAll(list);
            } catch (DbException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onEdit() {
        Log.e("test", "onEdit");
    }

    @Override
    public void onPage2Other(int former, int current) {
        Log.e("test", "former-->" + former + "  current-->" + current);
        // 取消圆点选中
        mLlDot.getChildAt(former)
                .findViewById(R.id.v_dot)
                .setBackgroundResource(R.drawable.dot_normal);
        // 圆点选中
        mLlDot.getChildAt(current)
                .findViewById(R.id.v_dot)
                .setBackgroundResource(R.drawable.dot_selected);
    }

    public void onAddOrDeletePage(int page, boolean isAdd) {
        if (isAdd) {
            mLlDot.addView(inflater.inflate(R.layout.dot, null));
        } else {
            mLlDot.removeViewAt(page);
        }
    }

    /**
     * 设置圆点
     *//*
	public void setOvalLayout() {
		mLlDot.removeAllViews();
		for (int i = 0; i < pageCount; i++) {
			mLlDot.addView(inflater.inflate(R.layout.dot, null));
		}
		// 默认显示第一页
		mLlDot.getChildAt(0).findViewById(R.id.v_dot)
				.setBackgroundResource(R.drawable.dot_selected);
	}*/

}
