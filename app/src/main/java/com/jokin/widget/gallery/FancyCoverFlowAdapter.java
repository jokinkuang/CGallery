/*
 * Copyright 2013 David Schreiber
 *           2013 John Paul Nalog
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.jokin.widget.gallery;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public abstract class FancyCoverFlowAdapter extends BaseAdapter {

    // =============================================================================
    // Supertype overrides
    // =============================================================================

    private int mRealCount;
    public void setRealCount(int count) {
        mRealCount = count;
    }
    public int getRealCount() {
        return mRealCount;
    }

    @Override
    public final View getView(int i, View reusableView, ViewGroup viewGroup) {
        FancyCoverFlow coverFlow = (FancyCoverFlow) viewGroup;

        View wrappedView = null;
        FancyCoverFlowItemWrapper coverFlowItem;

        if (reusableView != null) {
            coverFlowItem = (FancyCoverFlowItemWrapper) reusableView;
            wrappedView = coverFlowItem.getChildAt(0);
            coverFlowItem.removeAllViews();
        } else {
            coverFlowItem = new FancyCoverFlowItemWrapper(viewGroup.getContext());
            coverFlowItem.setCoverFlowGallery(coverFlow);
        }

        wrappedView = this.getCoverFlowItem(i, wrappedView, viewGroup);

        if (wrappedView == null) {
            throw new NullPointerException("getCoverFlowItem() was expected to return a view, but null was returned.");
        }

        // 设置了Item的属性
        final boolean isReflectionEnabled = coverFlow.isReflectionEnabled();
        coverFlowItem.setReflectionEnabled(isReflectionEnabled);

        if(isReflectionEnabled) {
            coverFlowItem.setReflectionGap(coverFlow.getReflectionGap());
            coverFlowItem.setReflectionRatio(coverFlow.getReflectionRatio());
        }


        coverFlowItem.addView(wrappedView);
        coverFlowItem.setLayoutParams(new EcoGallery.LayoutParams(wrappedView.getLayoutParams()));

        return coverFlowItem;
    }

    // =============================================================================
    // Abstract methods
    // =============================================================================

    public abstract View getCoverFlowItem(int position, View reusableView, ViewGroup parent);
}
