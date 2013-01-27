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

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

/** 
 * Custom adapter for the apllication's list view  
 */
public class ItemListBaseAdapter extends BaseAdapter
{	
	private TaskList				taskListModel;
	private LayoutInflater 			l_Inflater;
	private NoticeAlarmListener 	mListener;
	private static final int 		IMPORTANT_ON = R.drawable.red_spacer2;
	private static final int 		IMPORTANT_OFF = R.drawable.spacer;

	public ItemListBaseAdapter(Context context)
	{
		taskListModel = TaskList.getSingletonObject(context);
		l_Inflater = LayoutInflater.from(context);

		try 
		{	// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (NoticeAlarmListener) context;
		}
		catch (ClassCastException e)
		{	// The activity doesn't implement the interface, throw exception
			throw new ClassCastException("MainActivty must implement NoticeAlarmListener");
		}
	}

	public interface NoticeAlarmListener 
	{
		public void onAlarmClick(int position);
	}

	@Override
	public int getCount()
	{
		return taskListModel.getTasks().size();
	}

	@Override
	public Object getItem(int position)
	{
		return taskListModel.getTaskAt(position);
	}

	@Override
	public long getItemId(int position)
	{
		return taskListModel.getTaskAt(position).getId();
	}

	private final OnCheckedChangeListener doneCheckBoxOnClickListener = new OnCheckedChangeListener()
	{
		@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			int position = (Integer) buttonView.getTag();
			// set text to strike through
			if (isChecked)
			{
				taskListModel.getTaskAt(position).setCheckBoxState(isChecked);
				taskListModel.getTaskAt(position).
					setTextResource(Paint.STRIKE_THRU_TEXT_FLAG);
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			// remove strike through paint
			else
			{
				taskListModel.getTaskAt(position).setCheckBoxState(isChecked);
				taskListModel.getTaskAt(position).
					setTextResource((~ Paint.STRIKE_THRU_TEXT_FLAG));
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			notifyDataSetChanged();
		}
	};

	//set the alarm button click listener
	private final OnClickListener notificationClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			int position = (Integer) view.getTag();
			mListener.onAlarmClick(position);
			notifyDataSetChanged();
		}
	};
	
	/**
	 * Synchronize list view item appearance
	 * to updated data
	 * 
	 * @param holder
	 * @param position
	 */
	public void syncItemAppearance(ViewHolder holder, int position)
	{
		// update task title text
		holder.txt_itemTitle.setText(taskListModel.getTaskAt(position).getTaskTitle());
		// update task alarm button image
		holder.notificationButton.setBackgroundResource(taskListModel.getTaskAt(position).getAlarmImage());
		// update task check box state
		holder.doneCheckBox.setChecked(taskListModel.getTaskAt(position).isCheckBoxState());
		
		if (taskListModel.getTaskAt(position).isCheckBoxState())
		{
			holder.txt_itemTitle.setPaintFlags(holder.txt_itemTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		else
		{
			holder.txt_itemTitle.setPaintFlags(holder.txt_itemTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
		}

		if (taskListModel.getTaskAt(position).isImportant())
		{
			holder.spacer.setBackgroundResource(IMPORTANT_ON);
		}
		else
		{
			holder.spacer.setBackgroundResource(IMPORTANT_OFF);
		}
	}

	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		final ViewHolder holder;

		// only when adding a new object to the list allocate new memory
		// and use findViewById, saves huge amount of resources
		if (convertView == null) 
		{
			convertView = l_Inflater.inflate(R.layout.item_details_view, null);
			holder = new ViewHolder();

			holder.txt_itemTitle = (TextView) convertView.findViewById(R.id.title);
			holder.doneCheckBox = (CheckBox) convertView.findViewById(R.id.btnDone);
			holder.notificationButton = (ImageButton) convertView.findViewById(R.id.setNotification);
			holder.spacer = (ImageButton) convertView.findViewById(R.id.setImportent);

			holder.doneCheckBox.setOnCheckedChangeListener(doneCheckBoxOnClickListener);
			holder.notificationButton.setOnClickListener(notificationClickListener);
			
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.doneCheckBox.setTag(position);
		holder.notificationButton.setTag(position);
		
		syncItemAppearance(holder,position);
		return convertView;
	}

	// stores list view data in a static way to avoid
	// using every time the expansive findViewById function
	static class ViewHolder
	{
		TextView txt_itemTitle;
		CheckBox doneCheckBox;
		ImageButton notificationButton;
		ImageButton spacer;
	}
}
