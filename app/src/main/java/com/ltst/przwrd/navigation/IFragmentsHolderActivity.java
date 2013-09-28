package com.ltst.przwrd.navigation;

import javax.annotation.Nonnull;

public interface IFragmentsHolderActivity
{
    public void selectNavigationFragmentByPosition(int position);
    public void selectNavigationFragmentByClassname(@Nonnull String fragmentId);
    public String getPositionText();
    public String getScoreText();
    public boolean getFbSwitch();
    public boolean getVkSwitch();
    public boolean getIsTablet();
}
