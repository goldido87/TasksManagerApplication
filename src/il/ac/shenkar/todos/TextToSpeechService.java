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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

/**
 * Service that triggers on widget button press.
 * speaks today's tasks 
 */
public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener
{
	private TextToSpeech speakEngine;
	private String spokenText;
	private TaskList taskListModel;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		speakEngine = new TextToSpeech(this, this);
		taskListModel = TaskList.getSingletonObject(this);
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onInit(int status) 
	{
		spokenText = getSpokenText();

		speakEngine.setOnUtteranceProgressListener(new UtteranceProgressListener()
		{
			@Override
			public void onDone(String utteranceId)
			{
				if (speakEngine != null) 
				{
					speakEngine.stop();
					speakEngine.shutdown();
				}
			}
			@Override
			public void onError(String utteranceId) 
			{

			}
			@Override
			public void onStart(String utteranceId) 
			{

			}
		});

		if (status == TextToSpeech.SUCCESS)
		{
			int result = speakEngine.setLanguage(Locale.US);
			if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
			{
				speakEngine.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}

	/**
	 * Builds a string to speak with today's tasks
	 * 
	 * @return the string to speak
	 */
	private String getSpokenText()
	{
		String speakText = null;
		int taskNumber = 1;
		Calendar currentDate = Calendar.getInstance();		
		Calendar taskDate = Calendar.getInstance();
		Locale local = new Locale("EEE MMM dd HH:mm:ss z yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",local);

		speakText = "Today's task are:,";

		for (int i=0; i < taskListModel.getTasks().size(); i++)
		{
			try 
			{	// check if there is a notification time
				if (! taskListModel.getTaskAt(i).getDate().equals(Utils.DEFUALT_NOTIFICATION))
				{
					taskDate.setTime(sdf.parse(taskListModel.getTaskAt(i).getDate()));
				}
				else { continue; }
			} 
			catch (ParseException e)
			{
				e.printStackTrace();
			}

			// speak only tasks for today
			if (Utils.isSameDay(currentDate, taskDate))
			{
				speakText += "task " + taskNumber + "," + taskListModel.getTaskAt(i).getTaskTitle() + ",";
				taskNumber++;
			}
		}

		if (taskNumber == 1)
		{
			speakText = "There are no tasks for today";
		}
		return speakText;
	}
}