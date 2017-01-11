package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
/**
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 */

/**
 * This behavior should be applied on an AppBarLayout... More Explanations coming soon
 */
public class MergedAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private static final String TAG = MergedAppBarLayoutBehavior.class.getSimpleName();

    private boolean mInit = false;

    private FrameLayout.LayoutParams mBackGroundLayoutParams;

    private Context mContext;
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing {@link BottomSheetBehaviorGoogleMapsLike#mPeekHeight}
     * get changed dynamically we get the {@link NestedScrollView} that has
     * "app:layout_behavior=" {@link BottomSheetBehaviorGoogleMapsLike} inside the {@link CoordinatorLayout}
     */
    private BottomSheetBehaviorGoogleMapsLike mBottomSheetBehavior;
    private float mInitialY;
    private boolean mVisible = false;

    private String mToolbarTitle;

    private Toolbar mToolbar;
    private TextView mTitleTextView;
    private View mBackground;
    private View.OnClickListener mOnNavigationClickListener;

    private ValueAnimator mTitleAlphaValueAnimator;
    private int mCurrentTitleAlpha = 0;

    public MergedAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        if (!mInit) {
            init(parent, child, dependency);
        }
        /**
         * Following docs we should return true if the Behavior changed the child view's size or position, false otherwise
         */
        boolean childMoved = false;

        if(isDependencyYBelowAnchorPoint(dependency)){

            childMoved = setToolbarVisible(false,child);

        }else if(isDependencyYBetweenAnchorPointAndToolbar(child,dependency)){

            childMoved = setToolbarVisible(true,child);
            setFullBackGroundColor(android.R.color.transparent);
            setPartialBackGroundHeight(0);

        } else if(isDependencyYBelowToolbar(child, dependency) && ! isDependencyYReachTop(dependency)){

            childMoved = setToolbarVisible(true,child);
            if(isStatusBarVisible())
                setStatusBarBackgroundVisible(false);
            if(isTitleVisible())
                setTitleVisible(false);
            setFullBackGroundColor(android.R.color.transparent);
            setPartialBackGroundHeight((int)((child.getHeight() + child.getY()) - dependency.getY()));

        } else if(isDependencyYBelowStatusToolbar(child, dependency) || isDependencyYReachTop(dependency)){

            childMoved = setToolbarVisible(true,child);
            if(!isStatusBarVisible())
                setStatusBarBackgroundVisible(true);
            if(!isTitleVisible())
                setTitleVisible(true);
            setFullBackGroundColor(R.color.colorPrimary);
            setPartialBackGroundHeight(0);
        }
        return childMoved;
    }

    private void init(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency){

        AppBarLayout appBarLayout = (AppBarLayout) child;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }

        mToolbar = (Toolbar) appBarLayout.findViewById(R.id.expanded_toolbar);
        mBackground = appBarLayout.findViewById(R.id.background);
        mBackGroundLayoutParams = (FrameLayout.LayoutParams) mBackground.getLayoutParams();
        getBottomSheetBehavior(parent);

        mTitleTextView = findTitleTextView(mToolbar);
        if (mTitleTextView == null)
            return;

        mInitialY = child.getY();

        child.setVisibility(mVisible ? View.VISIBLE : View.INVISIBLE);
