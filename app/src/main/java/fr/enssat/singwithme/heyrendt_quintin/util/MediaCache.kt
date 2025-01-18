package fr.enssat.singwithme.heyrendt_quintin.util

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.ExoDatabaseProvider
import java.io.File

@UnstableApi
class MediaCache(context: Context, musicDir: String) {
    // Taille maximale du cache (en octets)
    private val cacheSize: Long = 100L * 1024L * 1024L // 100 Mo

    // Fournisseur de base de données pour Media3
    private val databaseProvider: DatabaseProvider = ExoDatabaseProvider(context)

    // Eviction LRU pour gérer la suppression des fichiers en fonction de l'espace
    private val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)

    // Répertoire du cache
    private val cacheDir = File(context.cacheDir, musicDir)

    // Instance de SimpleCache
    val simpleCache: SimpleCache = SimpleCache(cacheDir, cacheEvictor, databaseProvider)

    // Factory pour créer des DataSources avec le cache
    val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(simpleCache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}
