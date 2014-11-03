package com.dataart.vyakunin.udacitystudyproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ((TextView) rootView.findViewById(R.id.info_text)).setText(getActivity().getIntent().getStringExtra(DetailActivity.TEXT_EXTRA));
        return rootView;
    }
}
