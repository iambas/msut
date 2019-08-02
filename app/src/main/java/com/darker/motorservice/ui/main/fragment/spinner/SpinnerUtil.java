package com.darker.motorservice.ui.main.fragment.spinner;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

/**
 * Created by Darker on 27/11/60.
 */

public class SpinnerUtil {

    public static Spinner getSpinner(View view, int id, List<String> list){
        Spinner spinner = (Spinner) view.findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }
}
