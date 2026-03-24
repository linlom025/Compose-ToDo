package com.wisnu.kurniawan.composetodolist.features.setting.di

import com.wisnu.kurniawan.composetodolist.features.setting.data.ISettingEnvironment
import com.wisnu.kurniawan.composetodolist.features.setting.data.SettingEnvironment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SettingModule {

    @Binds
    abstract fun provideEnvironment(
        environment: SettingEnvironment
    ): ISettingEnvironment
}
