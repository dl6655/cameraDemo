package com.example.cameraDemo.camera;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;
import com.example.cameraDemo.CameraActivity;

import java.util.List;

public class CameraViewPagerAdapter extends FragmentPagerAdapter {

	
	private List<CameraPicImageViewFragment> fragments;
	
	public CameraViewPagerAdapter(FragmentManager fm, List<CameraPicImageViewFragment> fragments) {
		super(fm);
		this.fragments = fragments;
		
	}
	

	@Override
	public Fragment getItem(int arg0) {
		CameraPicImageViewFragment fragment = fragments.get(arg0);
		return fragment;
	}

	@Override
	public int getCount() {
		if(this.fragments != null && this.fragments.size() > 0){
			return fragments.size();
		}else{
			return 0;
		}
		
	}


	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}


	public void setImageList(List<CameraActivity.CameraPicInfo> list){
//		this.picList = list;
	}


	@Override
	public int getItemPosition(Object object) {
		return PagerAdapter.POSITION_NONE;
	}


	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		CameraPicImageViewFragment fragment = (CameraPicImageViewFragment)super.instantiateItem(container, position);
		return fragment;
	}
	

}
