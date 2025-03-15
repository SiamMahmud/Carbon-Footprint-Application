package com.example.carbonfootprintcalculation.di

import com.example.carbonfootprintcalculation.util.ISharedPreferencesUtil
import com.example.carbonfootprintcalculation.util.SharePreferencesUtil
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface OtherInterfacesModule {
    @Binds
    fun bindSharePreferencesUtil(sharePreferencesUtil: SharePreferencesUtil): ISharedPreferencesUtil
}