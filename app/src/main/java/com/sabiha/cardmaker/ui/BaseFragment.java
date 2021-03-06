package com.sabiha.cardmaker.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sabiha.cardmaker.BaseActivity;

public class BaseFragment extends Fragment {
    public BaseActivity baseActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
    }

    public void showToast(String msg) {
        baseActivity.showToast(msg);
    }
}
