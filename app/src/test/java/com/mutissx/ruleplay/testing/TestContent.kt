package com.mutissx.ruleplay.testing

import com.mutissx.ruleplay.data.content.BundledSportsRepository
import com.mutissx.ruleplay.data.content.ContentSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Reads the real bundled content (src/main/assets is on the unit-test classpath). */
val classpathContent = ContentSource { path ->
    TestContent::class.java.classLoader!!.getResourceAsStream("content/$path")?.bufferedReader()?.use { it.readText() }
}

object TestContent {
    fun repository(language: String = "en", dispatcher: CoroutineDispatcher = Dispatchers.Unconfined) =
        BundledSportsRepository(classpathContent, { language }, dispatcher)
}
