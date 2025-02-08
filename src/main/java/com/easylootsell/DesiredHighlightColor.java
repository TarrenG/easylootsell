package com.easylootsell;

import java.awt.Color;

public enum DesiredHighlightColor {
    PINK,
    BLUE,
    YELLOW,
    GREEN,
    NONE,
    ;

    public Color toJavaColor() {
        switch (this) {
            case PINK:
                return Color.PINK;
            case BLUE:
                return Color.BLUE;
            case YELLOW:
                return Color.YELLOW;
            default:
                return Color.GREEN;
        }
    }
}
