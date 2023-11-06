package com.github.tvbox.osc.ui.adapter;

import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.github.tvbox.osc.base.BaseLazyFragment;
import java.util.List;

public class HomePageAdapter extends FragmentPagerAdapter {
    public FragmentManager fragmentManager;
    public List<BaseLazyFragment> list;

    public HomePageAdapter(FragmentManager fragmentManager2) {
        super(fragmentManager2);
    }

    public HomePageAdapter(FragmentManager fragmentManager2, List<BaseLazyFragment> list2) {
        super(fragmentManager2);
        this.fragmentManager = fragmentManager2;
        this.list = list2;
    }

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    @Override // androidx.fragment.app.FragmentPagerAdapter
    public Fragment getItem(int i) {
        return this.list.get(i);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        List<BaseLazyFragment> list2 = this.list;
        if (list2 != null) {
            return list2.size();
        }
        return 0;
    }

    @Override // androidx.viewpager.widget.PagerAdapter, androidx.fragment.app.FragmentPagerAdapter
    public Fragment instantiateItem(ViewGroup viewGroup, int i) {
        Fragment fragment = (Fragment) super.instantiateItem(viewGroup, i);
        this.fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss();
        return fragment;
    }

    @Override // androidx.viewpager.widget.PagerAdapter, androidx.fragment.app.FragmentPagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        this.fragmentManager.beginTransaction().hide(this.list.get(i)).commitAllowingStateLoss();
    }
}
