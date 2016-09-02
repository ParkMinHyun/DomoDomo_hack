package ensharp.tapcorder.MobileHub.mobile.mysampleapp.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.regions.Regions;

import ensharp.tapcorder.MobileHub.mobile.AWSConfiguration;
import ensharp.tapcorder.R;


public class UserFilesDemoFragment extends DemoFragmentBase {
    private static final String S3_PREFIX_PUBLIC = "public/";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo_user_files, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_userFiles_public)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        browse(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, S3_PREFIX_PUBLIC, AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET_REGION);
                    }
                });
    }

    private void browse(String bucket, String prefix, Regions region) {
        UserFilesBrowserFragment fragment = new UserFilesBrowserFragment();
        Bundle args = new Bundle();
        args.putString(UserFilesBrowserFragment.BUNDLE_ARGS_S3_BUCKET, bucket);
        args.putString(UserFilesBrowserFragment.BUNDLE_ARGS_S3_PREFIX, prefix);
        args.putString(UserFilesBrowserFragment.BUNDLE_ARGS_S3_REGION, region.getName());
        fragment.setArguments(args);
        getActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }
}
