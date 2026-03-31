package com.example.takstud.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule - Provê dependências globais da aplicação via Hilt
 *
 * Responsabilidades:
 * - Prover instâncias do Firebase (Firestore, Storage, RemoteConfig)
 * - Repositories são injetados automaticamente via @Inject constructor
 *
 * Nota: Todos os repositórios já têm @Singleton e @Inject constructor,
 * então não precisam de providers manuais aqui.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    @Singleton
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }
}
