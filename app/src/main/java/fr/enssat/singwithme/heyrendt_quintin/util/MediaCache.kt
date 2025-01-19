package fr.enssat.singwithme.heyrendt_quintin.util

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import androidx.media3.datasource.cache.CacheDataSource.Factory as CacheDataSourceFactory

/**
 * Object pour la gestion du cache de ExoPlayer
 */
@UnstableApi
object MediaCache {

    private const val CACHE_SIZE: Long = 100 * 1024 * 1024 // 100 MB
    private var simpleCache: SimpleCache? = null

    /**
     * Singleton pour l'instance de cache
     */
    private fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDirectory = File(context.cacheDir, "media3_cache")
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
            val databaseProvider: DatabaseProvider = ExoDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDirectory, cacheEvictor, databaseProvider)
        }
        return simpleCache!!
    }

    /**
     * Factory pour le cache du lecteur audio
     */
    fun createCacheDataSourceFactory(context: Context): CacheDataSourceFactory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val cache = getSimpleCache(context)
        return CacheDataSourceFactory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
