package lk.ac.kln.todoapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private TabLayout tabLayout;
    private Button btnSkip;
    private Button btnNext;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        viewPager = findViewById(R.id.onboardViewPager);
        tabLayout = findViewById(R.id.onboardTabLayout);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        currentUserId = getIntent().getIntExtra("user_id", prefs.getInt("current_user_id", -1));

        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(R.drawable.onboard_image_1, "Welcome to Target", "Create tasks, set due dates and stay organized."));
        items.add(new OnboardingItem(R.drawable.onboard_image_2, "Edit & View", "Swipe right to edit and view tasks quickly."));
        items.add(new OnboardingItem(R.drawable.onboard_image_3, "Delete tasks", "Swipe left to delete tasks quickly."));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int p = viewPager.getCurrentItem();
            if (p + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(p + 1, true);
            } else {
                finishOnboarding();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Done");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void finishOnboarding() {
        if (currentUserId != -1) {
            SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("seen_onboarding_for_user_" + currentUserId, true).apply();
        }

        Intent i = new Intent(OnboardingActivity.this, MainActivity.class);
        i.putExtra("user_id", currentUserId);
        startActivity(i);
        finish();
    }
}
