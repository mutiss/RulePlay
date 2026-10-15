package com.mutissx.ruleplay.data.content

import com.mutissx.ruleplay.data.content.ContentParser.toDomain
import com.mutissx.ruleplay.domain.model.Sport
import com.mutissx.ruleplay.domain.repository.Catalog
import com.mutissx.ruleplay.domain.repository.SportsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/** Reads raw text from the bundled content folder. Abstracted so tests can read from the classpath. */
fun interface ContentSource {
    /** Returns the file at [path] (relative to the content root), or null if it does not exist. */
    fun read(path: String): String?
}

/** Supplies the language content should be resolved in (e.g. "en", "es"). */
fun interface ContentLanguageProvider {
    fun language(): String
}

/**
 * Offline encyclopedia backed by JSON files bundled in assets/content:
 * - catalog.json: categories + the lightweight sports index
 * - sports/<id>.json: the full English entry for each sport
 * - sports/<lang>/<id>.json: optional translated entries (falls back to English)
 *
 * Files are parsed lazily on [ioDispatcher] and cached per language.
 */
class BundledSportsRepository(
    private val source: ContentSource,
    private val languageProvider: ContentLanguageProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : SportsRepository {

    private val mutex = Mutex()
    private val catalogs = ConcurrentHashMap<String, Catalog>()
    private val sports = ConcurrentHashMap<String, Sport>()

    override suspend fun catalog(): Catalog {
        val language = languageProvider.language()
        catalogs[language]?.let { return it }
        return withContext(ioDispatcher) {
            mutex.withLock {
                catalogs.getOrPut(language) {
                    val text = source.read(CATALOG) ?: error("Missing $CATALOG")
                    ContentParser.parseCatalog(text, language)
                }
            }
        }
    }

    override suspend fun sport(id: String): Sport? {
        val language = languageProvider.language()
        val key = "$language/$id"
        sports[key]?.let { return it }
        val summary = catalog().sport(id) ?: return null
        return withContext(ioDispatcher) {
            val translated = if (language != DEFAULT_LANGUAGE) source.read("sports/$language/$id.json") else null
            val text = translated ?: source.read("sports/$id.json") ?: return@withContext null
            val sport = ContentParser.decodeSport(text).toDomain(
                summary = summary,
                isFallback = language != DEFAULT_LANGUAGE && translated == null,
            )
            sports.putIfAbsent(key, sport) ?: sport
        }
    }

    override fun cachedCatalog(): Catalog? = catalogs[languageProvider.language()]

    override fun cachedSport(id: String): Sport? = sports["${languageProvider.language()}/$id"]

    override suspend fun allSports(): List<Sport> = coroutineScope {
        catalog().sports.map { async { sport(it.id) } }.awaitAll().filterNotNull()
    }

    companion object {
        const val CATALOG = "catalog.json"
        const val DEFAULT_LANGUAGE = "en"
    }
}
