package com.todoroo.astrid.api;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.todoroo.andlib.data.AbstractModel;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.gtasks.GtasksList;
import com.todoroo.astrid.gtasks.GtasksMetadata;

import org.tasks.R;

public class GtasksFilter extends Filter {

    private static final int CLOUD = R.drawable.ic_cloud_queue_24dp;

    public long storeId;

    protected GtasksFilter() {
        super();
    }

    public GtasksFilter(GtasksList list) {
        super(list.getName(), getQueryTemplate(list), getValuesForNewTasks(list));
        storeId = list.getId();
        icon = CLOUD;
    }

    public long getStoreId() {
        return storeId;
    }

    private static QueryTemplate getQueryTemplate(GtasksList list) {
        return new QueryTemplate().join(
                Join.left(Metadata.TABLE, Task.ID.eq(Metadata.TASK))).where(Criterion.and(
                MetadataDao.MetadataCriteria.withKey(GtasksMetadata.METADATA_KEY),
                TaskDao.TaskCriteria.notDeleted(),
                GtasksMetadata.LIST_ID.eq(list.getRemoteId()))).orderBy(
                Order.asc(Functions.cast(GtasksMetadata.ORDER, "LONG")));
    }

    private static ContentValues getValuesForNewTasks(GtasksList list) {
        ContentValues values = new ContentValues();
        values.putAll(GtasksMetadata.createEmptyMetadataWithoutList(AbstractModel.NO_ID).getMergedValues());
        values.remove(Metadata.TASK.name);
        values.put(GtasksMetadata.LIST_ID.name, list.getRemoteId());
        values.put(GtasksMetadata.ORDER.name, PermaSql.VALUE_NOW);
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(storeId);
    }

    @Override
    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        storeId = source.readLong();
    }

    /**
     * Parcelable Creator Object
     */
    public static final Parcelable.Creator<GtasksFilter> CREATOR = new Parcelable.Creator<GtasksFilter>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public GtasksFilter createFromParcel(Parcel source) {
            GtasksFilter item = new GtasksFilter();
            item.readFromParcel(source);
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GtasksFilter[] newArray(int size) {
            return new GtasksFilter[size];
        }
    };
}
