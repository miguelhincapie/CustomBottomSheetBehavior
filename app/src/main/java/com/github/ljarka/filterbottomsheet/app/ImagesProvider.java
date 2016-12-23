package com.github.ljarka.filterbottomsheet.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagesProvider {

    private List<String> images = Arrays.asList(
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/carrot-2-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/cherry-2-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/colddrink-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/crop-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/icecream-4-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/water-ice-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/tomato-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/toffee-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/teapot-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/tea-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/sweet-icon.png",
            "http://icons.iconarchive.com/icons/graphicloads/food-drink/128/strawberry-icon.png"

    );


    public List<String> getImages() {
        List<String> list = new ArrayList<>();
        list.addAll(images);
        list.addAll(images);
        list.addAll(images);
        return list;
    }
}
