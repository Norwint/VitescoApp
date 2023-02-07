package com.otcengineering.vitesco.model

import android.content.Context
import com.otc.alice.api.model.General.UserProfile
import com.otc.alice.api.model.ProfileAndSettings.UserProfileResponse
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Vehicle
import com.otc.alice.api.model.Vehicle.VehicleData
import com.otc.alice.api.model.Vehicle.VehicleResponse
import com.otc.alice.api.model.Welcome
import com.otcengineering.otcble.MySharedPreferences
import com.otcengineering.otcble.NetworkSDK
import com.otcengineering.otcble.core.Prefs
import com.otcengineering.vitesco.utils.*
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.RuntimeException

class WelcomeViewModel {
    private val network: NetworkSDK by lazy { Common.network }

    fun login(phoneNumber: String, password: String): Observable<Welcome.LoginResponse> {
        return Observable.create { observable ->
            val loginRequest = Welcome.Login.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setPassword(password)
                .setMobileIMEI(getIMEI())
                .build()

            executeAPI(observable) {
                val response = network.welcomeApi.login(loginRequest).execute().body()

                if (response == null) {
                    observable.onError(IOException("Cannot execute API"))
                    return@executeAPI
                }

                if (response.status != Shared.OTCStatus.SUCCESS) {
                    observable.onError(OtcException(response.status))
                } else {
                    val loginResponse = response.data.unpack(Welcome.LoginResponse::class.java)
                    network.tokenSession.setAuthToken(loginResponse.apiToken)
                    network.tokenSession.setUserID(loginResponse.userId)
                    Common.sharedPreferences.putString(Constants.Preferences.PHONE_NUMBER, phoneNumber)
                    Common.sharedPreferences.putString(Constants.Preferences.PASSWORD, password)
                    observable.onNext(loginResponse)
                }
            }
        }
    }

    fun getProfile(): Observable<UserProfileResponse> {
        return Observable.create { observable ->
            runBlocking {
                val response = network.profileApi.getProfile().execute().body()

                if (response == null) {
                    observable.onError(IOException("Cannot execute API"))
                    return@runBlocking
                }

                if (response.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = response.data.unpack(UserProfileResponse::class.java)
                    observable.onNext(rsp)
                } else {
                    observable.onError(OtcException(response.status))
                }
            }
        }
    }

    fun changePhone(phoneNumber: String, password: String): Observable<Unit> {
        return Observable.create { observable ->
            val changePhoneRequest = Welcome.ChangePhone.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setPassword(password)
                .setMobileIMEI(getIMEI())
                .build()
            executeAPI(observable) {
                val response = network.welcomeApi.changeMobile(changePhoneRequest).execute().body()

                if (response == null) {
                    observable.onError(IOException("Cannot execute API"))
                    return@executeAPI
                }

                if (response.status == Shared.OTCStatus.SUCCESS) {
                    observable.onNext(Unit)
                } else {
                    observable.onError(OtcException(response.status))
                }
            }
        }
    }

    fun getVehicleList(): Observable<List<VehicleData>> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val response = network.vehicleApi.getVehicles().execute().body()

                if (response == null) {
                    observable.onError(IOException("Cannot execute API"))
                    return@executeAPI
                }

                if (response.status == Shared.OTCStatus.SUCCESS) {
                    val vehList = response.data.unpack(VehicleResponse::class.java)
                    observable.onNext(vehList.vehiclesList)
                } else {
                    observable.onError(OtcException(response.status))
                }
            }
        }
    }
}