//        setStatusBarBackgroundVisible(mVisible);

        setFullBackGroundColor(mVisible && mCurrentTitleAlpha == 1 ? R.color.colorPrimary: android.R.color.transparent);
        setPartialBackGroundHeight(0);
        mTitleTextView.setText(mToolbarTitle);
        mTitleTextView.setAlpha(mCurrentTitleAlpha);
        mInit = true;
    }

    /**
     * Look into the CoordiantorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior(CoordinatorLayout coordinatorLayout) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);

            if (child instanceof NestedScrollView) {

                try {
                    mBottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(child);
                    break;
                }
                catch (IllegalArgumentException e){}
            }
        }
    }

    private boolean isDependencyYBelowAnchorPoint(@NonNull View dependency){
        return dependency.getY() > mBottomSheetBehavior.mAnchorPoint;
    }

    private boolean isDependencyYBetweenAnchorPointAndToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= mBottomSheetBehavior.mAnchorPoint && dependency.getY() > child.getY() + child.getHeight();
    }

    private boolean isDependencyYBelowToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= child.getY() + child.getHeight() && dependency.getY() > child.getY();
    }

    private boolean isDependencyYBelowStatusToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= child.getY();
    }

    private boolean isDependencyYReachTop(@NonNull View dependency){
        return dependency.getY() == 0;
    }

    private void setPartialBackGroundHeight(int height){
        mBackGroundLayoutParams.height = height;
        mBackground.setLayoutParams(mBackGroundLayoutParams);
    }

    private void setFullBackGroundColor(@ColorRes int colorRes){
        mToolbar.setBackgroundColor(ContextCompat.getColor(mContext,colorRes));
    }

    private TextView findTitleTextView(Toolbar toolbar){
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View toolBarChild = toolbar.getChildAt(i);
            if (toolBarChild instanceof TextView &&
                    ((TextView)toolBarChild).getText() != null &&
                    ((TextView)toolBarChild).getText().toString().contentEquals(mContext.getResources().getString(R.string.key_binding_default_toolbar_name))) {
                return (TextView) toolBarChild;
            }
        }
        return null;
    }

    private boolean setToolbarVisible(boolean visible, final View child){
        ViewPropertyAnimator mAppBarLayoutAnimation;
        boolean childMoved = false;
        if(visible && !mVisible){
            childMoved = true;
            child.setY(-child.getHeight()/3);
            mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarLayoutAnimation.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    child.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((AppCompatActivity)mContext).setSupportActionBar(mToolbar);
                    mToolbar.setNavigationOnClickListener(mOnNavigationClickListener);
                    ActionBar actionBar = ((AppCompatActivity)mContext).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }
                    mVisible = true;
                }
            });
            mAppBarLayoutAnimation.alpha(1).y(mInitialY).start();
        }else if(!visible && mVisible){
            mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarLayoutAnimation.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    child.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((AppCompatActivity)mContext).setSupportActionBar(null);
                    mVisible = false;
                }
            });
            mAppBarLayoutAnimation.alpha(0).start();
        }

        return childMoved;
    }

    private boolean isTitleVisible(){
        return mTitleTextView.getAlpha() == 1;
    }

    private void setTitleVisible(boolean visible){

        if((visible && mTitleTextView.getAlpha() == 1)||
          (!visible && mTitleTextView.getAlpha() == 0))
            return;

        if(mTitleAlphaValueAnimator == null || !mTitleAlphaValueAnimator.isRunning()){
            mToolbar.setTitle(mToolbarTitle);
            int startAlpha = visible ? 0 : 1;
            int endAlpha = mCurrentTitleAlpha = visible ? 1 : 0;

            mTitleAlphaValueAnimator = ValueAnimator.ofFloat(startAlpha,endAlpha);
            mTitleAlphaValueAnimator.setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mTitleAlphaValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTitleTextView.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            mTitleAlphaValueAnimator.start();
        }
    }

    private boolean isStatusBarVisible(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return ((Activity)mContext).getWindow().getStatusBarColor() ==
                    ContextCompat.getColor(mContext,R.color.colorPrimaryDark);
        }
        return true;
    }

    private void setStatusBarBackgroundVisible(boolean visible){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(visible){
                Window window = ((Activity)mContext).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
            }else {
                Window window = ((Activity)mContext).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(mContext,android.R.color.transparent));
            }
        }
    }

    public void setNavigationOnClickListener(View.OnClickListener listener){
        this.mOnNavigationClickListener = listener;
    }

    public void setToolbarTitle(String title) {
        this.mToolbarTitle = title;
        if(this.mToolbar!=null)
            this.mToolbar.setTitle(title);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        return new SavedState(super.onSaveInstanceState(parent, child),
                mVisible,
                mToolbarTitle,
                mCurrentTitleAlpha);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        this.mVisible = ss.mVisible;
        this.mToolbarTitle = ss.mToolbarTitle;
        this.mCurrentTitleAlpha = ss.mTitleAlpha;
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean mVisible;
        final String mToolbarTitle;
        final int mTitleAlpha;

        public SavedState(Parcel source) {
            super(source);
            mVisible = source.readByte() != 0;
            mToolbarTitle = source.readString();
            mTitleAlpha = source.readInt();
        }

        public SavedState(Parcelable superState, boolean visible, String toolBarTitle, int titleAlpha) {
            super(superState);
            this.mVisible = visible;
            this.mToolbarTitle = toolBarTitle;
            this.mTitleAlpha = titleAlpha;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mVisible ? 1 : 0));
            out.writeString(mToolbarTitle);
            out.writeInt(mTitleAlpha);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public static <V extends View> MergedAppBarLayoutBehavior from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof MergedAppBarLayoutBehavior)) {
            throw new IllegalArgumentException("The view is not associated with " +
                    "MergedAppBarLayoutBehavior");
        }
        return (MergedAppBarLayoutBehavior) behavior;
    }
}
