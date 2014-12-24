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
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
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

    private ColorDrawable clearBackground;

    private LruCache<Point, Drawable> pageBackgrounds = new LruCache<Point, Drawable>(15) {
        @Override
        protected Drawable create(final Point page) {
            Fragment fragment = rows.get(page.y).getColumn(page.x);
            if (fragment instanceof LectureCardFragment) {
                TransitionDrawable background = new TransitionDrawable(new Drawable[] {
                        defaultBackground,
                        new ColorDrawable(((LectureCardFragment) fragment).getLectureTrackColor())
                });

                pageBackgrounds.put(page, background);
                notifyPageBackgroundChanged(page.y, page.x);
                background.startTransition(TRANSITION_DURATION_MILLIS);
            }

            return GridPagerAdapter.BACKGROUND_NONE;
        }
    };

    public LectureGridPagingAdapter(Context context, FragmentManager fm, List<Lecture> now,
                                    List<Lecture> nextHighlights, List<Lecture> nextAllRooms) {
        super(fm);
        this.context = context;
        rows = new ArrayList<Row>();
        defaultBackground = new ColorDrawable(R.color.dark_grey);
        clearBackground = new ColorDrawable(android.R.color.transparent);

        initAdapter(now, nextHighlights, nextAllRooms);
    }

    private void initAdapter(List<Lecture> now, List<Lecture> nextHighlights, List<Lecture> nextAllRooms) {
        if (now.size() == 0) {
            rows.add(new Row(cardFragment(R.string.card_no_running_lectures_title, R.string.card_no_running_lectures_description)));
        } else {

        }

        if (nextHighlights.size() > 0) {
            Fragment[] fragments = new Fragment[nextHighlights.size() + 1];
            fragments[0] = cardFragment(R.string.card_next_highlight_lectures_title, R.string.card_next_highlight_lectures_description);

            int counter = 1;

            for (Lecture lecture : nextHighlights) {
                fragments[counter] = lectureFragment(lecture);
                ++counter;
            }

            rows.add(new Row(fragments));
        }

        if (nextAllRooms.size() == 0) {
            rows.add(new Row(cardFragment(R.string.card_no_next_lectures_title, R.string.card_no_next_lectures_description)));
        } else {
            Fragment[] fragments = new Fragment[nextAllRooms.size() + 1];
            fragments[0] = cardFragment(R.string.card_next_lectures_title, R.string.card_description_swipe_for_lectures);

            int counter = 1;

            for (Lecture lecture : nextAllRooms) {
                fragments[counter] = lectureFragment(lecture);
                ++counter;
            }

            rows.add(new Row(fragments));
        }
    }

    private CardFragment cardFragment(int titleRes, int textRes) {
        Resources res = context.getResources();
        CardFragment fragment = CardFragment.create(res.getText(titleRes), res.getText(textRes));
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.card_margin_bottom));

        return fragment;
    }

    private LectureCardFragment lectureFragment(Lecture lecture) {
        Resources res = context.getResources();
        LectureCardFragment fragment = LectureCardFragment.create(lecture);
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
        return defaultBackground;
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        return pageBackgrounds.get(new Point(column, row));
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
