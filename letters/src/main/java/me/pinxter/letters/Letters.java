package me.pinxter.letters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Letters {

    final static public String TAG = "Letters";

    private Context context;
    private OnSelect select;
    private List<Field> fields = new ArrayList<>();
    private LinkedHashMap<Integer, String> letters = new LinkedHashMap<>();

    private String activeLetter;
    private int activeIndex;

    private Handler handler = new Handler();
    private Runnable runnableSearch = new Runnable() {
        @Override
        public void run() {
            if (select != null) {
                select.onSelect(activeIndex, activeLetter);
            }
        }
    };

    public interface OnSelect {
        void onSelect(int index, String letter);
    }

    public void setOnSelect(OnSelect select) {
        this.select = select;
    }

    public String getLetter(int position) {
        if (letters.containsKey(position)) {
            return letters.get(position);
        }
        return "";
    }

    private List<Field> getFields(Object object) {
        if (fields.isEmpty()) {
            for(Field field : object.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    public Letters(Context context, String column, List<Object> list) {
        this.context = context;
        letters = getLetters(column, list);
    }

    private LinkedHashMap<Integer, String> getLetters(String column, List<Object> list) {

        TreeMap<String, Integer> lettersList = new TreeMap<>(new NatSortComparator());

        for (int i = 0; i<list.size(); i++) {
            for (Field field : getFields(list.get(i))) {
                if (field.getName().equals(column)) {
                    try {
                        Object result = field.get(list.get(i));
                        if (result != null) {
                            String value = result.toString();
                            if (value.equals("")) {
                                continue;
                            }
                            String letter = String.valueOf(value.toUpperCase().charAt(0));
                            if (!lettersList.containsKey(letter)) {
                                lettersList.put(letter, i);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
        LinkedHashMap<Integer, String> letter = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : lettersList.entrySet()) {
            letter.put(entry.getValue(), entry.getKey());
        }
        return letter;
    }

    public LinearLayout getLetterLayout() {

        if (!letters.isEmpty()) {

            int paddingList = dpToPx(7);

            final LinearLayout listm = new LinearLayout(context);
            listm.setOrientation(LinearLayout.VERTICAL);
            listm.setPadding(paddingList, 0, 0, 0);
            listm.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            LinearLayout mainLetterLayout = new LinearLayout(context);
            mainLetterLayout.setVisibility(View.VISIBLE);
            mainLetterLayout.setOrientation(LinearLayout.VERTICAL);
            mainLetterLayout.setGravity(Gravity.END | Gravity.CENTER);
            mainLetterLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            int paddingTop = dpToPx(1);
            int paddingLeft = dpToPx(3);
            int line = dpToPx(1);
            int i = 0;
            final List<Integer> lettersIndex = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : letters.entrySet()) {

                final String letter = entry.getValue();
                lettersIndex.add(entry.getKey());

                TextView textView = new TextView(context);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
                textView.setBackgroundColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                textView.setText(letter);
                textView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);

                listm.addView(textView);

                if (i != letters.size() - 1) {
                    LinearLayout lineL = new LinearLayout(context);
                    lineL.setOrientation(LinearLayout.VERTICAL);
                    lineL.setBackgroundColor(Color.GRAY);
                    lineL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, line));
                    listm.addView(lineL);
                }
                i++;
            }

            final HashMap<String, Integer> values = new HashMap<>();

            listm.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {

                        if (!values.containsKey("height")) {

                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            int top = location[1];

                            values.put("index", -1);
                            values.put("Y", 0);
                            values.put("height-item", listm.getHeight()/letters.size());
                            values.put("top", top);
                            values.put("bottom", top + listm.getHeight());
                        }

                        final int Y = (int) event.getRawY();

                        if (!values.get("Y").equals(Y) && values.get("top") < Y && values.get("bottom") > Y) {
                            values.put("Y", Y);
                            int index = (Y - values.get("top")) / values.get("height-item");
                            if (!values.get("index").equals(index)) {
                                values.put("index", index);
                                if (select != null && lettersIndex.size() != index) {
                                    if (!letters.get(lettersIndex.get(index)).equals(activeLetter)) {

                                        activeIndex = lettersIndex.get(index);
                                        activeLetter = letters.get(lettersIndex.get(index));

                                        handler.removeCallbacks(runnableSearch);
                                        handler.postDelayed(runnableSearch, 50);
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            });
            mainLetterLayout.addView(listm);
            return mainLetterLayout;
        }
        return null;
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
