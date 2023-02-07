package com.otcengineering.vitesco.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.protobuf.ByteString
import com.otc.alice.api.model.ProfileAndSettings
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Shared.OTCStatus
import com.otc.alice.api.model.Welcome
import com.otc.alice.api.model.Welcome.CountriesResponse
import com.otcengineering.otcble.core.utils.getBytes
import com.otcengineering.otcble.utils.getBytes
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.executeAPI
import com.otcengineering.vitesco.utils.getIMEI
import io.reactivex.rxjava3.core.Observable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ProfileViewModel {
    fun getUserProfile(): Observable<ProfileAndSettings.UserProfileResponse> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.profileApi.getProfile().execute().body()

                if (resp == null) {
                    observable.onError(IOException("Cannot execute API"))
                    return@executeAPI
                }

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val profile = resp.data.unpack(ProfileAndSettings.UserProfileResponse::class.java)
                    observable.onNext(profile)
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }

    fun getCountry(countryID: Int): Observable<String> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.welcomeApi.getCountries().execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null..."))
                    return@executeAPI
                }

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = resp.data.unpack(CountriesResponse::class.java)
                    for (cnt in rsp.countriesList) {
                        if (cnt.id == countryID) {
                            observable.onNext(cnt.name)
                            return@executeAPI
                        }
                    }
                    observable.onError(OtcException(Shared.OTCStatus.NO_DATA))
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }

    fun getImage(imageID: Long, ctx: Context): Observable<Drawable> {
        return Observable.create { observable ->
            val file = File(ctx.externalCacheDir, "${imageID}.png")
            if (file.exists()) {
                val bytes = file.readBytes()
                val drawable = BitmapDrawable(ctx.resources, BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                observable.onNext(drawable)
                return@create
            }
            executeAPI(observable) {
                val resp = Common.network.fileApi.getFile(imageID.toString()).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null..."))
                    return@executeAPI
                }

                val bytes = resp.bytes()
                val drawable = BitmapDrawable(ctx.resources, BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                file.writeBytes(bytes)
                observable.onNext(drawable)
            }
        }
    }

    fun putImage(image: Bitmap, name: String): Observable<Unit> {
        return Observable.create { observable ->
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val imageBuild = ProfileAndSettings.UserImage.newBuilder()
                .setData(ByteString.copyFrom(baos.toByteArray()))
                .setName(name)
                .build()

            executeAPI(observable) {
                val response = Common.network.profileApi.putUserImage(imageBuild).execute().body()

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

    fun removeImage(): Observable<Boolean> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.profileApi.deleteUserImage().execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null..."))
                    return@executeAPI
                }

                observable.onNext(resp.status == OTCStatus.SUCCESS)
            }
        }
    }

    fun getFile(fileID: Long): Observable<ByteArray> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.fileApi.getFile(fileID.toString()).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null..."))
                    return@executeAPI
                }

                val bytes = resp.bytes()
                observable.onNext(bytes)
            }
        }
    }

    fun sendToken(): Observable<Unit> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.welcomeApi.sendPushToken(
                    Welcome.PushTokenRegistration.newBuilder()
                        .setPlatform(Welcome.PushTokenRegistration.Platform.GCM)
                        .setProject(Welcome.PushTokenRegistration.Project.OTC)
                        .setToken(Common.sharedPreferences.getString(Constants.Preferences.FIREBASE_TOKEN))
                        .build()
                ).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null..."))
                    return@executeAPI
                }

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    observable.onNext(Unit)
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }
}