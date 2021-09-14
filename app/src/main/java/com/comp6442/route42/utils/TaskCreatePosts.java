package com.comp6442.route42.utils;

import android.content.Context;

import com.comp6442.route42.BuildConfig;
import com.comp6442.route42.R;
import com.comp6442.route42.data.model.Post;
import com.comp6442.route42.data.model.User;
import com.comp6442.route42.data.model.UserLike;
import com.comp6442.route42.data.repository.PostRepository;
import com.comp6442.route42.data.repository.UserLikeRepository;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class TaskCreatePosts extends DataTask {
  private final List<Post> postList;
  private final List<UserLike> userLikesList;
  private int idx = 0;

  public TaskCreatePosts(Context context, boolean debug, boolean demo) {
    super(debug, demo, "posts", PostRepository.getInstance(), Post.class, context);
    gson = PostRepository.getJsonDeserializer();
    jsonString = readTextFile(demoContext.getResources().openRawResource(R.raw.posts));
    postList = Arrays.asList(gson.fromJson(jsonString, (Type) Post[].class));
    Collections.shuffle(postList);

    InputStream inputStream = context.getResources().openRawResource(R.raw.user_likes);
     userLikesList = UserLikeRepository.getInstance().deserializeJSON(inputStream);
    Timber.i("Created fake UserLike on posts");
  }

  @Override
  public void run() {
    if (this.mAuth.getCurrentUser() == null) {
      Timber.i("Signed in as test user");
      this.mAuth.signInWithEmailAndPassword(BuildConfig.testUserEmail, BuildConfig.testUserPassword);
    }
    if (this.mAuth.getCurrentUser() == null) Timber.w("not signed in");
    else {
      createPosts();
      Timber.d("Task completed.");
    }
  }

  private void createPosts() {
    if (!DEMO) {
      PostRepository.getInstance().createMany(postList);
      UserLikeRepository.getInstance().createMany(userLikesList);
    }
    else {
      createPostsMiniBatch(
              BuildConfig.batchSize,
              Math.min(postList.size(), BuildConfig.demoPostLimit)
      );
    }
    Timber.i("Created posts.");
  }



  public void createPostsMiniBatch(int batchSize, int limit) {
    Timber.i("Creating posts %d - %d", idx, Math.min(postList.size(), idx + BuildConfig.batchSize));

    int prevIdx = idx;
    idx += batchSize;
    if (idx + batchSize < limit) {
      PostRepository.getInstance().setMany(postList.subList(prevIdx, idx));
      UserLikeRepository.getInstance().setMany(userLikesList.subList(prevIdx, idx));
    } else {
      Thread.currentThread().interrupt();
      Timber.i("Completed demo.");
    }
  }
}
