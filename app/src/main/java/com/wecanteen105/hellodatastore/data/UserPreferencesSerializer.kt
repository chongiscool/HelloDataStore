package com.wecanteen105.hellodatastore.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.wecanteen105.hellodatastore.UserPrefs
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesSerializer:Serializer<UserPrefs> {
    override val defaultValue: UserPrefs = UserPrefs.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPrefs {
       try {
           return UserPrefs.parseFrom(input)
       } catch (exception: InvalidProtocolBufferException){
           throw CorruptionException("Cannot read proto.", exception)
       }
    }

    override suspend fun writeTo(t: UserPrefs, output: OutputStream) {
       t.writeTo(output)
    }
}