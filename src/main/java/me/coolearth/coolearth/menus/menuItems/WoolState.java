package me.coolearth.coolearth.menus.menuItems;

public enum WoolState {
    NOT_PURCHASABLE,
    PURCHASABLE,
    BOUGHT,
    BEDS;

    WoolState() {}

    public static WoolState get(boolean hasMoney, boolean hasBeds) {
        if (hasBeds) {
            return BEDS;
        } else {
            if (hasMoney) {
                return PURCHASABLE;
            } else {
                return NOT_PURCHASABLE;
            }
        }
    }
}
