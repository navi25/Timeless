package io.navendra.timeless.utils

import io.navendra.timeless.BuildConfig

object TimelessConstant{
    const val PACKAGE_NAME = BuildConfig.APPLICATION_ID
    const val IMAGE_URI = "$PACKAGE_NAME+.image_uri"
    const val FILE_AUTHORITY = "$PACKAGE_NAME+.provider"
}

