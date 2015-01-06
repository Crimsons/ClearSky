package ee.vincent.clearsky.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ee.vincent.clearsky.R;
import ee.vincent.clearsky.model.Route;

/**
 * Created by jakob on 4.01.2015.
 */
public class RoutesListAdapter extends RecyclerView.Adapter<RoutesListAdapter.ViewHolder> {

    private OnListItemClickListener listener;
    private List<Route> routes;


    public RoutesListAdapter(List<Route> routes) {
        this.routes = routes;
    }


    @Override
    public RoutesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View listItemLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_routes, parent, false);
        return new ViewHolder(listItemLayout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(routes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }


    public Route getItem(int position) {
        return routes.get(position);
    }

    public void setOnItemCLickListener(OnListItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView routeNameText;


        public ViewHolder(View view) {
            super(view);

            routeNameText = (TextView)view.findViewById(R.id.route_name);
        }

        public void bindData(Route route, final OnListItemClickListener listener) {

            routeNameText.setText(route.getName());
            routeNameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onListItemClick(getPosition());
                }
            });

        }

    }

    public interface OnListItemClickListener {
        public void onListItemClick(int position);
    }

}
