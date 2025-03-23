package com.example.demo3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import com.example.demo3.model.Badge;

import java.util.List;

public class AchievementAdapter extends BaseAdapter {
    private final Context context;
    private final List<Badge> badges;

    public AchievementAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @Override
    public int getCount() {
        return badges.size();
    }

    @Override
    public Object getItem(int position) {
        return badges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_achievement_badge, parent, false);
        }

        Badge badge = badges.get(position);

        ImageView badgeImage = convertView.findViewById(R.id.badgeImage);
        TextView badgeTitle = convertView.findViewById(R.id.badgeTitle);

        badgeImage.setImageResource(badge.getImageResId());
        badgeTitle.setText(badge.getTitle());

        // Set click listener for more details
        convertView.setOnClickListener(v -> {
            showBadgeDetails(badge);
        });

        return convertView;
    }

    private void showBadgeDetails(Badge badge) {
        // Show a dialog with badge details
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(badge.getTitle())
                .setMessage(badge.getDescription())
                .setPositiveButton("OK", null)
                .show();
    }
}

