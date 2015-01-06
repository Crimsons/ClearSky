package ee.vincent.clearsky.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ee.vincent.clearsky.model.Route;

/**
 * Created by jakob on 4.01.2015.
 */
public class RoutesListAdapter extends RecyclerView.Adapter<RoutesListAdapter.ViewHolder> {

    private List<Route> routes;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView routeNameTextView;

        public ViewHolder(TextView v) {
            super(v);
            routeNameTextView = v;
        }
    }


    public RoutesListAdapter(List<Route> routes) {
        this.routes = routes;
    }


    @Override
    public RoutesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        TextView routeNameTextView = new TextView(parent.getContext());
        routeNameTextView.setTextSize(60);
        ViewHolder vh = new ViewHolder(routeNameTextView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.routeNameTextView.setText(routes.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

}
