package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioGroup;

import java.util.concurrent.TimeUnit;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import pl.lukjar.bottom_sheet_library.BottomSheetBehavior;
import pl.lukjar.bottom_sheet_library.BottomSheetView;
import pl.lukjar.bottom_sheet_library.MergedAppBarLayoutBehavior;
import pl.lukjar.bottom_sheet_library.OnLayoutBehaviorReadyListener;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    View bottomSheetTextViewContainer;
    View blackOverlay;
    View showResultButton;
    View progressBar;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        blackOverlay = findViewById(R.id.overlay);
        showResultButton = findViewById(R.id.show_result_button);
        progressBar = findViewById(R.id.progress_bar);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        final BottomSheetView bottomSheet = (BottomSheetView) findViewById(R.id.bottom_sheet);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        final AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        final MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);

        mergedAppBarLayoutBehavior.setToolbarTitle("Filtruj i sortuj");
        mergedAppBarLayoutBehavior.setOnLayoutBehaviorReadyListener(new OnLayoutBehaviorReadyListener() {
            @Override
            public void onLayoutBehaviourReady() {
                bottomSheet.setAppBarTextLeftDistance(mergedAppBarLayoutBehavior.getTitleTextView().getLeft());
                bottomSheet.setAppBarTextTopDistance(mergedAppBarLayoutBehavior.getTitleTextView().getTop());
                bottomSheet.setAppBarTextSize(mergedAppBarLayoutBehavior.getTitleTextView().getTextSize());
            }
        });

        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        bottomSheetTextViewContainer = bottomSheet.findViewById(R.id.title_container);
        bottomSheetTextViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BottomSheetBehavior.STATE_COLLAPSED == behavior.getState()) {
                    behavior.setState(BottomSheetBehavior.STATE_ANCHOR_POINT);
                } else if (BottomSheetBehavior.STATE_ANCHOR_POINT == behavior.getState()) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        blackOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BottomSheetBehavior.STATE_ANCHOR_POINT == behavior.getState()) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        showResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                progressBar.setVisibility(View.VISIBLE);
                rx.Observable.just(1).delay(2, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }
    }


}
