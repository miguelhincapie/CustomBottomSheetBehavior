# CustomBottomSheetBehavior like Google Maps for Android [ ![Download](https://api.bintray.com/packages/mahc/maven/CustomBottomSheetBehavior/images/download.svg) ](https://bintray.com/mahc/maven/CustomBottomSheetBehavior/_latestVersion)
Android Studio Project using Support Library focused on get Google Maps 3 states behavior including background image parallax and toolbars animations.

## Installation
In your module/project build.gradle file add<br>
```
dependencies {
   ...
   implementation 'com.mahc.custombottomsheetbehavior:googlemaps-like:0.9.1'
}
```

## Usage
[![CustomBottomSheetBehavior](https://raw.githubusercontent.com/akan44/CustomBottomSheetBehavior/master/CustomBottomSheetBehaviorLikeGoogleMaps3states.gif)]<br><br>

In a nearly future I'm going to create a wiki explaining how to use, until then take a look at the example (app module).<br><br>
Check the following files
```
activity_main.xml  (is like a template)
activity_main.java  (you can see how to listen for states)
styles.xml  (just the line <style name="AppTheme.NoActionBar">)
AndroidManifest.xml  (just the line android:theme="@style/AppTheme.NoActionBar")
``` 


## What I did?
1. I took the code from BottomSheetBehavior.java from Support Library 23.4.0 and added one state for anchor mode, so now you have:<br>
STATE_HIDDEN<br>
STATE_COLLAPSED<br>
STATE_DRAGGING<br>
STATE_ANCHOR_POINT<br>
STATE_EXPANDED.<br><br>

2. Created Behaviors for FAB, ToolBars and ImageView.


## Motivation
Spend more than 3 days looking snipet of code or stackoverflow answers about it with no luck.

## Current state
Trying to help/close issues.

## Contributing
Do you want to help?
Wanna improve it?
Go ahead! you can start in issues page<br>

## Question about it in StackOverflow

If you like this project give me a vote up at:<br> [Sliding up image with Official Support Library 23.x.+ bottomSheet like google maps](http://stackoverflow.com/q/37335366/1332549).<br>
Related questions:<br>
[Android Support BottomSheetBehavior additional anchored state](http://stackoverflow.com/questions/36963798/android-support-bottomsheetbehavior-additional-anchored-state)<br>
[How to mimic Google Maps' bottom-sheet 3 phases behavior?](http://stackoverflow.com/questions/34160423/how-to-mimic-google-maps-bottom-sheet-3-phases-behavior/34176633?noredirect=1#comment56361295_34176633)<br>
[Sliding BottomSheet like google map](http://stackoverflow.com/questions/34310530/sliding-bottomsheet-like-google-map)<br>
[How to create bottom sheet effect which cover full activity when user scrolls up](http://stackoverflow.com/questions/35900862/how-to-create-bottom-sheet-effect-which-cover-full-activity-when-user-scrolls-up)<br>
[How to make custom CoordinatorLayout.Behavior with parallax scrolling effect for google MapView?](http://stackoverflow.com/questions/33945085/how-to-make-custom-coordinatorlayout-behavior-with-parallax-scrolling-effect-for)<br>
[How to handle issues of the new bottom sheet of the support/design library?](http://stackoverflow.com/questions/35971546/how-to-handle-issues-of-the-new-bottom-sheet-of-the-support-design-library)<br>
[Open an activity or fragment with Bottom Sheet Deep Linking](http://stackoverflow.com/questions/34243928/open-an-activity-or-fragment-with-bottom-sheet-deep-linking)<br>
[BottomSheetDialogFragment - How to set expanded height (or min top offset)](http://stackoverflow.com/questions/36030879/bottomsheetdialogfragment-how-to-set-expanded-height-or-min-top-offset)

## Credits
Giving thx to @akaN44 and @vit001 for their contribution :D.

## License
Licensed under the Apache License, Version 2.0
<br><br>
The true is I don't care about license... JUST USE IT, improve it if you can and give me vote up in stack overflow :D.


<a href="http://stackoverflow.com/users/1332549/miguelhincapiec">
<img src="http://stackoverflow.com/users/flair/1332549.png" width="208" height="58" alt="profile for MiguelHincapieC at Stack Overflow, Q&amp;A for professional and enthusiast programmers" title="profile for MiguelHincapieC at Stack Overflow, Q&amp;A for professional and enthusiast programmers">
</a>
