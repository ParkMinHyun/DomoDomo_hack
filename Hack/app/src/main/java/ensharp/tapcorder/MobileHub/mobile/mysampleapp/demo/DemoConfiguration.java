package ensharp.tapcorder.MobileHub.mobile.mysampleapp.demo;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ensharp.tapcorder.R;

public class DemoConfiguration {

    private static final List<DemoFeature> demoFeatures = new ArrayList<DemoFeature>();

    static {
        addDemoFeature("user_data_storage", R.mipmap.user_data_storage,
                R.string.feature_user_data_storage_title,
                R.string.feature_user_data_storage_subtitle
                , new DemoItem(R.string.main_fragment_title_user_files, R.mipmap.user_files,
                        R.string.feature_user_data_storage_demo_button_user_file_storage,
                        UserFilesDemoFragment.class)
        );
    }

    public static List<DemoFeature> getDemoFeatureList() {
        return Collections.unmodifiableList(demoFeatures);
    }

    public static DemoFeature getDemoFeatureByName(final String name) {
        for (DemoFeature demoFeature : demoFeatures) {
            if (demoFeature.name.equals(name)) {
                return demoFeature;
            }
        }
        return null;
    }

    private static void addDemoFeature(final String name, final int iconResId, final int titleResId,
                                       final int subtitleResId,
                                       final DemoItem... demoItems) {
        DemoFeature demoFeature = new DemoFeature(name, iconResId, titleResId, subtitleResId,
                demoItems);
        demoFeatures.add(demoFeature);
    }

    public static class DemoFeature {
        public String name;
        public int iconResId;
        public int titleResId;
        public int subtitleResId;
        public List<DemoItem> demos;

        public DemoFeature() {

        }

        public DemoFeature(final String name, final int iconResId, final int titleResId,
                           final int subtitleResId,
                           final DemoItem... demoItems) {
            this.name = name;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.subtitleResId = subtitleResId;
            this.demos = Arrays.asList(demoItems);
        }
    }

    public static class DemoItem {
        public int titleResId;
        public int iconResId;
        public int buttonTextResId;
        public String fragmentClassName;

        public DemoItem(final int titleResId, final int iconResId, final int buttonTextResId,
                        final Class<? extends Fragment> fragmentClass) {
            this.titleResId = titleResId;
            this.iconResId = iconResId;
            this.buttonTextResId = buttonTextResId;
            this.fragmentClassName = fragmentClass.getName();
        }
    }
}
