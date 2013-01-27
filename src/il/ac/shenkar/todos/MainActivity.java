/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package il.ac.shenkar.todos;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/** 
 * Main application activity 
 */
public class MainActivity extends Activity
	implements OnClickListener, OnItemClickListener, OnTouchListener,
	ItemListBaseAdapter.NoticeAlarmListener, NotificationAlertDialog.SetAlarmListener
{
	// custom base adapter
	static ItemListBaseAdapter	 	adapter;
	static ImageView				noTasksImage;
	// flag to determine if shake gestures are allowed
	static boolean					shakeGestures;
	// instance of the singleton class
	private TaskList				taskListModel;
	private EditText 				titleTextField;
	// popup window object for setting task notification alarm
	private NotificationAlertDialog notificationPopup;
	private AlertDialogs			alertDialog;
	// holds current list view item position
	private int 					currentPosition;
	// before task deletion, saves it in case of undone
	private Task					taskToUndo;
	// for undo button purposes
	private View 					viewContainer;
	// used for google analytics purposes
	private Tracker 				myTracker;
	// used for google analytics purposes
	private GoogleAnalytics 		myInstance; 
	// the user entered text in alert dialog
	private RelativeLayout 			getLinearLayout;
	private SensorManager 			mSensorManager;
	private ShakeEventListener 		mSensorListener;
	// manages sounds in the application
	private MediaPlayerHandler 		mediaPlayer;
	private TaskAlarms				taskAlarms;
	private ShareProvider			shareProvider;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// disable the window title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		taskListModel = TaskList.getSingletonObject(this);
		mediaPlayer = new MediaPlayerHandler(this);
		shakeGestures = true;

		ListView listView = (ListView) findViewById(R.id.listV_main);
		adapter = new ItemListBaseAdapter(this);
		// set our custom adapter for the listView
		listView.setAdapter(adapter);

		titleTextField = (EditText) findViewById(R.id.edit_message);
		Button addButton = (Button) findViewById(R.id.btnAdd);
		addButton.setOnClickListener(this);
		getLinearLayout = (RelativeLayout) findViewById(R.id.LinearLayout01);
		getLinearLayout.setOnTouchListener(this);
		viewContainer = findViewById(R.id.undobar);		
		myInstance = GoogleAnalytics.getInstance(getApplicationContext());
		taskToUndo = new Task();
		noTasksImage = (ImageView) findViewById(R.id.no_tasks_image);
		noTasksImage.setVisibility(View.GONE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeEventListener();

		// Placeholder tracking ID.
		myTracker = myInstance.getTracker(Utils.GOOGLE_ANALYTICS_CODE);
		// Set newTracker as the default tracker globally.
		myInstance.setDefaultTracker(myTracker); 
		// prints actions to the logcat
		myInstance.setDebug(true);
		taskListModel.getDataBase().open();

		mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() 
		{
			public void onShake() 
			{
				if (shakeGestures == true)
				{
					shakeGestures = false;
					alertDialog = new AlertDialogs();
					alertDialog.setArguments(getArguments(Utils.DIALOG_YES_NO_MESSAGE,currentPosition,Utils.DELETE_ALL_ALERT));
					alertDialog.show(getFragmentManager(),"ShowAlertDialog");
					adapter.notifyDataSetChanged();
				}
			}
		});
		// Create a ListView-specific touch listener.
		SwipeListViewTouchListener touchListener = 
				new SwipeListViewTouchListener(listView, new SwipeListViewTouchListener.OnSwipeCallback()
				{
					// when the user swipes a list view item
					@Override
					public void onSwipeLeft(ListView listView, int[] reverseSortedPositions)
					{
						// Intentionally empty
					}
					@Override
					public void onSwipeRight(ListView listView, int[] reverseSortedPositions)
					{
						for (int position : reverseSortedPositions)
						{
							taskToUndo = taskListModel.getTaskAt(position);
							taskListModel.removeTask(position);
							taskAlarms.disableTaskAlerts(taskToUndo);
							mediaPlayer.playAudio(Utils.DELETE_SOUND); 
							adapter.notifyDataSetChanged();
							showUndo(viewContainer);
						}
					}
				});

		listView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during ListView scrolling,
		// we don't look for swipes.
		listView.setOnScrollListener(touchListener.makeScrollListener());
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);
		// retrieve all the application data stored in the data base 
		taskListModel.retrieveData();
		// add software menu button support
		Utils.addLegacyOverflowButton(this.getWindow());
		// show/hide no tasks image
		Utils.checkAlertImageTrigger(taskListModel.getTasks().size());
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// reopen database 
		taskListModel.getDataBase().open();
		// register sensors resources
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause()
	{
		// release sensors resources
		mSensorManager.unregisterListener(mSensorListener);
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		// free database and media player resources 
		taskListModel.getDataBase().close();
		mediaPlayer.killMediaPlayer();
	}

	// when clicking the add task button
	@Override
	public void onClick(View v)
	{
		long opt_value = 1;
		// Where myTracker is an instance of Tracker.
		// count task addition to the google analytics
		myTracker.trackEvent("ui_action", "button_press", "add_task_button", opt_value);
		// Assigns user entered text to "taskTitleStr" and adds a new task 
		String taskTitleStr = titleTextField.getText().toString();
		taskListModel.addTask(taskTitleStr, Utils.DEFAULT_DESCRIPTION, Utils.DEFUALT_NOTIFICATION);
		titleTextField.setText("");
		adapter.notifyDataSetChanged();
	}

	// on list view item click
	@Override
	public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3)
	{
		alertDialog = new AlertDialogs();
		alertDialog.setArguments(getArguments(Utils.DIALOG_TASK_DETAILS,position,""));
		alertDialog.show(getFragmentManager(),"ShowAlertDialog");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		// if a touch was made on the screen outside the keyboard 
		if (v == getLinearLayout)
		{
			// hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(titleTextField.getWindowToken(), 0);
			return true;
		}
		return false;
	} 

	public void onLoad(long loadTime) 
	{
		// track application load time in google analytics
		myTracker.trackTiming("resources", loadTime, "high_scores", null);
	}

	@Override
	public void onStart() 
	{
		super.onStart();
		// reallocate resources
		taskListModel.getDataBase().open();
		EasyTracker.getInstance().activityStart(this); 
		// location provider is enabled each time the activity resumes from the stopped state.
		taskAlarms = new TaskAlarms(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	// creates the task properties menu on long click
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		shareProvider = new ShareProvider(menu);
		// stores current item position
		int position = info.position;
		// current task description
		String taskDescription = taskListModel.getTaskAt(position).getTaskDescription();
		// is task marked as important
		boolean isImportant = taskListModel.getTaskAt(position).isImportant();

		// checks if description is empty
		if (taskDescription.equals(Utils.DEFAULT_DESCRIPTION)) 
		{
			MenuItem item = menu.findItem(R.id.editDescription);
			item.setTitle("Add Task Description");
		}

		if (isImportant) 
		{
			MenuItem item = menu.findItem(R.id.markImportant);
			item.setTitle("Mark As Unimportant");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
		// holds current item position
		int index = info.position;
		currentPosition = index;    

		// determines which item was selected at the menu
		switch (menuItem.getItemId()) 
		{
		case R.id.editTitle:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(getArguments(Utils.DIALOG_TEXT_ENTRY,index,Utils.EDIT_TITLE));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			return true;

		case R.id.editDescription:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(getArguments(Utils.DIALOG_LONG_TEXT_ENTRY,index,Utils.EDIT_DESCRIPTION));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			return true;

		case R.id.markImportant:
			// mark the task as important/unimportant
			boolean important = taskListModel.getTaskAt(index).isImportant();
			taskListModel.getTaskAt(index).setImportant(!important);
			taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(index));
			adapter.notifyDataSetChanged();
			return true;

		case R.id.deleteTask:
			taskToUndo = taskListModel.getTaskAt(index);			
			taskListModel.removeTask(index);
			mediaPlayer.playAudio(Utils.DELETE_SOUND); 
			taskAlarms.disableTaskAlerts(taskToUndo);
			adapter.notifyDataSetChanged();
			showUndo(viewContainer);
			return true;

		case R.id.setLocation:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(getArguments(Utils.DIALOG_TEXT_ENTRY,index,Utils.SET_LOCATION));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			return true;

		case R.id.menu_item_share:
			// sets the information to be sent to the share location, e.g. gmail, message etc'
			Task taskToShare = taskListModel.getTaskAt(index);
			shareProvider.makeShareMenu(taskToShare.getTaskTitle(), taskToShare.getTaskDescription());

		default:
			return super.onContextItemSelected(menuItem);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		new MenuInflater(getApplicationContext()).inflate(R.menu.application_menu, menu);       
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.help:
			//showHelpScreen();
			return true;

		case R.id.delete_all:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(
					getArguments(Utils.DIALOG_YES_NO_MESSAGE,currentPosition,Utils.DELETE_ALL_ALERT));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			adapter.notifyDataSetChanged();
			return true;

		case R.id.save_to_server:
			return true;	

		case R.id.load_from_server:
			return true;

		default: 
			return true;
		}
	}

	/** 
	 * When user clicks on alarm clock button.
	 * (callback from the list adapter)
	 */
	@Override
	public void onAlarmClick(int position)
	{
		// checks if alarm button was clicked to turn it on or off
		if (taskListModel.getTaskAt(position).getAlarmImage() == Utils.ALARM_ON_IMAGE)
		{
			taskListModel.getTaskAt(position).setAlarmImage(Utils.ALARM_OFF_IMAGE);

			if (! taskListModel.getTaskAt(position).getDate().equals(Utils.DEFUALT_NOTIFICATION))
			{
				// set to no notification and cancel alarm
				taskListModel.getTaskAt(position).setDate(Utils.DEFUALT_NOTIFICATION);
				taskAlarms.cancelAlarm(taskListModel.getTaskAt(position).getId(), taskListModel.getTaskAt(position).getTaskTitle());
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			return;
		}

		// create and show the notification alert dialog
		currentPosition = position;
		notificationPopup = new NotificationAlertDialog();
		notificationPopup.show(getFragmentManager(),"notificationAlertDialog");
		return;
	}

	/**
	 * When user clicks OK button on set notification alert dialog 
	 */
	@Override
	public void onSetAlarmPositiveClick(DialogFragment dialog)
	{
		// get user selected time and date from the date and time pickers
		long repeatAlarmInterval;
		Calendar calendar = notificationPopup.getSelectedTimeAndDate();
		boolean repeating = notificationPopup.getAlarmInterval();
		String fullDate = calendar.getTime().toString();

		if (repeating == true)
		{
			// get the user selected time interval
			repeatAlarmInterval = notificationPopup.getSelectedInterval();
		}
		else
		{
			// indicates no interval
			repeatAlarmInterval = -1;
		}

		// update task data
		taskListModel.getTaskAt(currentPosition).setDate(fullDate);
		taskListModel.getTaskAt(currentPosition).setAlarmImage(Utils.ALARM_ON_IMAGE);
		taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(currentPosition));
		// set the alarm for notification
		taskAlarms.setAlarm(repeatAlarmInterval,calendar,currentPosition);
		adapter.notifyDataSetChanged();
	}

	/** 
	 * Shows the undo task deletion window.
	 * 
	 * @param viewContainer
	 */
	public static void showUndo(final View viewContainer) 
	{
		viewContainer.setVisibility(View.VISIBLE);
		viewContainer.setAlpha(1);
		viewContainer.animate().alpha(0.4f).setDuration(5000)
		.withEndAction(new Runnable() 
		{
			@Override
			public void run() 
			{
				viewContainer.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * when user clicks undone (after task deletion).
	 *  
	 * @param view
	 */
	public void undoTaskDeletion(View view)
	{
		viewContainer.setVisibility(View.GONE);
		// restore the deleted task
		taskListModel.addExistingTask(taskToUndo);
		adapter.notifyDataSetChanged();
	}


	/**
	 * Get arguments to send to alert dialogs
	 * 
	 * @param id - alert dialog type
	 * @param position - task position
	 * @param title - alert dialog title
	 * @return bundle with arguments
	 */
	public Bundle getArguments(int id, int position, String title)
	{
		Bundle args = new Bundle();
		args.putInt("id",id);
		args.putInt("position",position);
		args.putString("dialogTitle",title);
		return args;
	}
}