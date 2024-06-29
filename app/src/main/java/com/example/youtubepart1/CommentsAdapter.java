package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Context context;
    private List<Video.Comment> commentList;
    private CommentActionsListener commentActionsListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private String currentUserName;

    public CommentsAdapter(Context context, List<Video.Comment> commentList, CommentActionsListener listener, String currentUserName) {
        this.context = context;
        this.commentList = commentList;
        this.commentActionsListener = listener;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Video.Comment comment = commentList.get(position);
        holder.commentName.setText(comment.getName());
        holder.commentTime.setText(comment.getTime());
        holder.commentText.setText(comment.getText());
        byte[] bytes = Base64.decode(comment.getProfilePic(), Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        holder.iconProfile.setImageBitmap(image);

        if (position == selectedPosition) {
            holder.commentText.setTypeface(null, Typeface.BOLD);
            holder.commentTime.setTypeface(null, Typeface.BOLD);
        } else {
            holder.commentText.setTypeface(null, Typeface.NORMAL);
            holder.commentTime.setTypeface(null, Typeface.NORMAL);
        }

        if (comment.getName().equals(currentUserName)) {
            holder.iconEdit.setVisibility(View.VISIBLE);
            holder.iconDelete.setVisibility(View.VISIBLE);

            holder.iconEdit.setOnClickListener(v -> {
                commentActionsListener.onEditComment(position);
                selectedPosition = position;
                notifyDataSetChanged();
            });

            holder.iconDelete.setOnClickListener(v -> {
                commentActionsListener.onDeleteComment(position);
                selectedPosition = position;
                notifyDataSetChanged();
            });
        } else {
            holder.iconEdit.setVisibility(View.GONE);
            holder.iconDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void addComment(Video.Comment comment) {
        commentList.add(comment);
        notifyItemInserted(commentList.size() - 1);
    }

    public void updateComment(int position, Video.Comment comment) {
        commentList.set(position, comment);
        notifyItemChanged(position);
    }

    public void deleteComment(int position) {
        commentList.remove(position);
        notifyItemRemoved(position);
        if (position == selectedPosition) {
            selectedPosition = RecyclerView.NO_POSITION;
        }
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentName;
        TextView commentTime;
        TextView commentText;
        ImageView iconProfile;
        ImageView iconEdit;
        ImageView iconDelete;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentName = itemView.findViewById(R.id.commentName);
            commentTime = itemView.findViewById(R.id.commentTime);
            commentText = itemView.findViewById(R.id.commentText);
            iconProfile = itemView.findViewById(R.id.icon_profile); // Ensure this line is correct
            iconEdit = itemView.findViewById(R.id.icon_edit);
            iconDelete = itemView.findViewById(R.id.icon_delete);
        }
    }

    public interface CommentActionsListener {
        void onEditComment(int position);
        void onDeleteComment(int position);
    }
}