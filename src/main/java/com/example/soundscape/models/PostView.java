package com.example.soundscape.models;

public class PostView {
    private final Post post;
    private final String avatarUrl;

    public PostView(Post post, String avatarUrl) {
        this.post = post;
        this.avatarUrl = avatarUrl;
    }

    public Post getPost() { return post; }
    public String getAvatarUrl() { return avatarUrl; }
}
