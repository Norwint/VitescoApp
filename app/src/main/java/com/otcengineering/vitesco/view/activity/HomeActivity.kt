package com.otcengineering.vitesco.view.activity

import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.otcengineering.otcble.BleSDK
import com.otcengineering.vitesco.BuildConfig
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.ActivityHomeBinding
import com.otcengineering.vitesco.model.ProfileViewModel
import com.otcengineering.vitesco.model.WelcomeViewModel
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.service.LocationService
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.ImageUtils
import com.otcengineering.vitesco.utils.startActivity
import com.otcengineering.vitesco.view.components.TitleBar
import com.otcengineering.vitesco.view.fragment.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class HomeActivity : BaseActivity() {

    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val viewModel: ProfileViewModel by lazy { ProfileViewModel() }
    private val welcomeViewModel: WelcomeViewModel by lazy { WelcomeViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setFragment(DashboardFragment())
        binding.homeTitleBar.title = "Dashboard"

        binding.menuHome.navigationLayoutMore.visibility = View.GONE
        binding.menuHome.version = "Vitesco ${BuildConfig.VERSION_NAME}"

        viewModel.sendToken().subscribe({
            Log.d("HomeActivity", "Token sent")
        }, {
            Log.d("HomeActivity", "Token not sent")
        })


        if (BluetoothService.checkPermissions(this)) {
            LocationService.getService(this)
            BluetoothService.getService(this).executeService()
        }

        welcomeViewModel.getVehicleList().subscribe({ list ->
            val first = list.first()
            Common.macAddress = first.tcuMacAddress
            Common.sharedPreferences.putLong(Constants.Preferences.VEHICLE_ID, first.id)
            Common.serialNumber = first.tcuSerialNumber
            Common.sharedPreferences.putString(Constants.Preferences.SERIAL_NUMBER, first.tcuSerialNumber)
            Common.sharedPreferences.putString(Constants.Preferences.VIN, first.vin)
        }, {})

        binding.menuHome.txtLogout.setOnClickListener {
            Common.sharedPreferences.clear()
            BluetoothService.getService(this).bluetoothDisconnect()
            BluetoothService.getService(this).bluetoothRemoveSerialNumber()
            LoginActivity.newInstance(this)
            finish()
        }

        binding.menuHome.closeMenu.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
        }

        binding.menuHome.txtProfile.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
            setFragment(ProfileFragment())
            binding.homeTitleBar.title = "My Profile"
        }

        binding.menuHome.txtRanking.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
            startActivity(Intent(this,RankingActivity::class.java))
            binding.homeTitleBar.title = "Ranking"
        }

        binding.menuHome.txtMyRoutes.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
            setFragment(TracklogFragment())
            binding.homeTitleBar.title = "Tracklog History"
        }

        binding.menuHome.txtNotifications.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
            setFragment(NotificationHistoryFragment())
            binding.homeTitleBar.title = "Notification History"
        }

        binding.menuHome.txtProfile2.setOnClickListener {
            binding.menuHome.navigationLayoutMore.visibility = View.GONE
            setFragment(ConnectivityFragment())
            binding.homeTitleBar.title = getString(R.string.connectivity)
        }


        binding.navBar.navigationTabDashboard.setOnClickListener(createListenerForTab())
        binding.navBar.navigationTabLocation.setOnClickListener(createListenerForTab())
        binding.navBar.navigationTabNotifications.setOnClickListener(createListenerForTab())
        binding.navBar.navigationTabTracklog.setOnClickListener(createListenerForTab())

        viewModel.getUserProfile().subscribe({ profile ->
            binding.menuHome.user = profile
            if (profile.image != 0L) {
                viewModel.getImage(profile.image, this).subscribe({
                    binding.menuHome.profileImage.setImageDrawable(it)
                }, {

                })
            }
        }, {
            println(it)
        })

        binding.homeTitleBar.setListener(object: TitleBar.TitleBarListener {
            override fun onLeftClick() {
                binding.menuHome.navigationLayoutMore.visibility = View.VISIBLE
            }

            override fun onRight1Click() {

            }

            override fun onRight2Click() {

            }
        })

        BleSDK.enableBluetooth(this)

        binding.menuHome.profileImageButton.setOnClickListener { showPictureDialog() }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.menuHome.navigationLayoutMore.visibility == View.VISIBLE) {
                    binding.menuHome.navigationLayoutMore.visibility = View.GONE
                    return
                }

                finish()
            }
        })
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(R.string.select_action)
        val pictureDialogItems = arrayOf(
            getString(R.string.select_from_gallery),
            getString(R.string.capture_from_camera),
            "Remove photo"
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog: DialogInterface?, which: Int ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
                2 -> removeImage()
            }
        }
        pictureDialog.show()
    }

    private fun removeImage() {
        viewModel.removeImage().subscribe({
            if (it) {
                binding.menuHome.profileImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_placeholder_correct))
            }
        }, {})
    }

    private fun takePhotoFromCamera() {
        uriForCamera = FileProvider.getUriForFile(this, "$packageName.provider", File(externalCacheDir, "photo.png"))
        startForResultForCamera.launch(uriForCamera!!)
    }

    private var uriForCamera: Uri? = null

    private val startForResultForCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bitmap = ImageUtils.getBitmapExifCorrected(this, uriForCamera!!)
            binding.menuHome.profileImage.setImageBitmap(bitmap)

            viewModel.putImage(bitmap, "${System.currentTimeMillis()}.jpg").subscribe({
                viewModel.getUserProfile().subscribe({ profile ->
                    binding.menuHome.user = profile
                    if (profile.image != 0L) {
                        viewModel.getImage(profile.image, this).subscribe({
                            binding.menuHome.profileImage.setImageDrawable(it)
                        }, {

                        })
                    }
                }, {
                    println(it)
                })
            }, {})
        }
    }

    private val startForResultFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    if (result.data != null) {
                        val selectedImageUri: Uri? = result.data!!.data
                        val bitmap = BitmapFactory.decodeStream(
                            selectedImageUri?.let {
                                baseContext.contentResolver.openInputStream(
                                    it
                                )
                            }
                        )
                        binding.menuHome.profileImage.setImageBitmap(bitmap)
                        viewModel.putImage(bitmap, "${System.currentTimeMillis()}.jpg").subscribe({
                            viewModel.getUserProfile().subscribe({ profile ->
                                binding.menuHome.user = profile
                                if (profile.image != 0L) {
                                    viewModel.getImage(profile.image, this).subscribe({
                                        binding.menuHome.profileImage.setImageDrawable(it)
                                    }, {

                                    })
                                }
                            }, {
                                println(it)
                            })
                        }, {})
                    }
                } catch (exception: java.lang.Exception) {
                    Log.d("TAG", "" + exception.localizedMessage)
                }
            }
        }
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (galleryIntent.resolveActivity(packageManager) != null) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForResultFromGallery.launch(intent)
        } else {
            val filesIntent =
                Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForResultFromGallery.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Exception", e)
        }
        LocationService.getService(this)
        BluetoothService.getService(this).executeService()
    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun createListenerForTab(): View.OnClickListener {
        return View.OnClickListener { view: View ->
            when (view.id) {
                R.id.navigation_tabDashboard -> {
                    setFragment(DashboardFragment())
                    binding.homeTitleBar.title = "Dashboard"
                }
                R.id.navigation_tabLocation -> {
                    setFragment(DTCFragment())
                    binding.homeTitleBar.title = "DTC History"
                }
                R.id.navigation_tabNotifications -> {
                    setFragment(NotificationHistoryFragment())
                    binding.homeTitleBar.title = "Notification History"
                }
                R.id.navigation_tabTracklog -> {
                    setFragment(TracklogFragment())
                    binding.homeTitleBar.title = "Tracklog History"
                }
            }
        }
    }

    companion object {
        fun newInstance(ctx: Context) = ctx.startActivity(HomeActivity::class.java)
    }
}