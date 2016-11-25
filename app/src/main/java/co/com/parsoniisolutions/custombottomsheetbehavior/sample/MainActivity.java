package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.databinding.ActivityMainBinding;
import pl.lukjar.bottom_sheet_library.BottomSheetView;
import pl.lukjar.bottom_sheet_library.MergedAppBarLayout;
import rx.Observable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initToolbar(binding.toolbar);

        final BottomSheetView bottomSheet = (BottomSheetView) findViewById(R.id.bottom_sheet);
        final MergedAppBarLayout mergedAppBarLayout = (MergedAppBarLayout) findViewById(R.id.merged_appbar_layout);
        bottomSheet.defaultBehaviorConnectionWith(mergedAppBarLayout);

        binding.overlay.setOnClickListener(v -> {
            if (bottomSheet.isInAnchorPosition()) {
                bottomSheet.close();
            }
        });

        binding.showResultButton.setOnClickListener(view -> bottomSheet.close());
        ((RadioGroup) findViewById(R.id.radio_group))
                .setOnCheckedChangeListener((radioGroup, i) -> simulateLongNetworkOperation());
    }

    private void simulateLongNetworkOperation() {
        binding.progressBar.setVisibility(VISIBLE);
        Observable.just(null).delay(2, SECONDS)
                .observeOn(mainThread())
                .subscribe(it -> binding.progressBar.setVisibility(GONE));
    }

    private void initToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }
    }


}
