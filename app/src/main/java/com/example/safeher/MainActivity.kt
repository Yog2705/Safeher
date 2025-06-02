package com.example.safeher

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.telephony.SmsManager
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.sqrt
import android.content.SharedPreferences
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private val shakeThreshold = 12f
    private val shakeInterval = 1000
    private val ADD_CONTACT_REQUEST = 1001

    private var isPanicModeActive = false
    private var mediaPlayer: MediaPlayer? = null
    private val panicHandler = Handler(Looper.getMainLooper())
    private val panicInterval = 2 * 60 * 1000L
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (loadDarkModePref()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, MultiLanguageActivity::class.java)
        startActivity(intent)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.nav_header_email)

      // Example: Get user from FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser
        usernameTextView.text = user?.displayName ?: "Unknown User"
        emailTextView.text = user?.email ?: "Unknown Email"

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        loadTrustedContactsIntoNavMenu()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                R.id.nav_multilanguage -> {
                    val intent = Intent(this, MultiLanguageActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.nav_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<Button>(R.id.btn_add_contact).setOnClickListener {
            startActivityForResult(Intent(this, AddContactActivity::class.java), ADD_CONTACT_REQUEST)
        }
        findViewById<Button>(R.id.btnSOS).setOnClickListener { sendSOS() }
        findViewById<Button>(R.id.btnCallPolice).setOnClickListener { callPolice() }
        findViewById<Button>(R.id.btnSafePlace).setOnClickListener { startActivity(Intent(this, SafePlaceActivity::class.java)) }
        findViewById<Button>(R.id.btnPanicMode).setOnClickListener { togglePanicMode() }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull {
            cameraManager.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun loadDarkModePref(): Boolean =
        getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark_mode", false)

    private fun loadTrustedContactsIntoNavMenu() {
        val contacts = ContactStorage.loadContacts()
        val menu = navView.menu
        for (i in menu.size() - 1 downTo 0) {
            if (menu.getItem(i).itemId in 2000..2999) menu.removeItem(menu.getItem(i).itemId)
        }
        contacts.forEachIndexed { index, contact ->
            val menuItem = menu.add(R.id.nav_trusted_contacts_submenu, 2000 + index, Menu.NONE, "${contact.name} (${contact.phone})")
            menuItem.setIcon(R.drawable.ic_contacts)
            menuItem.setOnMenuItemClickListener {
                showRemoveContactDialog(contact)
                true
            }
        }
    }

    private fun showRemoveContactDialog(contact: ContactStorage.Contact) {
        AlertDialog.Builder(this)
            .setTitle("Remove Contact")
            .setMessage("Are you sure you want to remove ${contact.name}?")
            .setPositiveButton("Yes") { _, _ ->
                ContactStorage.removeContact(contact)
                loadTrustedContactsIntoNavMenu()
                Toast.makeText(this, "${contact.name} removed.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun togglePanicMode() {
        if (isPanicModeActive) deactivatePanicMode() else activatePanicMode()
    }

    private fun activatePanicMode() {
        isPanicModeActive = true
        playSiren()
        startFlashing()
        sendSOS()
        panicHandler.postDelayed(panicRunnable, panicInterval)
        Toast.makeText(this, "Panic Mode Activated", Toast.LENGTH_SHORT).show()
    }

    private fun deactivatePanicMode() {
        isPanicModeActive = false
        stopSiren()
        stopFlashing()
        panicHandler.removeCallbacks(panicRunnable)
        Toast.makeText(this, "Panic Mode Deactivated", Toast.LENGTH_SHORT).show()
    }

    private val panicRunnable = object : Runnable {
        override fun run() {
            if (isPanicModeActive) {
                sendSOS()
                panicHandler.postDelayed(this, panicInterval)
            }
        }
    }

    private fun playSiren() {
        mediaPlayer = MediaPlayer.create(this, R.raw.siren)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun stopSiren() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startFlashing() {
        cameraId?.let { id ->
            val flashHandler = Handler(Looper.getMainLooper())
            val flashRunnable = object : Runnable {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun run() {
                    if (isPanicModeActive) {
                        try {
                            cameraManager.setTorchMode(id, true)
                            flashHandler.postDelayed({ cameraManager.setTorchMode(id, false) }, 500)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                        flashHandler.postDelayed(this, 1000)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flashRunnable.run()
            }
        }
    }

    private fun stopFlashing() {
        cameraId?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(it, false)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendSOS() {
        val contacts = ContactStorage.loadContacts()
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No trusted contacts found.", Toast.LENGTH_SHORT).show()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1002)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val message = location?.let {
                "I'm in danger! My location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "I'm in danger! Location not available."

            val smsManager = SmsManager.getDefault()
            contacts.forEach { contact ->
                smsManager.sendTextMessage(contact.phone, null, message, null, null)
            }
            Toast.makeText(this, "SOS sent to trusted contacts.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callPolice() {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:100")))
    }

    private fun logout() {
        getSharedPreferences("user_session", Context.MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            val currentTime = System.currentTimeMillis()
            if (acceleration > shakeThreshold && currentTime - lastShakeTime > shakeInterval) {
                lastShakeTime = currentTime
                sendSOS()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
