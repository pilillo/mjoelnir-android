package at.aau.nes.mjoelnir;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;

import java.util.ArrayList;
import java.util.List;

import at.aau.nes.mjoelnir.aggregatedData.MeterSummary;
import at.aau.nes.mjoelnir.disaggregatedData.DeviceListFragment;
import at.aau.nes.mjoelnir.webView.Analytics;

public class MainFragment extends Fragment {

    private AppPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new AppPagerAdapter(getChildFragmentManager());
        adapter.addFragment(MeterSummary.newInstance(), "Main");
        adapter.addFragment(DeviceListFragment.newInstance(), "Devices");
        adapter.addFragment(Analytics.newInstance(), "Analytics");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        //viewPager.setPageTransformer(true, new RotateUpTransformer());

        tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();

        return v;
    }

    public class AppPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public AppPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
