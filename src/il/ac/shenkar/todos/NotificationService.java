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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

/** 
 * Service that creates notifications
 */
public class NotificationService extends IntentService
{
	int vibrationTime = 500;

	public NotificationService()
	{
		super("NotificationService");
	}

	@Override
	public void onHandleIntent(Intent intent)
	{
		// Vibrate the mobile phone
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		//vibrator.vibrate(vibrationTime);
		vibrator.vibrate(vibrationTime);
		
		String[] tokens = intent.getStringExtra("description").split(",");
		int id = Integer.parseInt(tokens[0]);
		String title = tokens[1];

		// Prepare intent which is triggered if the
		// notification is selected
		Intent myIntent = new Intent(this, NotificationReceiverActivity.class);
		myIntent.putExtra("taskContent", "Title: " + title + "\n\nDescription:" + tokens[2]);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, myIntent, 0);

		// Build notification
		Notification noti = new Notification.Builder(this)
		.setContentTitle(title)
		.setContentText(tokens[2]).setSmallIcon(R.drawable.icon)
		.setContentIntent(pIntent).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		//To play the default sound with your notification:
		noti.defaults |= Notification.DEFAULT_SOUND;
		
		notificationManager.notify(id, noti);
	}
} 
