package io.scrollback.neighborhoods;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.scrollback.neighborhoods.data.AreaModel;

public abstract class AreaViewHolder extends RecyclerView.ViewHolder {

    private final TextView name;
    private final TextView description;
    private final TextView distance;

    private AreaModel itemModel;

    public AreaViewHolder(View view) {
        super(view);

        name = (TextView) itemView.findViewById(R.id.area_name);
        description = (TextView) itemView.findViewById(R.id.area_description);
        distance = (TextView) itemView.findViewById(R.id.area_distance);

        (itemView.findViewById(R.id.area_item)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(itemModel);
            }
        });
    }

    public abstract void onItemClick(AreaModel model);

    public void bind(AreaModel model) {
        itemModel = model;

        name.setText(model.getName());

        final double meters = model.getDistFromLocation();

        if (meters != 0.0) {
            if (meters > 1000) {
                distance.setText(String.format("%.2f km", meters / 1000.0));
            } else {
                distance.setText(String.format("%d m", Math.round(meters)));
            }
        }

        description.setText(model.getDescription());
    }
}
