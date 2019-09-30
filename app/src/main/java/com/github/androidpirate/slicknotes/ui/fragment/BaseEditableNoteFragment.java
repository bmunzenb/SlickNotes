/*
 * <!--
 *  Copyright (C) 2016 The Android Open Source Project
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 * -->
 */

package com.github.androidpirate.slicknotes.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.github.androidpirate.slicknotes.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public abstract class BaseEditableNoteFragment extends Fragment {

    private static final String FAB_WHITE_COLOR_ID = "#FAFAFA";
    private static final String FAB_PINK_COLOR_ID = "#F2B4F7";
    private static final String FAB_YELLOW_COLOR_ID = "#F8EDB2";
    private static final String FAB_BLUE_COLOR_ID = "#B6CDFC";
    private static final String FAB_ORANGE_COLOR_ID = "#FAB266";
    private static final String FAB_GREEN_COLOR_ID = "#B6FCB9";
    private static final String FAB_PURPLE_COLOR_ID = "#C9B0F7";
    private static final String FAB_GRAY_COLOR_ID = "#D5D5D5";

    boolean isKeyboardOn = false;
    EditText title;
    EditText details;
    private Animation fabExpand, fabCollapse, fabRotateLeft,
            fabRotateRight, fabActionShow, fabActionHide;
    private View rootView;
    private FloatingActionButton fabAction;
    private FloatingActionButton fabAddLabel;
    private FloatingActionButton fabChangeColor;
    private FloatingActionButton fabShare;
    private NavController navController;
    private AlertDialog colorPickerDialog;
    boolean isFabActionOpen = false;
    boolean isFabOn = true;

    abstract void onColorPickerFabClick(int colorId);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater
                .inflate(R.layout.fragment_note_editable_base,
                        container,
                        false);
        // Set soft keyboard listener
        setSoftKeyboardListener(rootView);
        title = rootView.findViewById(R.id.et_title);
        details = rootView.findViewById(R.id.et_details);
        // Setup fab action
        setupFabAction(rootView);
        // setup fab animations
        setupFabAnimations();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navController = Navigation
                .findNavController(
                        Objects.requireNonNull(getActivity()),
                        R.id.nav_host_fragment);
    }

    void hideSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                Objects.requireNonNull(getActivity())
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocusedView = getActivity().getCurrentFocus();
        if(currentFocusedView != null) {
            Objects.requireNonNull(inputManager)
                    .hideSoftInputFromWindow(currentFocusedView.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    void navigateToList(int deletedNoteId) {
        Bundle args = new Bundle();
        args.putInt("deletedNoteId", deletedNoteId);
        navController.navigate(R.id.nav_details_to_home, args);
    }

    void setBackgroundColor(int colorId) {
        rootView.setBackgroundColor(colorId);
    }

    void hideColorPickerDialog() {
        colorPickerDialog.cancel();
    }

    private void setSoftKeyboardListener(final View view) {
        view.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    //r will be populated with the coordinates of
                    // your view that area still visible.
                    view.getWindowVisibleDisplayFrame(r);
                    int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
                    if (heightDiff > 500 && isFabOn) {
                        // if more than 100 pixels, its probably a keyboard...
                        isKeyboardOn = true;
                        isFabOn = false;
                        hideFabAction();
                    } else if (heightDiff < 500 && !isFabOn){
                        isKeyboardOn = false;
                        isFabOn = true;
                        clearFocusFromTextFields();
                        showFabAction();
                    }
                }
            });
    }

    private void clearFocusFromTextFields() {
        title.clearFocus();
        details.clearFocus();
    }

    private void setupFabAnimations() {
        Context context = getContext();
        fabExpand = AnimationUtils.loadAnimation(context, R.anim.fab_expand);
        fabCollapse = AnimationUtils.loadAnimation(context, R.anim.fab_collapse);
        fabRotateLeft = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_left);
        fabRotateRight = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_right);
        fabActionShow = AnimationUtils.loadAnimation(context, R.anim.fab_action_show);
        fabActionHide = AnimationUtils.loadAnimation(context, R.anim.fab_action_hide);
    }

    private void animateFab() {
        if(isFabActionOpen) {
            fabAction.startAnimation(fabRotateRight);
            fabAddLabel.startAnimation(fabCollapse);
            fabAddLabel.setClickable(false);
            fabChangeColor.startAnimation(fabCollapse);
            fabChangeColor.setClickable(false);
            fabShare.startAnimation(fabCollapse);
            fabShare.setClickable(false);
            isFabActionOpen = false;
        } else {
            fabAction.startAnimation(fabRotateLeft);
            fabAddLabel.startAnimation(fabExpand);
            fabAddLabel.setClickable(true);
            fabChangeColor.startAnimation(fabExpand);
            fabChangeColor.setClickable(true);
            fabShare.startAnimation(fabExpand);
            fabShare.setClickable(true);
            isFabActionOpen = true;
        }
    }

    private void showFabAction() {
        fabAction.startAnimation(fabActionShow);
    }

    private void hideFabAction() {
        if(isFabActionOpen) {
            animateFab();
        }
        fabAction.startAnimation(fabActionHide);
    }

    private void setupFabAction(View view) {
        fabAction = view.findViewById(R.id.fab_actions);
        fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
            }
        });
        fabAddLabel = view.findViewById(R.id.fab_add_label);
        fabAddLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 3: Implement adding labels here
            }
        });
        fabChangeColor = view.findViewById(R.id.fab_change_color);
        fabChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayColorPickerDialog();
            }
        });
        fabShare = view.findViewById(R.id.fab_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 4: Implement sharing options here
            }
        });
    }

    private void displayColorPickerDialog() {
        // Inflate dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pick Card Background")
                .setView(dialogView)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideColorPickerDialog();
                    }
                });
        colorPickerDialog = builder.create();
        // Setup color picker fabs
        setupColorPickerFabs(dialogView);
        colorPickerDialog.show();
    }

    private void setupColorPickerFabs(final View dialogView) {
        FloatingActionButton fabWhite = dialogView.findViewById(R.id.fab_white);
        fabWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_WHITE_COLOR_ID));
            }
        });
        FloatingActionButton fabPink = dialogView.findViewById(R.id.fab_pink);
        fabPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_PINK_COLOR_ID));
            }
        });
        FloatingActionButton fabYellow = dialogView.findViewById(R.id.fab_yellow);
        fabYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_YELLOW_COLOR_ID));
            }
        });
        FloatingActionButton fabBlue = dialogView.findViewById(R.id.fab_blue);
        fabBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_BLUE_COLOR_ID));
            }
        });
        FloatingActionButton fabOrange = dialogView.findViewById(R.id.fab_orange);
        fabOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_ORANGE_COLOR_ID));
            }
        });
        FloatingActionButton fabGreen = dialogView.findViewById(R.id.fab_green);
        fabGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_GREEN_COLOR_ID));
            }
        });
        FloatingActionButton fabPurple = dialogView.findViewById(R.id.fab_purple);
        fabPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_PURPLE_COLOR_ID));
            }
        });
        FloatingActionButton fabGray = dialogView.findViewById(R.id.fab_gray);
        fabGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorPickerFabClick(Color.parseColor(FAB_GRAY_COLOR_ID));
            }
        });

    }
}
