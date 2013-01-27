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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/** 
 * Creates an AlertDialog for various purposes 
 */
public class AlertDialogs extends DialogFragment 
{	
	private Context context;
	private int id = 0;
	private int currentPosition = 0;
	private String dialogTitle;
	// layout text views
	private TextView taskDescriptionText;
	private TextView taskNotificationText;
	private TextView taskLocationText;
	private View layout;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		context = getActivity();
		final TaskList taskListModel = TaskList.getSingletonObject(context);
		id =  getArguments().getInt("id");
		currentPosition = getArguments().getInt("position");
		dialogTitle = getArguments().getString("dialogTitle");
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getActivity().getLayoutInflater();

		switch (id)
		{
		case Utils.DIALOG_TASK_DETAILS:
			// retrieve sent arguments
			String title = taskListModel.getTaskAt(currentPosition).getTaskTitle();
			String description = taskListModel.getTaskAt(currentPosition).getTaskDescription();
			String notificationTime = taskListModel.getTaskAt(currentPosition).getDate();
			String location = taskListModel.getTaskAt(currentPosition).getLocation();

			layout = inflater.inflate(R.layout.task_description_popup, null);

			taskNotificationText = (TextView) layout.findViewById(R.id.taskNotification);
			taskDescriptionText = (TextView) layout.findViewById(R.id.taskDescription);
			taskLocationText = (TextView) layout.findViewById(R.id.taskLocation);
			taskNotificationText.setText("Notification Time:\n" + notificationTime);
			taskDescriptionText.setText("Description:\n" + description);
			taskLocationText.setText("Location:\n" + location);
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			builder.setTitle(title)
			.setView(layout)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			});
			return builder.create();

		case Utils.DIALOG_YES_NO_MESSAGE:
			builder.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(dialogTitle)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					taskListModel.deleteAllTasks();
					new MediaPlayerHandler(context).playAudio(Utils.DELETE_SOUND);
					// allow sensor shake gestures
					MainActivity.shakeGestures = true;
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					dialog.cancel();
				}
			});
			return builder.create();

		case Utils.DIALOG_TEXT_ENTRY:
			layout = inflater.inflate(R.layout.alert_dialog_text_entry, null);
			builder.setTitle(dialogTitle)
			.setView(layout)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					EditText et = (EditText) layout.findViewById(R.id.field_edit);
					
					if (dialogTitle.equals(Utils.EDIT_TITLE))
					{
						taskListModel.updateTitle(et.getText().toString(), currentPosition);
					}
					else
					{
						String location = et.getText().toString();
						
						if (location != null && !location.equals(""))
						{
							// check for Geocoder matches with the user entered location 
							new GeocoderProgressTask(context,currentPosition,location).execute();
						}
					}
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					dialog.cancel();
				}
			});
			return builder.create();

		case Utils.DIALOG_LONG_TEXT_ENTRY:
			layout = inflater.inflate(R.layout.alert_dialog_long_text_entry, null);
			builder.setTitle(dialogTitle)
			.setView(layout)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					EditText et = (EditText) layout.findViewById(R.id.long_field_edit);
					taskListModel.updateDescription(et.getText().toString(), currentPosition);
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
				}
			});
			return builder.create();
		}
		return null;
	}
}