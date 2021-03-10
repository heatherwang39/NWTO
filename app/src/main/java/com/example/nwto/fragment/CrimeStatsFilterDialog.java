package com.example.nwto.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.nwto.R;

import java.util.Arrays;
import java.util.List;

public class CrimeStatsFilterDialog extends DialogFragment {
    private static final String TAG = "TAG: " + CrimeStatsFilterDialog.class.getSimpleName();

    private NumberPicker mDivisionPicker, mNeighbourhoodPicker;
    private Spinner mPremiseTypeSpinner, mCrimeTypeSpinner;
    private SeekBar mDateSeekBar, mRadiusSeekBar;
    private TextView mDateText, mRadiusText;
    private Button mCancel, mSave;

    public static CrimeStatsFilterDialog display(FragmentManager fragmentManager) {
        CrimeStatsFilterDialog crimeStatsFilterDialog = new CrimeStatsFilterDialog();
        crimeStatsFilterDialog.show(fragmentManager, TAG);
        return crimeStatsFilterDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_crimestats_preference, container, false);

        // Initializes Layout Variables
        mDivisionPicker = (NumberPicker) view.findViewById(R.id.preference_divisionPicker);
        mNeighbourhoodPicker = (NumberPicker) view.findViewById(R.id.preference_neighbourhoodPicker);
        mPremiseTypeSpinner = (Spinner) view.findViewById(R.id.preference_premiseType_spinner);
        mCrimeTypeSpinner = (Spinner) view.findViewById(R.id.preference_crimeType_spinner);
        mDateSeekBar = (SeekBar) view.findViewById(R.id.preference_date_seekBar);
        mDateText = (TextView) view.findViewById(R.id.preference_date_text);
        mRadiusSeekBar = (SeekBar) view.findViewById(R.id.preference_radius_seekBar);
        mRadiusText = (TextView) view.findViewById(R.id.preference_radius_text);
        mCancel = (Button) view.findViewById(R.id.preference_cancel_button);
        mSave = (Button) view.findViewById(R.id.preference_save_button);

        // Initializes Division & Neighbourhood Pickers
        String[] policeDivisions = new String[] {"00", "11", "12", "13", "14", "22", "23", "31", "32", "33", "41", "42", "43", "51", "52", "53", "54", "55", "58"};
        mDivisionPicker.setMinValue(0);
        mDivisionPicker.setMaxValue(policeDivisions.length - 1);
        mDivisionPicker.setDisplayedValues(policeDivisions);


        String[] neighbourhoodNumbs = getNeighbourhoodNames();
        mNeighbourhoodPicker.setMinValue(0);
        mNeighbourhoodPicker.setMaxValue(neighbourhoodNumbs.length - 1);
        mNeighbourhoodPicker.setDisplayedValues(neighbourhoodNumbs);

