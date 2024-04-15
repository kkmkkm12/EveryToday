package com.example.everytoday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 처음에 표시할 프래그먼트를 설정합니다.
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();

        // 네비게이션 바의 아이템 클릭 이벤트를 처리합니다.
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if(item.getItemId() == R.id.nav_home)
                selectedFragment = new MainFragment();
            else if(item.getItemId() == R.id.nav_dashboard)
                selectedFragment = new GoalListFragment();
            else if(item.getItemId() == R.id.nav_notifications)
                selectedFragment = new CalenderFragment();

            // 선택된 프래그먼트를 화면에 표시합니다.
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
        });
    }
}