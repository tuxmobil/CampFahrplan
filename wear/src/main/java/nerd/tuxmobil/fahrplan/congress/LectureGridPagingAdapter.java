/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nerd.tuxmobil.fahrplan.congress;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs fragments as requested by the GridViewPager. For each row a different background is
 * provided.
 * <p>
 * Always avoid loading resources from the main thread. In this sample, the background images are
 * loaded from an background task and then updated using {@link #notifyRowBackgroundChanged(int)}
 * and {@link #notifyPageBackgroundChanged(int, int)}.
 */
public class LectureGridPagingAdapter extends FragmentGridPagerAdapter {

    private static final int TRANSITION_DURATION_MILLIS = 100;

    private final Context context;

    private List<Row> rows;

    private ColorDrawable defaultBackground;

    public LectureGridPagingAdapter(Context context, FragmentManager fm, List<Lecture> now,
                                    List<Lecture> nextHighlights, List<Lecture> nextAllRooms) {
        super(fm);
        this.context = context;

        rows = new ArrayList<Row>();

        rows.add(new Row(cardFragment(R.string.app_name, R.string.app_name)));
//        rows.add(new Row(cardFragment(R.string.about_title, R.string.about_text)));
//        rows.add(new Row(
//                cardFragment(R.string.cards_title, R.string.cards_text),
//                cardFragment(R.string.expansion_title, R.string.expansion_text)));
//        rows.add(new Row(
//                cardFragment(R.string.backgrounds_title, R.string.backgrounds_text),
//                cardFragment(R.string.columns_title, R.string.columns_text)));
//        rows.add(new Row(cardFragment(R.string.dismiss_title, R.string.dismiss_text)));
        defaultBackground = new ColorDrawable(R.color.dark_grey);
    }

    LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
        @Override
        protected Drawable create(final Integer row) {

//                    TransitionDrawable background = new TransitionDrawable(new Drawable[] {
//                            defaultBackground,
//                            new ColorDrawable()
//                    });
//                    mRowBackgrounds.put(row, background);
//                    notifyRowBackgroundChanged(row);
//                    background.startTransition(TRANSITION_DURATION_MILLIS);

            return defaultBackground;
        }
    };

    private Fragment cardFragment(int titleRes, int textRes) {
        Resources res = context.getResources();
        CardFragment fragment = CardFragment.create(res.getText(titleRes), res.getText(textRes));
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }

    /** A convenient container for a row of fragments. */
    private class Row {
        final List<Fragment> columns = new ArrayList<Fragment>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = rows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return mRowBackgrounds.get(row);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        return GridPagerAdapter.BACKGROUND_NONE;
//        return mPageBackgrounds.get(new Point(column, row));
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return rows.get(rowNum).getColumnCount();
    }
}