        // Initializes Premise & Crime Type Spinners
        String[] premiseTypes = new String[] {"All", "Apartment", "Commercial", "Educational", "House", "Transit", "Outside", "Other"};
        List<String> premiseList = Arrays.asList(premiseTypes);
        ArrayAdapter<String>  premiseAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, premiseList);
        premiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPremiseTypeSpinner.setAdapter(premiseAdapter);

        String[] crimeTypes = new String[] {"All", "Assault", "Auto Theft", "Break and Enter", "Homicide", "Robbery", "Sexual Violation", "Shooting", "Theft Over"};
        List<String> crimeList = Arrays.asList(crimeTypes);
        ArrayAdapter<String>  crimeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, crimeList);
        crimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCrimeTypeSpinner.setAdapter(crimeAdapter);



        return view;
    }

    private String[] getNeighbourhoodNames() {
        String[] neighbourhoodNames = {
                "Agincourt North (129)",
                "Agincourt South-Malvern West (128)",
                "Alderwood (20)",
                "Annex (95)",
                "Banbury-Don Mills (42)",
                "Bathurst Manor (34)",
                "Bay Street Corridor (76)",
                "Bayview Village (52)",
                "Bayview Woods-Steeles (49)",
                "Bedford Park-Nortown (39)",
                "Beechborough-Greenbrook (112)",
                "Bendale (127)",
                "Birchcliffe-Cliffside (122)",
                "Black Creek (24)",
                "Blake-Jones (69)",
                "Briar Hill-Belgravia (108)",
                "Bridle Path-Sunnybrook-York Mills (41)",
                "Broadview North (57)",
                "Brookhaven-Amesbury (30)",
                "Cabbagetown-South St.James Town (71)",
                "Caledonia-Fairbank (109)",
                "Casa Loma (96)",
                "Centennial Scarborough (133)",
                "Church-Yonge Corridor (75)",
                "Clairlea-Birchmount (120)",
                "Clanton Park (33)",
                "Cliffcrest (123)",
                "Corso Italia-Davenport (92)",
                "Danforth (66)",
                "Danforth East York (59)",
                " Don Valley Village (47)",
                "Dorset Park (126)",
                "Dovercourt-Wallace Emerson-Junction (93)",
                "Downsview-Roding-CFB (26)",
                "Dufferin Grove (83)",
                "East End-Danforth (62)",
                "Edenbridge-Humber Valley (9)",
                "Eglinton East (138)",
                "Elms-Old Rexdale (5)",
                "Englemount-Lawrence (32)",
                "Eringate-Centennial-West Deane (11)",
                "Etobicoke West Mall (13)",
                "Flemingdon Park (44)",
                "Forest Hill North (102)",
                "Forest Hill South (101)",
                "Glenfield-Jane Heights (25)",
                "Greenwood-Coxwell (65)",
                "Guildwood (140)",
                "Henry Farm (53)",
                "High Park North (88)",
                "High Park-Swansea (87)",
                "Highland Creek (134)",
                "Hillcrest Village (48)",
                "Humber Heights-Westmount (8)",
                "Humber Summit (21)",
                "Humbermede (22)",
                "Humewood-Cedarvale (106)",
                "Ionview (125)",
                "Islington-City Centre West (14)",
                "Junction Area (90)",
                "Keelesdale-Eglinton West (110)",
                "Kennedy Park (124)",
                "Kensington-Chinatown (78)",
                "Kingsview Village-The Westway (6)",
                "Kingsway South (15)",
                "L'Amoreaux (117)",
                "Lambton Baby Point (114)",
                "Lansing-Westgate (38)",
                "Lawrence Park North (105)",
                "Lawrence Park South (103)",
                "Leaside-Bennington (56)",
                "Little Portugal (84)",
                "Long Branch (19)",
                "Malvern (132)",
                "Maple Leaf (29)",
                "Markland Wood (12)",
                "Milliken (130)",
                "Mimico (includes Humber Bay Shores) (17)",
                "Morningside (135)",
                "Moss Park (73)",
                "Mount Dennis (115)",
                "Mount Olive-Silverstone-Jamestown (2)",
                "Mount Pleasant East (99)",
                "Mount Pleasant West (104)",
                "New Toronto (18)",
                "Newtonbrook East (50)",
                "Newtonbrook West (36)",
                "Niagara (82)",
                "North Riverdale (68)",
                "North St.James Town (74)",
                "O'Connor-Parkview (54)",
                "Oakridge (121)",
                "Oakwood Village (107)",
                "Old East York (58)",
                "Palmerston-Little Italy (80)",
                "Parkwoods-Donalda (45)",
                "Pelmo Park-Humberlea (23)",
                "Playter Estates-Danforth (67)",
                "Pleasant View (46)",
                "Princess-Rosethorn (10)",
                "Regent Park (72)",
                "Rexdale-Kipling (4)",
                "Rockcliffe-Smythe (111)",
                "Roncesvalles (86)",
                "Rosedale-Moore Park (98)",
                "Rouge (131)",
                "Runnymede-Bloor West Village (89)",
                "Rustic (28)",
                "Scarborough Village (139)",
                "South Parkdale (85)",
                "South Riverdale (70)",
                "St.Andrew-Windfields (40)",
                "Steeles (116)",
                "Stonegate-Queensway (16)",
                "Tam O'Shanter-Sullivan (118)",
                "Taylor-Massey (61)",
                "The Beaches (63)",
                "Thistletown-Beaumond Heights (3)",
                "Thorncliffe Park (55)",
                "Trinity-Bellwoods (81)",
                "University (79)",
                "Victoria Village (43)",
                "Waterfront Communities-The Island (77)",
                "West Hill (136)",
                "West Humber-Clairville (1)",
                "Westminster-Branson (35)",
                "Weston (113)",
                "Weston-Pellam Park (91)",
                "Wexford/Maryvale (119)",
                "Willowdale East (51)",
                "Willowdale West (37)",
                "Willowridge-Martingrove-Richview (7)",
                "Woburn (137)",
                "Woodbine Corridor (64)",
                "Woodbine-Lumsden (60)",
                "Wychwood (94)",
                "Yonge-Eglinton (100)",
                "Yonge-St.Clair (97)",
                "York University Heights (27)",
                "Yorkdale-Glen Park (31)",
        };
        return neighbourhoodNames;
    }
}
