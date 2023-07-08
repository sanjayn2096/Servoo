import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.servoo.R

import com.example.servoo.dao.UserDao
import com.example.servoo.data.Restaurant
import com.example.servoo.data.VerificationStatus
import com.example.servoo.data.model.UserInfo
import com.example.servoo.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userDao: UserDao
    private lateinit var userInfoFromLogin: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        userDao = UserDao()
        setContentView(binding.root)

        val userInfoJson = intent.getStringExtra("userInfo")
        val gson = Gson()
        userInfoFromLogin = gson.fromJson(userInfoJson, UserInfo::class.java)

        val bundle = Bundle().apply {
            putString("userInfo", userInfoJson)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.tab_restaurant_fragment).isChecked = true
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_restaurant_fragment -> {
                    if (getRestaurants(userInfoFromLogin).isNotEmpty())
                        navController.navigate(R.id.RestaurantFragment)
                    else
                        navController.navigate(R.id.AddRestaurantFragment)
                    true
                }
                R.id.tab_menu_fragment -> {
                    if (getRestaurants(userInfoFromLogin).isNotEmpty())
                        navController.navigate(R.id.MenuFragment)
                    else
                        navController.navigate(R.id.AddRestaurantFragment)
                    true
                }
                R.id.tab_orders_fragment -> {
                    if (getRestaurants(userInfoFromLogin).isNotEmpty() &&
                        getVerfiedRestaurantList(userInfoFromLogin).isNotEmpty())
                        navController.navigate(R.id.OrdersFragment)
                    else
                        navController.navigate(R.id.AddRestaurantFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun getRestaurants(userInfo: UserInfo) : List<Restaurant> {
        var restaurants : List<Restaurant> = emptyList()
        userDao.getUserRestaurants(userInfo,
            onSuccess = { fetchedRestaurants ->
                restaurants = fetchedRestaurants
            },
            onFailure = { e ->
                // Error occurred while fetching the restaurants
                // Handle the error here, if needed
                Log.e(TAG, "Error : $e")
            }
        )
        return restaurants
    }

    private fun getVerfiedRestaurantList(userInfo: UserInfo) : List<Restaurant> {
        val  allRestaurants : List<Restaurant> = getRestaurants(userInfo)
        var filteredRests : List<Restaurant> = emptyList()
        for (r in allRestaurants) {
            if (r.verificationStatus == VerificationStatus.VERIFIED) {
                filteredRests.plus(r)
            }
        }
        return filteredRests
    }

    //private fun getVerifiedRestaurantList


    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
