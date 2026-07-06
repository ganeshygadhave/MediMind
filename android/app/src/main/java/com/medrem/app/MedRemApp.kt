package com.medrem.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MedRem Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class MedRemApp : Application()
