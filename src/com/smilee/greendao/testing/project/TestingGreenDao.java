package com.smilee.greendao.testing.project;

import java.text.DateFormat;
import java.util.Date;
import smilee.testing.greendao.DaoMaster;
import smilee.testing.greendao.DaoMaster.DevOpenHelper;
import smilee.testing.greendao.DaoSession;
import smilee.testing.greendao.TeammateData;
import smilee.testing.greendao.TeammateDataDao;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class TestingGreenDao extends Activity implements TextWatcher, OnCheckedChangeListener, OnItemClickListener {

	private static final String TAG = "TestingGreenDao";
	private Context context;
	private SQLiteDatabase db;
	private EditText etName, etAge, etBirth, etSeniority;
	private SimpleCursorAdapter adapter;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private TeammateDataDao mTeammateDataDao;
	private RadioGroup mRadioGroup;
	private ListView lvDataList;
	private Cursor cursor;
	private String gender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = TestingGreenDao.this;
		setupDb();
	}

	private void setupDb() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
				"smileetestgreendao-db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		mTeammateDataDao = daoSession.getTeammateDataDao();
		initView();
	}

	private void initView() {
		lvDataList = (ListView) findViewById(R.id.lvDataList);
		lvDataList.setDividerHeight(3);
		lvDataList.setClickable(true);
		initAdapter();
		lvDataList.setAdapter(adapter);
		etName = (EditText) findViewById(R.id.etName);
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		etAge = (EditText) findViewById(R.id.etAge);
		etBirth = (EditText) findViewById(R.id.etBirth);
		etSeniority = (EditText) findViewById(R.id.etSeniority);
		addUiListeners();
	}

	private void initAdapter() {
		String[] textColumn = { TeammateDataDao.Properties.Name.columnName,
				TeammateDataDao.Properties.Gender.columnName,
				TeammateDataDao.Properties.Age.columnName,
				TeammateDataDao.Properties.Birthday.columnName,
				TeammateDataDao.Properties.Seniority.columnName,
				TeammateDataDao.Properties.AddDate.columnName };
		cursor = db.query(mTeammateDataDao.getTablename(),
				mTeammateDataDao.getAllColumns(), null, null, null, null, null);
		int[] to = { R.id.tvName, R.id.tvGender, R.id.tvAgeValue, R.id.tvBirth,
				R.id.tvSeniorityValue, R.id.tvAddingDate };
		adapter = new SimpleCursorAdapter(context, R.layout.item_data_list,
				cursor, textColumn, to, 0);
	}

	protected void addUiListeners() {
		etName.addTextChangedListener(this);
		etAge.addTextChangedListener(this);
		etBirth.addTextChangedListener(this);
		etSeniority.addTextChangedListener(this);
		mRadioGroup.setOnCheckedChangeListener(this);
		lvDataList.setOnItemClickListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_add_data:
			Log.d(TAG, "click add btn");
			if (isFieldAllFilled()) {
				addTeammateData();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rbGenderM:
			gender = "Man";
			break;
		case R.id.rbGenderW:
			gender = "Woman";
			break;
		default:
			gender = null;
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position,
			long id) {
		mTeammateDataDao.deleteByKey(id);
		Toast.makeText(getApplicationContext(),
				"Data ID: " + id + " was deleted.",
				Toast.LENGTH_SHORT).show();
		getCursorAndRefreshListView();
	}
	
	private void getCursorAndRefreshListView() {
		// Get cursor and refresh data display on list view.
		cursor = db.query(mTeammateDataDao.getTablename(),
				mTeammateDataDao.getAllColumns(), null, null, null, null, null);
		adapter.swapCursor(cursor);
		adapter.notifyDataSetChanged();
	}
	
	private String[] getText() {
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM);
		String addingDate = " - Added on " + df.format(new Date());
		String[] text = { etName.getText().toString(), gender,
				etAge.getText().toString(), etBirth.getText().toString(),
				etSeniority.getText().toString(), addingDate };
		return text;
	}

	private void addTeammateData() {
		Log.d(TAG, "addTeammateData()");
		String[] data = getText();
		// data insert
		TeammateData mTeammateData = new TeammateData(null, data[0], data[1],
				Integer.parseInt(data[2]), data[3], Integer.parseInt(data[4]),
				data[5]);
		mTeammateDataDao.insert(mTeammateData);
		Toast.makeText(context,
				"Inserted new teammate Data, ID: " + mTeammateData.getId(),
				Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Inserted new teammate Data, ID: " + mTeammateData.getId());
		getCursorAndRefreshListView();
		mRadioGroup.clearCheck();
		etName.setText("");
		etAge.setText("");
		etBirth.setText("");
		etSeniority.setText("");
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
	}
	
	private boolean isFieldAllFilled() {
		// Check all edit text or radio button fields are filled or not.
		String[] text = getText();
		int count = 0;
		for (String each : text) {
			if (each != null && !each.equals("")) {
				// Check edit text field value.
				count++;
				continue;
			} else {
				Toast.makeText(TestingGreenDao.this,
						"Sorry, these fields can not be empty.",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

}
