# CustomBottomSheetBehavior like Google Maps for Android
Simple Android Studio Project giving an Activity and a "CoordinatorLayout.Behavior<V>" extended trying 
to get same 3 states behavior than Google Maps.

## Example
[![CustomBottomSheetBehavior](https://raw.githubusercontent.com/miguelhincapie/CustomBottomSheetBehavior/master/CustomBottomSheetBehaviorLikeGoogleMaps3states.gif)]

## Motivation
Spend more than 2 days looking snipet of code or stackoverflow answers about it with no lucky.

## What I did?
I took the code from BottomSheetBehavior.java from Support Library 23.4.0 and added one state for anchor mode, so now you have:<br>
STATE_HIDDEN<br>
STATE_COLLAPSED<br>
STATE_DRAGGING<br>
STATE_ANCHOR_POINT<br>
STATE_EXPANDED.<br><br>

You can use `setBottomSheetCallback` like you do in original `BottomSheetBehavior`<br><br>
The XML (without anything about parallax image like google maps) looks like:<br>
```xml
<CoordinatorLayout>
    <FrameLayout>
    <AppBarLayout>
        <CollapsingToolbarLayout>
            <Toolbar>
    <NestedScrollView>
        <LinearLayout>
```



## Help or improvements
Do you want to help?
Wanna improve it?
Go ahead!

## Current state
Working on get parallax image behavior and toolbar like google maps.

## Question about it in StackOverflow

[Android Support BottomSheetBehavior additional anchored state](http://stackoverflow.com/questions/36963798/android-support-bottomsheetbehavior-additional-anchored-state)<br>
[How to mimic Google Maps' bottom-sheet 3 phases behavior?](http://stackoverflow.com/questions/34160423/how-to-mimic-google-maps-bottom-sheet-3-phases-behavior/34176633?noredirect=1#comment56361295_34176633)<br>
[Sliding BottomSheet like google map](http://stackoverflow.com/questions/34310530/sliding-bottomsheet-like-google-map)<br>
[How to create bottom sheet effect which cover full activity when user scrolls up](http://stackoverflow.com/questions/35900862/how-to-create-bottom-sheet-effect-which-cover-full-activity-when-user-scrolls-up)<br>
[How to make custom CoordinatorLayout.Behavior with parallax scrolling effect for google MapView?](http://stackoverflow.com/questions/33945085/how-to-make-custom-coordinatorlayout-behavior-with-parallax-scrolling-effect-for)<br>
[How to handle issues of the new bottom sheet of the support/design library?](http://stackoverflow.com/questions/35971546/how-to-handle-issues-of-the-new-bottom-sheet-of-the-support-design-library)<br>
[Open an activity or fragment with Bottom Sheet Deep Linking](http://stackoverflow.com/questions/34243928/open-an-activity-or-fragment-with-bottom-sheet-deep-linking)<br>
[BottomSheetDialogFragment - How to set expanded height (or min top offset)](http://stackoverflow.com/questions/36030879/bottomsheetdialogfragment-how-to-set-expanded-height-or-min-top-offset)


[My Profile](http://stackoverflow.com/users/1332549/miguelhincapiec?tab=profile)
