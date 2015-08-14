package com.example.cameraDemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import com.example.cameraDemo.camera.CameraPicImageViewFragment;
import com.example.cameraDemo.camera.CameraPicManager;
import com.example.cameraDemo.camera.CameraViewPagerAdapter;
import com.example.cameraDemo.inject.From;
import com.example.cameraDemo.inject.Injector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingli on 2015-8-13.
 */
public class CameraPicViewActivity extends FragmentActivity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    @From(R.id.camera_viewpager)
    private ViewPager viewpager;
    @From(R.id.close_imageview)
    private ImageView closeImageView;
    @From(R.id.del_imageview)
    private ImageView delImageView;

    private CameraViewPagerAdapter pageViewAdapter;

    private List<CameraPicImageViewFragment> fragments = new ArrayList<CameraPicImageViewFragment>();

    private int currentPosition;

    private boolean isDelImage = false;


//	private ArrayList<CameraPicInfo> cameraPicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_pic_view);
        Injector.inject(this);
        initFragments();
        pageViewAdapter = new CameraViewPagerAdapter(getSupportFragmentManager(),fragments);
        Bundle bundle = null;
        if(getIntent() != null){
            bundle = getIntent().getExtras();
            if(bundle == null){
                bundle = savedInstanceState;
            }
        }
        if(bundle != null){
            currentPosition = bundle.getInt("position");
        }
        closeImageView.setClickable(true);
        closeImageView.setOnClickListener(this);
        delImageView.setClickable(true);
        delImageView.setOnClickListener(this);

        viewpager.setAdapter(pageViewAdapter);
        viewpager.setCurrentItem(currentPosition);
        viewpager.setOnPageChangeListener(this);
    }

    private void initFragments() {
        fragments.clear();
        List<CameraActivity.CameraPicInfo> picList = CameraPicManager.getInstance().getCameraPicInfoList();
        if(picList != null && picList.size() > 0){
            CameraPicImageViewFragment fragment;
            for(int i = 0;i<picList.size();i++){
                fragment = CameraPicImageViewFragment.newInstance(picList.get(i));
                fragments.add(fragment);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(closeImageView)){
            Bundle bundle = new Bundle();
            bundle.putBoolean(CameraActivity.BUNDLE_KEY_IS_DEL_IMAGE, isDelImage);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            this.setResult(RESULT_OK,intent);
            this.finish();
        }else if(v.equals(delImageView)){
            isDelImage = true;
            CameraPicManager.getInstance().getCameraPicInfoList().remove(currentPosition);
            CameraPicManager.getInstance().getThumbBitmapList().remove(currentPosition);
            if(CameraPicManager.getInstance().getCameraPicInfoList().size() <= 0){
                Bundle bundle = new Bundle();
                bundle.putBoolean(CameraActivity.BUNDLE_KEY_IS_DEL_IMAGE, isDelImage);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                this.setResult(RESULT_OK,intent);
                this.finish();
            }else{
                initFragments();
                List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
                if(fragmentList != null && fragmentList.size() > 0){
                    fragmentList.clear();
                }
                pageViewAdapter = new CameraViewPagerAdapter(getSupportFragmentManager(),fragments);
                viewpager.setAdapter(pageViewAdapter);
                pageViewAdapter.notifyDataSetChanged();
                if(currentPosition > fragments.size() -1){
                    currentPosition = fragments.size() -1;
                }else if(currentPosition <= 0){
                    currentPosition = 0;
                }else{
                    currentPosition = currentPosition-1;
                }
                viewpager.setCurrentItem(currentPosition);
            }

        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int currentPosition) {
        this.currentPosition = currentPosition;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", currentPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(CameraActivity.BUNDLE_KEY_IS_DEL_IMAGE, isDelImage);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        this.setResult(RESULT_OK,intent);
        this.finish();
    }



}
